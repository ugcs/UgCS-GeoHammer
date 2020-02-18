package com.ugcs.gprvisualizer.app;


import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class StatusBar extends GridPane {
	private Model model;
	
	private Label first = new Label();
	private TextField textField = new TextField();
	{
		textField.setEditable(false);
		textField.setStyle("-fx-focus-color: transparent;");
	}
	
	public StatusBar(Model model) {
		this.model = model;
		
	
	
		
		this.getColumnConstraints().add(new ColumnConstraints(140));
        ColumnConstraints column2 = new ColumnConstraints(150,150,Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column2);
         
        ColumnConstraints column3 = new ColumnConstraints(70,70,Double.MAX_VALUE);
        column3.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column3);
         
        //statusBar.setGridLinesVisible(true);
        //statusBar.setColumnIndex(first, 0);
        //statusBar.setColumnIndex(second, 1);
        //statusBar.setColumnIndex(third, 2);
		
		this.add(first, 0, 0);
		this.add(textField, 1, 0);
		this.add(new Label(""), 2, 0);
		
	}
	
	public void showProgressText(String txt) {
		System.out.println("status text: '" + txt + "'");
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				textField.setText(txt);
			}
		});
		
			
	}
	
	public void showGPSPoint(LatLon click) {
		
		showProgressText(click.toString());
		
	}

}
