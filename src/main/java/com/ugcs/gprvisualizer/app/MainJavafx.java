package com.ugcs.gprvisualizer.app;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainJavafx extends Application {
	//private Settings settings = new Settings();
	private Model model = new Model();
	
	//private SceneAmplitudeMap sceneAmplitudeMap = new SceneAmplitudeMap(model);
	private MapView layersWindowBuilder = new MapView(model);
	
	Scene scene1;
	Scene scene2;
	Stage primaryStage;
	@Override     
	public void start(Stage primaryStage) throws Exception {
		
		this.primaryStage = primaryStage;

		//scene1 = sceneAmplitudeMap.build();//new Scene(bPane, 1024, 768); 
		
		//scene1 = layersWindowBuilder.build();
		
		scene2 = new Scene(getScene2(), 1024, 768); 
		
		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			//System.out.println("Height: " + primaryStage.getHeight() + " Width: " + primaryStage.getWidth());
	        //bottom.setText(getStatusLineText());
		};

	    primaryStage.widthProperty().addListener(stageSizeListener);
	    primaryStage.heightProperty().addListener(stageSizeListener); 

		primaryStage.setTitle("UgCS GPR data visualizer");
		primaryStage.setScene(scene1); 
		primaryStage.show(); 		
	}

	private Parent getScene2() {
		BorderPane bPane = new BorderPane();   
		 
		bPane.setCenter(new Text("SCENE II")); 

		
		return bPane;
	}

	
	public static void main(String args[]){           
		launch(args);      
	} 
	
}
