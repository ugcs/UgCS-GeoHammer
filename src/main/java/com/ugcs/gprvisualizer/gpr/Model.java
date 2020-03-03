package com.ugcs.gprvisualizer.gpr;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.FileManager;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.DepthHeight;
import com.ugcs.gprvisualizer.app.auxcontrol.DepthStart;
import com.ugcs.gprvisualizer.draw.LocalScan;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

public class Model {

	public static final int TOP_MARGIN = 50;
	
	private boolean loading = false; 
	
	private MapField field = new MapField();
	private ProfileField vField = new ProfileField(this);
	
	private FileManager fileManager = new FileManager();
	private List<SgyFile> undoFiles = null;
	
	
	private Settings settings = new Settings();
	
	//private List<Trace> foundTrace = new ArrayList<>();
	//private Map<SgyFile, List<Integer>> foundIndexes = new HashMap<>();
	private Set<FileChangeType> changes = new HashSet<>();
	
	private List<BaseObject> auxElements = new ArrayList<>();
	private List<BaseObject> controls = null;
	
	private Rectangle2D.Double bounds;
	private int maxHeightInSamples = 0;
	
	public int getTracesCount() {
		return getFileManager().getTraces().size();
	}
	
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

	public MapField getField() {
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
		
		auxElements.add(new DepthStart(ShapeHolder.topSelection));
		auxElements.add(new DepthHeight(ShapeHolder.botSelection));
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
	
	public ProfileField getVField() {
		return vField;
	}

	public int getMaxHeightInSamples() {
		return maxHeightInSamples;
	}

	public void updateMaxHeightInSamples() {
		
		//set index of traces
		int maxHeight = 0;
		for(int i=0; i< this.getFileManager().getTraces().size(); i++ ) {
			Trace tr = this.getFileManager().getTraces().get(i);
			maxHeight = Math.max(maxHeight, tr.getNormValues().length);
		}
		
		this.maxHeightInSamples = maxHeight;
		getSettings().maxsamples = maxHeightInSamples;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public void updateSgyFileOffsets() {
		int startTraceNum = 0;
		for(SgyFile sgyFile : this.getFileManager().getFiles()) {
			
			sgyFile.getOffset().setStartTrace(startTraceNum);
			startTraceNum += sgyFile.getTraces().size();
			sgyFile.getOffset().setFinishTrace(startTraceNum);
			sgyFile.getOffset().setMaxSamples(maxHeightInSamples);
			//try {
			//	new MarkupFile().load(sgyFile, model);
			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
		}
	}

	public void initField() {
		// center
		MinMaxAvg lonMid = new MinMaxAvg();
		MinMaxAvg latMid = new MinMaxAvg();
		for (Trace trace : this.getFileManager().getTraces()) {
			if(trace == null || trace.getLatLon() == null) {
				System.out.println("null");
				continue;
			}
			
			latMid.put(trace.getLatLon().getLatDgr());
			lonMid.put(trace.getLatLon().getLonDgr());
		}
		
		  
		this.getField().setPathCenter(new LatLon(latMid.getMid(), lonMid.getMid()));

		this.getField().setSceneCenter(new LatLon(latMid.getMid(), lonMid.getMid()));
		
		this.getField().setZoom(18);
	}

	public void init() {
		
		
		this.updateMaxHeightInSamples();
		
		this.updateSgyFileOffsets();
		
		this.updateAuxElements();
		
		//this.initField();
		//this.getVField().clear();
	}

	public List<SgyFile> getUndoFiles() {
		return undoFiles;
	}

	public void setUndoFiles(List<SgyFile> undoFiles) {
		this.undoFiles = undoFiles;
	}

	public boolean isActive() {
		return getFileManager().isActive();
	}
	
}
