package com.ugcs.gprvisualizer.ui;

import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class BaseCheckBox {
	private Settings settings;
	private CheckBox checkBox;
	protected Label label;
	protected String name;
	protected ChangeListener<Boolean> listenerExt;
	
	public BaseCheckBox(Settings settings){
		this.settings = settings;
		name = "Autogain";
	}
	
	protected ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> source, Boolean oldValue, Boolean newValue) {
        	boolean val = updateModel();
        	//label.textProperty().setValue(name + ": " + String.valueOf(val) + " " + units);
        } 
    };
	
	public BaseCheckBox(Settings settings, ChangeListener<Boolean> listenerExt) {
		if(settings == null) {
			throw new RuntimeException("settings == null");
		}
		this.settings = settings;
		this.listenerExt = listenerExt;
	}
	
	public Node produce() {
		
		
		 
		checkBox = new CheckBox();
        
        updateUI();
        
        checkBox.selectedProperty().addListener(listener);
        checkBox.selectedProperty().addListener(listenerExt);
        checkBox.setText(name);        
        
        HBox root = new HBox();
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(5));
        root.setSpacing(5);        
        root.getChildren().addAll(new Text("autogain"), checkBox);
        
        return root;
	}
	
	public void updateUI() {
		checkBox.setSelected(settings.autogain);
	}
	
	public boolean updateModel() {
		settings.autogain = checkBox.isSelected();
		return settings.autogain;
	}
	
}
