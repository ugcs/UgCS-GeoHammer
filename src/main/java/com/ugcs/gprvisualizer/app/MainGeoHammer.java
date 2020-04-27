package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScanFull;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;
import com.ugcs.gprvisualizer.math.TraceStacking;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainGeoHammer extends Application implements SmthChangeListener {

	private static final String TITLE_VERSION = "UgCS GeoHammer v.1.0.1";
	private static final int RIGHT_BOX_WIDTH = 330;
	private Scene scene;
	private BorderPane bPane;
	private ModeFactory modeFactory;
	private VBox rightBox = new VBox();
	private Model model = new Model();
	private ToolBar toolBar = new ToolBar();

	
	SplitPane sp;
	
	MapView layersWindowBuilder;
	
	TabPane tabPane;
	
	public MainGeoHammer() {
		
		AppContext.model = model;
		AppContext.levelFilter = new LevelFilter(model);
		AppContext.loader = new Loader(model);
		
		AppContext.pluginRunner = new PluginRunner(model);		
		AppContext.navigator = new Navigator(model);
		
		AppContext.statusBar = new StatusBar(model);
		
		layersWindowBuilder = new MapView(model);
		AppContext.smthListener.add(layersWindowBuilder);

		AppContext.cleverImageView = new ProfileView(model);
		
		AppContext.smthListener.add(this);
	}
	
	public static void main(String args[]) {
		launch(args);
		
		
	}

	@Override
	public void start(Stage stage) throws Exception {

		AppContext.stage = stage;
		AppContext.saver = new Saver(model, stage);

		stage.getIcons().add(ResourceImageHolder.IMG_LOGO24);
		
		scene = createScene();

		stage.setTitle(TITLE_VERSION);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();

		model.getSettings().center_box_width = (int) (bPane.getWidth() - rightBox.getWidth()); 
		model.getSettings().center_box_height = (int) (bPane.getHeight() - toolBar.getHeight());
		
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		    	
	        	if(model.stopUnsaved()) {
	        		t.consume();
	        		return;
	        	}        	
		    	
		        Platform.exit();
		        System.exit(0);
		    }
		});		
		
		
		if(getParameters().getRaw().size() > 0) {
			String name = getParameters().getRaw().get(0);
			System.out.println("args " + name);
			
			List<File> f = new ArrayList<>();
			f.add(new File(name));
			AppContext.loader.loadWithNotify(f, new ProgressListener() {
				
				@Override
				public void progressPercent(int percent) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void progressMsg(String msg) {
					// TODO Auto-generated method stub
					
				}
			});
		}
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
		sp.setDividerPositions(0.15f, 0.65f, 0.2f);
		
		
		sp.getItems().add(layersWindowBuilder.getCenter());
		
		sp.getItems().add(AppContext.cleverImageView.getCenter());
		
		sp.getItems().add(getRightPane());
		
		bPane.setCenter(sp);
		
		rightBox.getChildren().clear();
		
        tabPane = prepareTabPane();        
        
        rightBox.getChildren().addAll(tabPane);
        rightBox.getChildren().addAll( AppContext.cleverImageView.getRight());

        ////
		bPane.setBottom(AppContext.statusBar);
		
		
		
		scene = new Scene(bPane, 1280, 768);
		
		
		
		return scene;
	}

	public TabPane prepareTabPane() {
		TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tab tab1 = new Tab("Gain", new Label("Show all planes available"));
        Tab tab2 = new Tab("Search"  , new Label("Show all cars available"));
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        
        prepareTab1(tab1);

        prepareTab2(tab2);
        
		return tabPane;
	}

	public void prepareTab1(Tab tab1) {
		VBox t1 = new VBox();
        t1.getChildren().addAll(layersWindowBuilder.getRight());
        tab1.setContent(t1);
	}

	ToggleButton prepareToggleButton(String title, String imageName, MutableBoolean bool, Change change) {
		ToggleButton btn = new ToggleButton(title, ResourceImageHolder.getImageView(imageName));
		
		btn.setSelected(bool.booleanValue());
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println(btn.isSelected());
				
				bool.setValue(btn.isSelected());
				
				AppContext.notifyAll(new WhatChanged(change));
			}
		});
		
		return btn;
	}
	
	private ToggleButton hyperLiveViewBtn = new ToggleButton("Hyperbola detection mode", ResourceImageHolder.getImageView("hypLive.png"));
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			model.getSettings().hyperliveview = hyperLiveViewBtn.isSelected();
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			//repaintEvent();
		}
	};
	
	public void prepareTab2(Tab tab2) {
		VBox t2 = new VBox(10);		
        t2.getChildren().addAll(AppContext.cleverImageView.getRightSearch());
        
		hyperLiveViewBtn.setTooltip(new Tooltip("Hyperbola view mode"));
		hyperLiveViewBtn.setOnAction(showMapListener);

		Button buttonDecimator = CommandRegistry.createButton(new TraceStacking());		

		t2.getChildren().addAll(
				CommandRegistry.createAsinqTaskButton(new AlgorithmicScan()),				
				hyperLiveViewBtn, buttonDecimator, 
				new HBox(
						CommandRegistry.createButton(new EdgeFinder()),
						CommandRegistry.createButton(new EdgeSubtractGround())
						),
				new HBox(
						prepareToggleButton("show edge", null, model.getSettings().showEdge, Change.justdraw),
						prepareToggleButton("show good", null, model.getSettings().showGood, Change.justdraw)
						)
				//,new HBox(
				//		CommandRegistry.createButton(new HorizontalGroupScan()),
				//		CommandRegistry.createButton(new HorizontalGroupFilter())
				//	)				
				
			);
        tab2.setContent(t2);
	}


	private Node getToolBar() {
		toolBar.setDisable(true);
		
		toolBar.getItems().addAll(AppContext.saver.getToolNodes());
		
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().addAll(AppContext.levelFilter.getToolNodes());
		
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().addAll(CommandRegistry.createAsinqTaskButton(
				new AlgorithmicScanFull(),
				e->{ 
					
					Sout.p("select1");
					//tabPane.getSelectionModel().select(1);
					layersWindowBuilder.radarMap.selectAlgMode();
					 
				}));
		
		toolBar.getItems().addAll(AppContext.pluginRunner.getToolNodes());
		
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
