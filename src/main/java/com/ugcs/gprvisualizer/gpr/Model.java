package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.FileManager;
import com.ugcs.gprvisualizer.draw.LocalScan;

public class Model {

	private Field field = new Field();
	private FileManager fileManager = new FileManager();
	
	private Settings settings = new Settings();
	private List<Scan> scans;
	private List<LocalScan> localScans;
	
	private Rectangle2D.Double bounds;
	
	public Settings getSettings() {
		return settings;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public List<Scan> getScans() {
		return scans;
	}
	
	public void setScans(List<Scan> scans) {
		this.scans = scans;
	}

	public List<LocalScan> getLocalScans() {
		return localScans;
	}
	
	public void setLocalScans(List<LocalScan> scans) {
		this.localScans = scans;
	}
	
	public void setBounds(Rectangle2D.Double bounds) {
		this.bounds = bounds;		
	}
	
	public Rectangle2D.Double getBounds(){
		return bounds;
	}

	public Field getField() {
		return field;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}
