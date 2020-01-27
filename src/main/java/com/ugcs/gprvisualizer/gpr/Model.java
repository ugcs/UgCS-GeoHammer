package com.ugcs.gprvisualizer.gpr;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.FileManager;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.LocalScan;

public class Model {

	public static final int TOP_MARGIN = 50;
	
	private boolean loading = false; 
	
	private Field field = new Field();
	private VerticalCutField vField = new VerticalCutField(this, TOP_MARGIN);
	
	private FileManager fileManager = new FileManager();
	
	private Settings settings = new Settings();
	
	//private List<Trace> foundTrace = new ArrayList<>();
	//private Map<SgyFile, List<Integer>> foundIndexes = new HashMap<>();
	private Set<FileChangeType> changes = new HashSet<>();
	
	private List<BaseObject> auxElements = new ArrayList<>();
	private List<BaseObject> controls = null;
	
	private Rectangle2D.Double bounds;
	private int maxHeightInSamples = 0;
	
	
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

//	public List<Trace> getFoundTrace() {
//		return foundTrace;
//	}
//
//	public void setFoundTrace(List<Trace> foundTrace) {
//		this.foundTrace = foundTrace;
//	}
//
//	public Map<SgyFile, List<Integer>> getFoundIndexes() {
//		return foundIndexes;
//	}

	public Set<FileChangeType> getChanges() {
		return changes;
	}

	public List<BaseObject> getAuxElements() {
		return auxElements;
	}

	public List<BaseObject> getControls() {
		return controls;
	}

	public void setControls(List<BaseObject> controls) {
		this.controls = controls;
	}
	
	public void updateAuxElements() {
		auxElements.clear();
		for(SgyFile sf : getFileManager().getFiles()) {
			auxElements.addAll(sf.getAuxElements());
		}
	}
	
	public SgyFile getSgyFileByTrace(int i) {
		
		for(SgyFile fl : getFileManager().getFiles()) {
			if(i <= fl.getTraces().get(fl.getTraces().size()-1).indexInSet) {
				
				return fl;
			}		
		}
		
		return null;
	}

	public int getSgyFileIndexByTrace(int i) {
		
		for(int index=0; index<getFileManager().getFiles().size(); index++) {
			SgyFile fl =  getFileManager().getFiles().get(index);
			
			if(i <= fl.getTraces().get(fl.getTraces().size()-1).indexInSet) {
				
				return index;
			}		
		}
		
		return 0;
	}
	
	public VerticalCutField getVField() {
		return vField;
	}

	public int getMaxHeightInSamples() {
		return maxHeightInSamples;
	}

	public void setMaxHeightInSamples(int maxHeightInSamples) {
		this.maxHeightInSamples = maxHeightInSamples;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
}
