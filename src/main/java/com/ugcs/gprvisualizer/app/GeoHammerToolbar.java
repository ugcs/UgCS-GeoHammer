package com.ugcs.gprvisualizer.app;

import javax.annotation.PostConstruct;

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

@Component
public class GeoHammerToolbar extends ToolBar implements SmthChangeListener {

	@Autowired
	private Model model; 
	
	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private Saver saver; 
	
    @Autowired
	private LevelFilter levelFilter;
	
	@Autowired
	private RadarMap radarMap;
	
	@Autowired
	private HoughScan houghScan;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	@PostConstruct
	public void postConstruct() {
		setDisable(true);
		
		getItems().addAll(saver.getToolNodes());
		
		getItems().add(getSpacer());
		
		getItems().addAll(levelFilter.getToolNodes());
		
		getItems().add(getSpacer());

		getItems().addAll(commandRegistry.createAsinqTaskButton(
					new AlgorithmicScanFull(),
					e -> { 
						radarMap.selectAlgMode();
					 }
				),
				commandRegistry.createAsinqTaskButton(
					houghScan, 
					e -> {
						radarMap.selectAlgMode();
					}
				),
				commandRegistry.createAsinqTaskButton(
						new PluginRunner(model),
						e -> {}
				)
		);
		
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
