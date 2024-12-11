package com.ugcs.gprvisualizer.app;

import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
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
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private Model model;

	public ToggleButton prepareToggleButton(String title,
											String imageName,
											MutableBoolean bool,
											WhatChanged.Change change) {
		
		ToggleButton btn = new ToggleButton(title, 
				ResourceImageHolder.getImageView(imageName));
		
		btn.setSelected(bool.booleanValue());
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				bool.setValue(btn.isSelected());
				eventPublisher.publishEvent(new WhatChanged(this, change));
			}
		});
		
		return btn;
	}
	
	
	
	public Node createSlider(MutableInt val, WhatChanged.Change change, int min, int max, String name) {
		
		return createSlider(val, change, min, max, name,
			new ChangeListener<Number>() {
				@Override
				public void changed(
					ObservableValue<? extends Number> observable, 
					Number oldValue,
					Number newValue) {
					
					eventPublisher.publishEvent(new WhatChanged(this, change));
				}
		});
	};

	public Node createSlider(MutableInt val, WhatChanged.Change change, int min, int max, String name, ChangeListener<Number> listener) {
		return SliderFactory.create(name, val, min, max, listener, 5);
	};
}
