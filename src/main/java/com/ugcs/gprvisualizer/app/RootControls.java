package com.ugcs.gprvisualizer.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.math.LevelFilter;

@Component
public class RootControls {

	@Autowired
	private MapView layersWindowBuilder;
	
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
	private GHToolbar toolBar;
	
	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	public MapView getLayersWindowBuilder() {
		return layersWindowBuilder;
	}

	public void setLayersWindowBuilder(MapView layersWindowBuilder) {
		this.layersWindowBuilder = layersWindowBuilder;
	}

	public RadarMap getRadarMap() {
		return radarMap;
	}

	public void setRadarMap(RadarMap radarMap) {
		this.radarMap = radarMap;
	}

	public ProfileView getProfileView() {
		return profileView;
	}

	public void setProfileView(ProfileView profileView) {
		this.profileView = profileView;
	}

	public Loader getLoader() {
		return loader;
	}

	public void setLoader(Loader loader) {
		this.loader = loader;
	}

	public Saver getSaver() {
		return saver;
	}

	public void setSaver(Saver saver) {
		this.saver = saver;
	}

	public LevelFilter getLevelFilter() {
		return levelFilter;
	}

	public void setLevelFilter(LevelFilter levelFilter) {
		this.levelFilter = levelFilter;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	public void setStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	public GHToolbar getToolBar() {
		return toolBar;
	}

	public void setToolBar(GHToolbar toolBar) {
		this.toolBar = toolBar;
	}

	public Broadcast getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(Broadcast broadcast) {
		this.broadcast = broadcast;
	}

	public CommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	public void setCommandRegistry(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
	}	
}
