package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.cursor.CursorType;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.Loader;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class TraceCutter implements Layer, SmthChangeListener {

	private MapField field;
	private List<LatLon> points;
	private final int RADIUS = 5;
	private Integer active = null;
	private Model model;
	private RepaintListener listener;
	
	private ToggleButton buttonCutMode = new ToggleButton("Select", ResourceImageHolder.getImageView("select_rect20.png"));
	private Button buttonSet = new Button("Crop", ResourceImageHolder.getImageView("scisors3-20.png"));	
	private Button buttonUndo = new Button("Undo Crop", ResourceImageHolder.getImageView("clear20.png"));
	
	
	public TraceCutter(Model model, RepaintListener listener) {
		this.model = model; 
		this.listener = listener;
		this.field = model.getField();
		
		AppContext.smthListener.add(this);
	}
	
	public void clear() {
		points = null;
		active = null;
	}
	
	public void init() {
		
		points = new ArrayList<>();
		points.add(field.screenTolatLon(new Point(100, 100)));
		points.add(field.screenTolatLon(new Point(100,-100)));
		points.add(field.screenTolatLon(new Point(-100,-100)));
		points.add(field.screenTolatLon(new Point(-100, 100)));
		
	}
	
	public boolean mousePressed(Point2D point){
		if(points == null) {
			return false;
		}
		
		List<Point2D> border = getScreenPoligon(field);
		for(int i=0; i<border.size(); i++) {
			Point2D p = border.get(i);
			if(point.distance(p) < RADIUS) {
				active = i;
				listener.repaint();
				return true;
			}			
		}
		active = null;
		return false;
	}
	
	public boolean mouseRelease(Point2D point) {
		if(points == null) {
			return false;
		}
		
		if(active != null) {
			listener.repaint();
			active = null;
		
			return true;
		}
		return false;
	}
	
	public boolean mouseMove(Point2D point) {
		if(points == null) {
			return false;
		}
		
		if(active == null) {
			return false;
		}
		
		points.get(active).from(field.screenTolatLon(point));
		listener.repaint();		
		return true;
	}
	
	public void draw(Graphics2D g2) {
		if(points == null) {
			return ;
		}
		
		List<Point2D> border = getScreenPoligon(field);
		
		for(int i=0; i<border.size(); i++) {
			
			Point2D p1 = border.get(i);
			Point2D p2 = border.get((i+1) % border.size());
			
			g2.setColor(Color.YELLOW);
			g2.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
		}
		
		for(int i=0; i<border.size(); i++) {
			Point2D p1 = border.get(i);			
			
			g2.setColor(Color.RED);
			g2.fillOval((int)p1.getX() - RADIUS, (int)p1.getY() - RADIUS, 2*RADIUS, 2*RADIUS);
			if(active != null && active == i) {
				g2.setColor(Color.BLUE);
				g2.drawOval((int)p1.getX() - RADIUS, (int)p1.getY() - RADIUS, 2*RADIUS, 2*RADIUS);
			}			
		}		
	}
	
	public void apply() {
		
		model.setControls(null);
		
		MapField fld = new MapField(field);
		fld.setZoom(28);
		List<Point2D> border = getScreenPoligon(fld);
		
		////
		List<SgyFile> slicedSgyFiles = new ArrayList<>();
		for(SgyFile file : model.getFileManager().getFiles()) {
			slicedSgyFiles.addAll(splitFile(file, fld, border));
		}
		
		//
		model.setUndoFiles(model.getFileManager().getFiles());
		
		model.getFileManager().setFiles(slicedSgyFiles);
		
		//to update index inset
		model.getFileManager().clearTraces();
		
		model.init();
		
		//model.initField();
		model.getVField().clear();
	}

	public void undo() {
		model.setControls(null);

		if(model.getUndoFiles() != null) {
			model.getFileManager().setFiles(model.getUndoFiles());
			model.setUndoFiles(null);
			
			for(SgyFile sf : model.getFileManager().getFiles()) {
				sf.updateInternalIndexes();
			}

			model.getFileManager().clearTraces();
			
			model.init();
			
			//model.initField();
			model.getVField().clear();
		}
		
	}

	
	public boolean isTraceInsideSelection(MapField fld, List<Point2D> border, Trace trace) {
		Point2D p = fld.latLonToScreen(trace.getLatLon());
		boolean ins = inside(p, border);
		return ins;
	}
	
	private SgyFile generateSgyFileFrom(SgyFile file, List<Trace> traces, int part) {
		
		SgyFile sgyFile = new SgyFile();
		
		sgyFile.setTraces(traces);
		sgyFile.setFile(getPartFile(file, part, file.getFile().getParentFile()));
		
		sgyFile.setBinHdr(file.getBinHdr());
		sgyFile.setTxtHdr(file.getTxtHdr());
		
		int begin = traces.get(0).indexInFile;
		int end = traces.get(traces.size()-1).indexInFile;
		sgyFile.setAuxElements(copyAuxObjects(file, sgyFile, begin, end));
		
		
		sgyFile.updateInternalIndexes();
		
		System.out.println("generate sgy file " + traces.size()  + "     '" + file.getFile().getName()+"'" );
		
		return sgyFile;
	}

	private File getPartFile(SgyFile file, int part, File nfolder) {
		File nfile;
		String name = file.getFile().getName();
		int pos = name.lastIndexOf(".");
		String onlyname = name.substring(0, pos);
		String spart = String.format("%03d", part);
		nfile = new File(nfolder, onlyname + "_" + spart + name.substring(pos));
		return nfile;
	}

	
	private List<SgyFile> splitFile(SgyFile file, MapField field, List<Point2D> border) {
		
		
		List<SgyFile> splitList = new ArrayList<>();
		List<Trace> sublist = new ArrayList<>();
		
		int part=1; 
		
		for(Trace trace : file.getTraces()) {
			boolean inside = isTraceInsideSelection(field, border, trace);
			
			if(inside) {
				sublist.add(trace);
			}else{
				if(!sublist.isEmpty()){
				//end of active block of traces
					
					if(isGoodForFile(sublist)){ //filter too small files
						SgyFile subfile = generateSgyFileFrom(file, sublist, part++);
						splitList.add(subfile);
					}
					sublist = new ArrayList<>();
				}
			}
		}
		//for last
		if(isGoodForFile(sublist)){					
			SgyFile subfile = generateSgyFileFrom(file, sublist, part++);
			splitList.add(subfile);
		}
		
		if(splitList.size() == 1) {
			splitList.get(0).setFile(file.getFile());
		}
		
		return splitList;
	}

	public List<BaseObject> copyAuxObjects(SgyFile file, SgyFile sgyFile, int begin, int end) {
		List<BaseObject> auxObjects = new ArrayList<>();				
		for(BaseObject au : file.getAuxElements()) {
			if(au.isFit(begin, end)) {
				auxObjects.add(au.copy(begin, sgyFile.getOffset()));
			}
		}
		return auxObjects;
	}

	public boolean isGoodForFile(List<Trace> sublist) {
		return sublist.size() > 3;
	}
	
	private List<Point2D> getScreenPoligon(MapField fld) {

		List<Point2D> border = new ArrayList<>();
		for(LatLon ll : points) {
			border.add(fld.latLonToScreen(ll));
		}
		return border;
	}

	private boolean inside(Point2D p, List<Point2D> border) {
		
		boolean result = false;
		for(int i=0; i<border.size(); i++) {
			Point2D pt1 = border.get(i);
			Point2D pt2 = border.get((i+1) % border.size());
		
			if ((pt1.getY() > p.getY()) != (pt2.getY() > p.getY()) 
					&&
					(p.getX() < 
						(pt2.getX() - pt1.getX()) * 
						(p.getY() - pt1.getY()) / 
						(pt2.getY()-pt1.getY()) + pt1.getX())) {
		         result = !result;
		    }		
		}
		
		return result;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if(changed.isFileopened()) {
			clear();
			model.setUndoFiles(null);
			initButtons();
		}
	}

	@Override
	public List<Node> getToolNodes() {
		return Arrays.asList();
	}
	
	public List<Node> getToolNodes2() {
		
		initButtons();
		
		buttonCutMode.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        
		    	updateCutMode();
		    	
		    }
		});
		
		buttonSet.setOnAction(e -> {
	    	apply();
	    	
	    	buttonCutMode.setSelected(false);
	    	updateCutMode();
	    	buttonUndo.setDisable(false);

	    	
	    	AppContext.notifyAll(new WhatChanged(Change.traceCut));
	    	
		});
		
		
		buttonUndo.setOnAction(e -> {
	    	undo();

	    	buttonCutMode.setSelected(false);
	    	updateCutMode();
	    	buttonUndo.setDisable(true);

	    	AppContext.notifyAll(new WhatChanged(Change.traceCut));
	    	
		});
		
		return Arrays.asList(buttonCutMode, buttonSet, buttonUndo);
	}

	public void initButtons() {
		buttonCutMode.setSelected(false);
		buttonSet.setDisable(true);
		buttonUndo.setDisable(model.getUndoFiles() == null);
	}
	
	private void updateCutMode() {
		if(buttonCutMode.isSelected()) {
    		init();
    		buttonSet.setDisable(false);
    	}else{
    		clear();
    		buttonSet.setDisable(true);
    	}
		
		
		
    	listener.repaint();
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}
	
}

