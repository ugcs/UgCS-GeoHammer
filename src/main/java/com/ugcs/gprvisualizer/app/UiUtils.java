package com.ugcs.gprvisualizer.app;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.math.HoughDiscretizer;
import com.ugcs.gprvisualizer.ui.SliderFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

@Component
public class UiUtils {

	@Autowired
	private Broadcast broadcast; 
	
	
	
	public ToggleButton prepareToggleButton(String title, 
			String imageName, MutableBoolean bool, Change change) {
		
		ToggleButton btn = new ToggleButton(title, 
				ResourceImageHolder.getImageView(imageName));
		
		btn.setSelected(bool.booleanValue());
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				bool.setValue(btn.isSelected());
				
				broadcast.notifyAll(new WhatChanged(change));
			}
		});
		
		return btn;
	}
	
	
	
	public Node createSlider(MutableInt val, Change change, int min, int max, String name) {
		
		return createSlider(val, change, min, max, name,
			new ChangeListener<Number>() {
				@Override
				public void changed(
					ObservableValue<? extends Number> observable, 
					Number oldValue,
					Number newValue) {
					
					broadcast.notifyAll(new WhatChanged(change));
				}
		});
	};

	public Node createSlider(MutableInt val, Change change, int min, int max, String name, ChangeListener<Number> listener) {
		return SliderFactory.create(name, val, min, max, listener, 5);
	};
}
