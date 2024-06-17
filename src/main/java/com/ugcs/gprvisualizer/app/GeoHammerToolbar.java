package com.ugcs.gprvisualizer.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.commands.AlgorithmicScanFull;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HoughScan;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.Region;

//@Component
//TODO: remove this class
public class GeoHammerToolbar extends ToolBar implements SmthChangeListener, InitializingBean {

	@Autowired
	private Model model; 
	
	@Autowired
	private Broadcast broadcast;

    @Autowired
	private LevelFilter levelFilter;
	
	@Autowired
	private RadarMap radarMap;
	
	@Autowired
	private HoughScan houghScan;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setDisable(true);
		
		//getItems().addAll(saver.getToolNodes());
		
		//getItems().add(getSpacer());
		
		//getItems().addAll(levelFilter.getToolNodes());
		
		//getItems().add(getSpacer());

		
	}
	
	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(10);
		return r3;
	}
	
	@Override
	public void somethingChanged(WhatChanged changed) {

		if (changed.isFileopened()) {
			setDisable(!model.isActive());
		}		
	}
	
}
