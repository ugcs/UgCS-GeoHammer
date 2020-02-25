package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HyperFinder;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainSingleWindow extends Application implements SmthChangeListener {

	private static final String TITLE_VERSION = "UgCS GPR GeoHammer v.0.9.6";
	private static final int RIGHT_BOX_WIDTH = 330;
	private Scene scene;
	private Stage stage;
	private BorderPane bPane;
	private ModeFactory modeFactory;
	private VBox rightBox = new VBox();
	//private VBox centerBox = new VBox();
	private Model model = new Model();
	private ToolBar toolBar = new ToolBar();

	
	SplitPane sp;
	
	MapView layersWindowBuilder;
	ProfileView cleverImageView;
	
	public MainSingleWindow() {
		
		AppContext.model = model;
		AppContext.levelFilter = new LevelFilter(model);
		AppContext.loader = new Loader(model);
		
		AppContext.pluginRunner = new PluginRunner(model);		
		AppContext.navigator = new Navigator(model);
		
		AppContext.statusBar = new StatusBar(model);
		
		layersWindowBuilder = new MapView(model);
		AppContext.smthListener.add(layersWindowBuilder);

		cleverImageView = new ProfileView(model);
		
		AppContext.smthListener.add(this);
	}
	
	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		this.stage = stage;
		
		AppContext.saver = new Saver(model, stage);

		stage.getIcons().add(ResourceImageHolder.IMG_LOGO24);
		
		scene = createScene();

		stage.setTitle(TITLE_VERSION);
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
		
		
		sp.getItems().add(layersWindowBuilder.getCenter());
		
		sp.getItems().add(cleverImageView.getCenter());
		
		sp.getItems().add(getRightPane());
		
		bPane.setCenter(sp);
		
		rightBox.getChildren().clear();
		
        TabPane tabPane = new TabPane();

        Tab tab1 = new Tab("Gain", new Label("Show all planes available"));
        Tab tab2 = new Tab("Search"  , new Label("Show all cars available"));
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        
        VBox t1 = new VBox();
        t1.getChildren().addAll(layersWindowBuilder.getRight());
        t1.getChildren().addAll(cleverImageView.getRight());
        tab1.setContent(t1);

        VBox t2 = new VBox();
        t2.getChildren().addAll(cleverImageView.getRightSearch());
        tab2.setContent(t2);
        
        rightBox.getChildren().addAll(tabPane);
		//rightBox.getChildren().addAll();
		//rightBox.getChildren().addAll(cleverImageView.getRight());
		
		/////////
		
		////
		bPane.setBottom(AppContext.statusBar);
		
		
		
		scene = new Scene(bPane, 1280, 768);
		
		
		
		return scene;
	}


	private Node getToolBar() {
		toolBar.setDisable(true);
		
		toolBar.getItems().addAll(AppContext.saver.getToolNodes());
		
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
		
		//toolBar.getItems().add(getSpacer());
		//toolBar.getItems().addAll(AppContext.navigator.getToolNodes());
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
			toolBar.setDisable(!model.isActive());
			
			AppContext.levelFilter.clearForNewFile();
			//gpsMode.setSelected(true);
			//setModeFactory(modeMap.get(gpsMode));			
		}
		
	}

}
