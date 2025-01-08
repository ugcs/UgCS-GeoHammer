package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.events.FileClosedEvent;
import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.event.FileSelectedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Component
public class ProfileView implements InitializingBean {

	private final Model model;
	private final AuxElementEditHandler auxEditHandler;
	private final Navigator navigator;
	private final Saver saver;

	private final ToggleButton auxModeBtn = new ToggleButton("aux");

	private final ToolBar toolBar = new ToolBar();
	
	private final Button zoomInBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ZOOM_IN, new Button());
	private final Button zoomOutBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ZOOM_OUT, new Button());

	private SgyFile currentFile;

	public ProfileView(Model model, AuxElementEditHandler auxEditHandler, Navigator navigator,
                       Saver saver) {
		this.model = model;
        this.auxEditHandler = auxEditHandler;
        this.navigator = navigator;
		this.saver = saver;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		prepareToolbar();
		zoomInBtn.setTooltip(new Tooltip("Zoom in"));
		zoomOutBtn.setTooltip(new Tooltip("Zoom out"));
		zoomInBtn.setOnAction(e -> {
			//FIXME: after merge
			//model.getProfileField().zoom(1, false);
		});
		zoomOutBtn.setOnAction(e -> {
			//model.getProfileField().zoom(-1, false);
		});
	}

	private void prepareToolbar() {
		toolBar.setDisable(true);

		toolBar.getItems().addAll(saver.getToolNodes());
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().addAll(auxEditHandler.getRightPanelTools());
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().addAll(navigator.getToolNodes());
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().add(zoomInBtn);
		toolBar.getItems().add(zoomOutBtn);
		toolBar.getItems().add(getSpacer());
	}

	private VBox center;

	private Pane profileScrollPane;

	//center
	public Node getCenter() {
		if (center == null) {
			center = new VBox();
			center.setMinWidth(100);

			ScrollPane centerScrollPane = new ScrollPane();
			centerScrollPane.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

			profileScrollPane = new Pane();
			profileScrollPane.setMinHeight(ProfileScroll.HEIGHT);
			profileScrollPane.setPrefHeight(ProfileScroll.HEIGHT);
			profileScrollPane.setPrefWidth(center.getWidth());

			center.getChildren().addAll(toolBar, centerScrollPane);

			centerScrollPane.setFitToWidth(true);
			centerScrollPane.setFitToHeight(true);
			centerScrollPane.setContent(model.getChartsContainer());
		}

		return center;
	}

	public List<Node> getRight(SgyFile file) {
		var contrastNode = model.getProfileField(file).getContrastSlider().produce();
		return List.of(contrastNode);
	}

	@EventListener
	private void somethingChanged(WhatChanged changed) {
		if (changed.isJustdraw() && currentFile != null) {
			var gprChart = model.getProfileField(currentFile);
			//TODO: filter events
			gprChart.updateScroll();
			gprChart.repaintEvent();
		}
	}

	@EventListener
	private void fileClosed(FileClosedEvent event) {
		SgyFile closedFile = event.getSgyFile();
		var gprPane = model.getProfileField(closedFile);
		var vbox = (VBox) gprPane.getRootNode();

		//TODO: fix visible
		//gprPane.getProfileScroll().setVisible(model.isActive() && gprPane.getTracesCount() > 0);
		//vbox.setVisible(model.isActive() && gprPane.getTracesCount() > 0);

		gprPane.getProfileScroll().setVisible(false);
		model.getChartsContainer().getChildren().remove(vbox);

		model.removeChart(closedFile);
	}

	@EventListener
	private void fileOpened(FileOpenedEvent event) {

		List<File> openedFiles = event.getFiles();
		for (File file : openedFiles) {
			System.out.println("ProfileView.fileOpened " + file.getAbsolutePath());
			model.getFileManager().getGprFiles().stream().filter(f -> f.getFile().equals(file)).findFirst().ifPresent(f -> {
				System.out.println("Loaded traces: " + f.getTraces().size());
				var gprPane = model.getProfileField(f);
				var vbox = (VBox) gprPane.getRootNode();

				//TODO:
				//gprPane.clear();
				//model.updateSgyFileOffsets();

				if (!model.getChartsContainer().getChildren().contains(vbox)) {
					model.getChartsContainer().getChildren().add(vbox);
					vbox.setPrefHeight(Math.max(400, vbox.getScene().getHeight()));
					vbox.setMinHeight(Math.max(400, vbox.getScene().getHeight() / 2));
				}

				fileSelected(new FileSelectedEvent(this, f));
				model.selectAndScrollToChart(gprPane);

			});
		}

		toolBar.setDisable(!model.isActive());
	}

	@EventListener
	private void fileSelected(FileSelectedEvent event) {
		if (event.getFile() != null && !event.getFile().equals(currentFile)) {
			currentFile = event.getFile();

			if (center.getChildren().get(1) instanceof ProfileScroll) {
				center.getChildren().remove(1);
			}

			var gprChart = model.getProfileField(currentFile);

			var profileScroll = gprChart.getProfileScroll();
			profileScroll.setVisible(true);
			center.getChildren().add(1, profileScroll);

			ChangeListener<Number> sp2SizeListener = (observable, oldValue, newValue) -> {
				if (Math.abs(newValue.intValue() - oldValue.intValue()) > 1) {
					gprChart.setSize((int) (center.getWidth() - 21), (int) (Math.max(400, ((VBox) gprChart.getRootNode()).getHeight()) - 4));
				}
			};
			center.widthProperty().addListener(sp2SizeListener);
			//((VBox) gprChart.getRootNode()).widthProperty().addListener(sp2SizeListener);
			((VBox) gprChart.getRootNode()).heightProperty().addListener(sp2SizeListener);
		}
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}
}