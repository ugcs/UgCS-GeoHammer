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
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.AvgShiftFilter;
import com.ugcs.gprvisualizer.math.HyperFinder;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainSingleWindow extends Application implements SmthChangeListener {

	private static final int RIGHT_BOX_WIDTH = 330;
	private Scene scene;
	private Stage stage;
	private BorderPane bPane;
	private ModeFactory modeFactory;
	private VBox rightBox = new VBox();
	//private VBox centerBox = new VBox();
	private Model model = new Model();
	private ToolBar toolBar = new ToolBar();

	// modes
//	private ToggleGroup group = new ToggleGroup();
//	private ToggleButton gpsMode = new ToggleButton("GPS", null);
//	private ToggleButton prismMode = new ToggleButton("Prism", null);
//	private ToggleButton cutMode = new ToggleButton("Waveform", null);
//	private ToggleButton matrixMode = new ToggleButton("Matrix", null);
//	
//	private ToggleButton cleverMode = new ToggleButton("Clever", null);
	
	SplitPane sp;
	
	//Map<Node, ModeFactory> modeMap = new HashMap<>();
	{
	}
	LayersWindowBuilder layersWindowBuilder;
	CleverImageView cleverImageView;
	
	public MainSingleWindow() {
		
		AppContext.model = model;
		AppContext.levelFilter = new LevelFilter(model);
		AppContext.loader = new Loader(model);
		AppContext.saver = new Saver(model);
		AppContext.pluginRunner = new PluginRunner(model);		
		AppContext.navigator = new Navigator(model);
		
		layersWindowBuilder = new LayersWindowBuilder(model);
		AppContext.smthListener.add(layersWindowBuilder);

		cleverImageView = new CleverImageView(model);
		
		AppContext.smthListener.add(this);
	}
	
	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		this.stage = stage;

		stage.getIcons().add(ResourceImageHolder.IMG_LOGO24);
		
		scene = createScene();

		stage.setTitle("UgCS GPR data visualizer");
		stage.setScene(scene);
		stage.show();

		model.getSettings().center_box_width = (int) (bPane.getWidth() - rightBox.getWidth()); 
		model.getSettings().center_box_height = (int) (bPane.getHeight() - toolBar.getHeight());
		
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		        Platform.exit();
		        System.exit(0);
		    }
		});		
	}

	private Scene createScene() {

		bPane = new BorderPane();

		bPane.setOnDragOver(AppContext.loader.getDragHandler());
		bPane.setOnDragDropped(AppContext.loader.getDropHandler());

		bPane.setTop(getToolBar());

		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			model.getSettings().center_box_width = (int) (bPane.getWidth() - RIGHT_BOX_WIDTH); 
			model.getSettings().center_box_height = (int) (bPane.getHeight() - toolBar.getHeight());
		};
		bPane.widthProperty().addListener(stageSizeListener);
		bPane.heightProperty().addListener(stageSizeListener);

		sp = new SplitPane();
		sp.setDividerPositions(0.2f, 0.6f, 0.2f);
		Pane sp1 = new Pane();
		
		ChangeListener<Number> sp1SizeListener = (observable, oldValue, newValue) -> {
			layersWindowBuilder.setSize((int) (sp1.getWidth()), (int) (sp1.getHeight()));
		};
		
		
		Node n1 = layersWindowBuilder.getCenter();
		sp1.getChildren().add(n1);
		sp1.widthProperty().addListener(sp1SizeListener);
		sp1.heightProperty().addListener(sp1SizeListener);
		sp.getItems().add(sp1);
		
		Pane sp2 = new Pane();
		ChangeListener<Number> sp2SizeListener = (observable, oldValue, newValue) -> {
			cleverImageView.setSize((int) (sp2.getWidth()), (int) (sp2.getHeight()));
		};
		sp2.widthProperty().addListener(sp2SizeListener);
		sp2.heightProperty().addListener(sp2SizeListener);
		sp2.getChildren().add(cleverImageView.getCenter());
		sp.getItems().add(sp2);
		
		sp.getItems().add(getRightPane());
		
		bPane.setCenter(sp);
		
		rightBox.getChildren().clear();
		rightBox.getChildren().addAll(layersWindowBuilder.getRight());
		rightBox.getChildren().addAll(cleverImageView.getRight());
		
		scene = new Scene(bPane, 1280, 768);
		
		
		
		return scene;
	}

	private Node getToolBar() {

		toolBar.getItems().addAll(AppContext.saver.getToolNodes());
		
		
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().addAll(AppContext.levelFilter.getToolNodes());
		
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().addAll(AppContext.pluginRunner.getToolNodes());
		
		///		
		Button buttonHyperFinder = new Button("Hyper math finder");
		toolBar.getItems().add(buttonHyperFinder);
		buttonHyperFinder.setOnAction(e -> {
			HyperFinder hf = new HyperFinder(model);
			hf.process();
		});
		
		toolBar.getItems().add(getSpacer());
		toolBar.getItems().addAll(AppContext.navigator.getToolNodes());
		///
		
		return toolBar;
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(10);
		return r3;
	}

	public ModeFactory getModeFactory() {
		return modeFactory;
	}

	public void setModeFactory(ModeFactory modeFactory) {
		this.modeFactory = modeFactory;

		
		//centerBox.getChildren().clear();
		//centerBox.getChildren().add(getModeFactory().getCenter());


		showCenter();
	}

	private void showCenter() {
		if(getModeFactory() != null) {
			getModeFactory().show();
		}
	}

	private Node getRightPane() {
		rightBox = new VBox();
		rightBox.setPadding(new Insets(3, 13, 3, 3));
		rightBox.setPrefWidth(RIGHT_BOX_WIDTH);
		rightBox.setMinWidth(RIGHT_BOX_WIDTH);
		rightBox.setMaxWidth(RIGHT_BOX_WIDTH);
		
		return rightBox;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {


		if(changed.isFileopened()) {

			//gpsMode.setSelected(true);
			//setModeFactory(modeMap.get(gpsMode));			
		}
		
	}

}
