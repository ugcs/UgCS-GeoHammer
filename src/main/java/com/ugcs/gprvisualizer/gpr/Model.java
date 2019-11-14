package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.FileManager;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.LocalScan;

public class Model {

	private Field field = new Field();
	private FileManager fileManager = new FileManager();
	
	private Settings settings = new Settings();
	
	private List<Trace> foundTrace = new ArrayList<>();
	private Map<SgyFile, List<Integer>> foundIndexes = new HashMap<>();
	
	private Rectangle2D.Double bounds;
	
	public Settings getSettings() {
		return settings;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
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

	public List<Trace> getFoundTrace() {
		return foundTrace;
	}

	public void setFoundTrace(List<Trace> foundTrace) {
		this.foundTrace = foundTrace;
	}

	public Map<SgyFile, List<Integer>> getFoundIndexes() {
		return foundIndexes;
	}
}
