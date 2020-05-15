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
import com.ugcs.gprvisualizer.math.HoughScan;
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class MainGeoHammer extends Application {

	private static final String TITLE_VERSION = "UgCS GeoHammer v.1.0.1";
	private static final int RIGHT_BOX_WIDTH = 330;
	
	private Scene scene;
	private BorderPane bPane;
	private ModeFactory modeFactory;
	
	private Model model;
	

	private RootControls rootControls;
	
	//view controls
	private TabPane tabPane;
	
	ApplicationContext context; 
	
	public MainGeoHammer() {
		
	}
	
	public static void main(String args[]) {
		launch(args);
	}
	
	@Override
    public void init() {
		 context = new ClassPathXmlApplicationContext("spring.xml");
		 
		 model = context.getBean(Model.class);
		 
		 
		 
		  
		 rootControls = context.getBean(RootControls.class);
		
		 //layersWindowBuilder = context.getBean(MapView.class);		 
//		 cleverImageView = context.getBean(ProfileView.class);
//		 loader = context.getBean(Loader.class);
//		 saver = context.getBean(Saver.class);
//		 statusBar = context.getBean(StatusBar.class);
		 
//		 broadcast = context.getBean(Broadcast.class);
		 
		 
//		 levelFilter = context.getBean(LevelFilter.class);
		 
		
    }	

	@Override
	public void start(Stage stage) throws Exception {

		AppContext.stage = stage;
		
		

		stage.getIcons().add(ResourceImageHolder.IMG_LOGO24);
		
		scene = createScene();

		stage.setTitle(TITLE_VERSION);
		stage.setScene(scene);
		//stage.setMaximized(true);
		stage.show();

//		model.getSettings().center_box_width = (int) (bPane.getWidth() - rightBox.getWidth()); 
//		model.getSettings().center_box_height = (int) (bPane.getHeight() - toolBar.getHeight());
		
		
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
		
		
		if(!getParameters().getRaw().isEmpty()) {
			String name = getParameters().getRaw().get(0);
			System.out.println("args " + name);
			
			List<File> f = new ArrayList<>();
			f.add(new File(name));
			rootControls.getLoader().loadWithNotify(f, new ProgressListener() {
				@Override
				public void progressPercent(int percent) {}
				
				@Override
				public void progressMsg(String msg) {}
			});
		}
	}

	private Scene createScene() {

		bPane = new BorderPane();

		bPane.setOnDragOver(rootControls.getLoader().getDragHandler());
		bPane.setOnDragDropped(rootControls.getLoader().getDropHandler());

		bPane.setTop(rootControls.getToolBar());
		bPane.setCenter(createSplitPane());
		
        ////
		bPane.setBottom(rootControls.getStatusBar());
		
		scene = new Scene(bPane, 1280, 768);
		
		return scene;
	}

	public SplitPane createSplitPane() {
		SplitPane sp = new SplitPane();
		sp.setDividerPositions(0.15f, 0.65f, 0.2f);
		
		//map view
		sp.getItems().add(rootControls.getLayersWindowBuilder().getCenter());
		
		//profile view
		sp.getItems().add(rootControls.getProfileView().getCenter());
		
		//options tabs
		sp.getItems().add(getRightPane());
		
		return sp;
	}

	private TabPane prepareTabPane() {
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
        t1.getChildren().addAll(rootControls.getLayersWindowBuilder().getRight());
        tab1.setContent(t1);
	}

	private ToggleButton prepareToggleButton(String title, String imageName, MutableBoolean bool, Change change) {
		ToggleButton btn = new ToggleButton(title, ResourceImageHolder.getImageView(imageName));
		
		btn.setSelected(bool.booleanValue());
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println(btn.isSelected());
				
				bool.setValue(btn.isSelected());
				
				rootControls.getBroadcast().notifyAll(new WhatChanged(change));
			}
		});
		
		return btn;
	}
	
	private ToggleButton hyperLiveViewBtn = new ToggleButton("Hyperbola detection mode", ResourceImageHolder.getImageView("hypLive.png"));
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			model.getSettings().hyperliveview = hyperLiveViewBtn.isSelected();
			rootControls.getBroadcast().notifyAll(new WhatChanged(Change.justdraw));

		}
	};
	
	private void prepareTab2(Tab tab2) {
		VBox t2 = new VBox(10);		
        t2.getChildren().addAll(rootControls.getProfileView().getRightSearch());
        
		hyperLiveViewBtn.setTooltip(new Tooltip("Hyperbola view mode"));
		hyperLiveViewBtn.setOnAction(showMapListener);

		Button buttonDecimator = rootControls.getCommandRegistry().createButton(new TraceStacking());		

		t2.getChildren().addAll(
				rootControls.getCommandRegistry().createAsinqTaskButton(new AlgorithmicScan()),				
				hyperLiveViewBtn, buttonDecimator, 
				new HBox(
						rootControls.getCommandRegistry().createButton(new EdgeFinder()),
						rootControls.getCommandRegistry().createButton(new EdgeSubtractGround())
						),
				new HBox(
						prepareToggleButton("show edge", null, model.getSettings().showEdge, Change.justdraw),
						prepareToggleButton("show good", null, model.getSettings().showGood, Change.justdraw)
						)
				
			);
        tab2.setContent(t2);
	}

	private Node getRightPane() {
		VBox rightBox = new VBox();
		
		rightBox.setPadding(new Insets(3, 13, 3, 3));
		rightBox.setPrefWidth(RIGHT_BOX_WIDTH);
		rightBox.setMinWidth(RIGHT_BOX_WIDTH);
		rightBox.setMaxWidth(RIGHT_BOX_WIDTH);
		
        tabPane = prepareTabPane();        
        rightBox.getChildren().addAll(tabPane);
        rightBox.getChildren().addAll(rootControls.getProfileView().getRight());

		
		return rightBox;
	}

}
