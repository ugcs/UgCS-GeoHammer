package com.ugcs.gprvisualizer.app;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.TraceStacking;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class OptionPane extends VBox {
	private static final int RIGHT_BOX_WIDTH = 330;
	
	@Autowired
	private MapView mapView;
	
	@Autowired
	private Broadcast broadcast; 

	@Autowired
	private ProfileView profileView;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	@Autowired
	private Model model;

	public OptionPane() {
		
	}
	
	@PostConstruct
	public void postConstruct() {
		this.setPadding(new Insets(3, 13, 3, 3));
		this.setPrefWidth(RIGHT_BOX_WIDTH);
		this.setMinWidth(RIGHT_BOX_WIDTH);
		this.setMaxWidth(RIGHT_BOX_WIDTH);
		
        this.getChildren().addAll(prepareTabPane());
        this.getChildren().addAll(profileView.getRight());
	}
	
	private TabPane prepareTabPane() {
		TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tab tab1 = new Tab("Gain");
        Tab tab2 = new Tab("Search");
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        
        prepareTab1(tab1);

        prepareTab2(tab2);
        
		return tabPane;
	}

	private void prepareTab1(Tab tab1) {
		VBox t1 = new VBox();
        t1.getChildren().addAll(mapView.getRight());
        tab1.setContent(t1);
	}

	private ToggleButton prepareToggleButton(String title, String imageName, MutableBoolean bool, Change change) {
		ToggleButton btn = new ToggleButton(title, ResourceImageHolder.getImageView(imageName));
		
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
	
	private void prepareTab2(Tab tab2) {
		VBox t2 = new VBox(10);		
        t2.getChildren().addAll(profileView.getRightSearch());
        
		t2.getChildren().addAll(
				commandRegistry.createAsinqTaskButton(new AlgorithmicScan()),				
				prepareToggleButton("Hyperbola detection mode", "hypLive.png", model.getSettings().getHyperliveview(), Change.justdraw),				
				commandRegistry.createButton(new TraceStacking()), 
				new HBox(
						commandRegistry.createButton(new EdgeFinder()),
						commandRegistry.createButton(new EdgeSubtractGround())
						),
				new HBox(
						prepareToggleButton("show edge", null, model.getSettings().showEdge, Change.justdraw),
						prepareToggleButton("show good", null, model.getSettings().showGood, Change.justdraw)
						)
				
			);
        tab2.setContent(t2);
	}
}