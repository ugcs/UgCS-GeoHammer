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
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
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
import javafx.stage.WindowEvent;

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
	
	private ToggleButton cleverMode = new ToggleButton("Clever", null);
	
	Map<Node, ModeFactory> modeMap = new HashMap<>();
	{
	}

	public MainSingleWindow() {
		
		//Map<String, String> env = System.getenv();
		//System.out.println(env.get("ANT_HOME"));
		
		AppContext.levelFilter = new LevelFilter(model);
		AppContext.loader = new Loader(model);
		AppContext.saver = new Saver(model);
		AppContext.pluginRunner = new PluginRunner(model);		

		gpsMode.setToggleGroup(group);
		prismMode.setToggleGroup(group);
		cutMode.setToggleGroup(group);
		matrixMode.setToggleGroup(group);
		cleverMode.setToggleGroup(group);

		modeMap.put(cutMode, new VerticalCut(model));

		PrismModeFactory pmf = new PrismModeFactory(model);
		modeMap.put(prismMode, pmf);
		AppContext.smthListener.add(pmf);
		
		MatrixModeFactory tmf = new MatrixModeFactory(model);
		modeMap.put(matrixMode, tmf);
		AppContext.smthListener.add(tmf);

		LayersWindowBuilder layersWindowBuilder = new LayersWindowBuilder(model);
		modeMap.put(gpsMode, layersWindowBuilder);
		AppContext.smthListener.add(layersWindowBuilder);

		modeMap.put(cleverMode, new CleverImageView(model));
		
		group.selectedToggleProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable o) {
				if (group.getSelectedToggle() == null) {

				} else {
					setModeFactory(modeMap.get(group.getSelectedToggle()));
				}
			}
		});

		AppContext.smthListener.add(this);
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
		bPane.setRight(getRightPane());
		//bPane.setCenter(centerBox);

		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			
			model.getSettings().center_box_width = (int) (bPane.getWidth() - rightBox.getWidth()); 
			model.getSettings().center_box_height = (int) (bPane.getHeight() - toolBar.getHeight());

			AppContext.notifyAll(new WhatChanged(Change.windowresized));
			
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
		
		toolBar.getItems().addAll(gpsMode, prismMode, cutMode, matrixMode, cleverMode);
		
		Region r2 = new Region();
		r2.setPrefWidth(10);
		toolBar.getItems().add(r2);
		
		toolBar.getItems().addAll(AppContext.levelFilter.getToolNodes());
		
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
		if(getModeFactory() != null) {
			getModeFactory().show();
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


		if(changed.isFileopened()) {

			gpsMode.setSelected(true);
			setModeFactory(modeMap.get(gpsMode));			
		}
		
	}

}
