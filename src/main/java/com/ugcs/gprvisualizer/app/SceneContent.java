package com.ugcs.gprvisualizer.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

@Component
public class SceneContent extends BorderPane implements InitializingBean {

	@Autowired
	private Loader loader;

	@Autowired
	private StatusBar statusBar;
	
//	@Autowired
//	private GeoHammerToolbar toolBar;

	@Autowired
	private MapView mapView;

	@Autowired
	private ProfileView profileView;
	
	@Autowired
	private OptionPane optionPane;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.setOnDragOver(loader.getDragHandler());
		this.setOnDragDropped(loader.getDropHandler());
		//this.setTop(toolBar);
		this.setCenter(createSplitPane());
		this.setBottom(statusBar);
		
	}

	private SplitPane createSplitPane() {
		SplitPane sp = new SplitPane();
		sp.setDividerPositions(0.15f, 0.65f, 0.2f);
		
		//map view
		sp.getItems().add(mapView.getCenter());
		
		//profile view
		sp.getItems().add(profileView.getCenter());
		
		//options tabs
		sp.getItems().add(optionPane);
		
		return sp;
	}
	
}
