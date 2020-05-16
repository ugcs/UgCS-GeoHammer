package com.ugcs.gprvisualizer.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.math.LevelFilter;

@Component
public class RootControls {

	@Autowired
	private MapView mapView;
	
	@Autowired
	private RadarMap radarMap;

	@Autowired
	private ProfileView profileView;
	
	@Autowired
	private Loader loader;
	
	@Autowired
	private Saver saver;
	
	@Autowired
	private LevelFilter levelFilter;
	
	@Autowired
	private StatusBar statusBar;
	
	@Autowired
	private GeoHammerToolbar toolBar;
	
	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	@Autowired
	private SceneContent sceneContent;
	
	@Autowired
	private OptionPane optionPane;
	
	
	public MapView getMapView() {
		return mapView;
	}

	public RadarMap getRadarMap() {
		return radarMap;
	}

	public ProfileView getProfileView() {
		return profileView;
	}

	public Loader getLoader() {
		return loader;
	}

	public Saver getSaver() {
		return saver;
	}

	public LevelFilter getLevelFilter() {
		return levelFilter;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	public GeoHammerToolbar getToolBar() {
		return toolBar;
	}

	public Broadcast getBroadcast() {
		return broadcast;
	}

	public CommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	public OptionPane getOptionPane() {
		return optionPane;
	}

	public SceneContent getSceneContent() {
		return sceneContent;
	}

}
