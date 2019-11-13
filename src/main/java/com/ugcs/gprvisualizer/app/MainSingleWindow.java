package com.ugcs.gprvisualizer.app;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.thecoldwine.sigrun.common.ext.AmplitudeMatrix;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainSingleWindow extends Application implements SmthChangeListener {

	private Scene scene;
	private Stage stage;
	private BorderPane bPane;
	private ModeFactory modeFactory;
	private VBox rightBox = new VBox();
	//private VBox centerBox = new VBox();
	private Model model = new Model();
	private ToolBar toolBar = new ToolBar();

	// modes
	private ToggleGroup group = new ToggleGroup();
	private ToggleButton gpsMode = new ToggleButton("GPS", null);
	private ToggleButton prismMode = new ToggleButton("Prism", null);
	private ToggleButton cutMode = new ToggleButton("Waveform", null);
	private ToggleButton matrixMode = new ToggleButton("Matrix", null);
	Map<Node, ModeFactory> modeMap = new HashMap<>();
	{
	}

	public MainSingleWindow() {
		AppContext.levelFilter = new LevelFilter(model);
		AppContext.loader = new Loader(model, this);
		AppContext.saver = new Saver(model);
		AppContext.pluginRunner = new PluginRunner(model);
		

		gpsMode.setToggleGroup(group);
		prismMode.setToggleGroup(group);
		cutMode.setToggleGroup(group);
		matrixMode.setToggleGroup(group);

		modeMap.put(cutMode, new VerticalCut(model));

		modeMap.put(prismMode, new PrismModeFactory(model, this));
		
		MatrixModeFactory tmf = new MatrixModeFactory(model, this);
		modeMap.put(matrixMode, tmf);
		AppContext.smthListener.add(tmf);

		LayersWindowBuilder layersWindowBuilder = new LayersWindowBuilder(model);
		modeMap.put(gpsMode, layersWindowBuilder);
		AppContext.smthListener.add(layersWindowBuilder);

		
		group.selectedToggleProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable o) {
				if (group.getSelectedToggle() == null) {

				} else {
					setModeFactory(modeMap.get(group.getSelectedToggle()));
				}
			}
		});

		
	}
	//python "d:/install/sgy_processing/main.py" "d:\georadarData\mines\2019-08-29-12-48-48-gpr_0005.SGY" --model "d:\install\sgy_processing\model.pb"
	//python "d:/install/sgy_processing/main.py" "d:\georadarData\mines\2019-08-29-12-48-48-gpr_0005.SGY" --model "d:\install\sgy_processing\model.pb" --no_progressbar
	//python "d:/install/sgy_processing/main.py" "d:\georadarData\mines\2019-08-29-12-48-48-gpr_0005.SGY" --model "d:\install\sgy_processing\model.pb" --no_progressbar -t 0.4
	
	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		this.stage = stage;

		scene = createScene();

		stage.setTitle("UgCS GPR data visualizer");
		stage.setScene(scene);
		stage.show();

	}

	private Scene createScene() {

		bPane = new BorderPane();

		bPane.setOnDragOver(AppContext.loader.getDragHandler());
		bPane.setOnDragDropped(AppContext.loader.getDropHandler());

		bPane.setTop(getToolBar());
		bPane.setRight(getRightPane());
		//bPane.setCenter(centerBox);

		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			showCenter();
		};
		bPane.widthProperty().addListener(stageSizeListener);
		bPane.heightProperty().addListener(stageSizeListener);

		scene = new Scene(bPane, 1024, 768);
		return scene;
	}

	private Node getToolBar() {

		toolBar.getItems().addAll(AppContext.saver.getToolNodes());
		toolBar.getItems().addAll(AppContext.pluginRunner.getToolNodes());
		
		Region r = new Region();
		r.setPrefWidth(10);
		toolBar.getItems().add(r);
		
		toolBar.getItems().addAll(gpsMode, prismMode, cutMode, matrixMode);
		
		Region r2 = new Region();
		r2.setPrefWidth(10);
		toolBar.getItems().add(r2);
		
		toolBar.getItems().addAll(AppContext.levelFilter.getToolNodes());
		
//		Button tstbtn = new Button("test dlg");
		
//		tstbtn.setOnAction(new EventHandler<ActionEvent>() {
//		    @Override public void handle(ActionEvent e) {
//		    	new TaskRunner(stage).start();
//		    }
//		});
				
		
//		toolBar.getItems().add(tstbtn);
		
		return toolBar;
	}

	public ModeFactory getModeFactory() {
		return modeFactory;
	}

	public void setModeFactory(ModeFactory modeFactory) {
		this.modeFactory = modeFactory;

		bPane.setCenter(getModeFactory().getCenter());
		//centerBox.getChildren().clear();
		//centerBox.getChildren().add(getModeFactory().getCenter());

		rightBox.getChildren().clear();
		rightBox.getChildren().addAll(getModeFactory().getRight());

		showCenter();
	}

	private void showCenter() {
		int w = (int) (bPane.getWidth() - rightBox.getWidth());
		int h = (int) (bPane.getHeight() - toolBar.getHeight());
		
		if(getModeFactory() != null) {
			getModeFactory().show(w, h);
		}
	}

	private Node getRightPane() {
		rightBox = new VBox();
		rightBox.setPadding(new Insets(3, 13, 3, 3));
		rightBox.setPrefWidth(300);
		rightBox.setMinWidth(300);
		return rightBox;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {

		for (SmthChangeListener lst : AppContext.smthListener) {

			try {
				lst.somethingChanged(changed);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		if(changed.isFileopened()) {

			gpsMode.setSelected(true);
			setModeFactory(modeMap.get(gpsMode));			
		}
		
	}

}
