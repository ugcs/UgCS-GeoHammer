package com.ugcs.gprvisualizer.app;


import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.intf.Status;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

@Component
public class StatusBar extends GridPane implements Status {
	
	private Label first = new Label();
	
	private TextField textField = new TextField();
	
	{
		textField.setEditable(false);
		textField.setStyle("-fx-focus-color: transparent;");
	}
	
	public StatusBar() {
		this.getColumnConstraints().add(new ColumnConstraints(140));
        ColumnConstraints column2 = new ColumnConstraints(150, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column2);
         
        ColumnConstraints column3 = new ColumnConstraints(70, 70, Double.MAX_VALUE);
        column3.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column3);
         
		this.add(first, 0, 0);
		this.add(textField, 1, 0);
		this.add(new Label(""), 2, 0);
		
		AppContext.status = this;
	}
	
	public void showProgressText(String txt) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textField.setText(txt);
			}
		});
	}
}
