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
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RepaintListener;
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

public class TraceCutter implements Layer{

	Field field;
	List<LatLon> points;
	final int RADIUS = 5;
	Integer active = null;
	Model model;
	RepaintListener listener;
	Button buttonSet = new Button("Apply");
	Button buttonMinus = new Button("Minus");
	Button buttonPlus = new Button("Plus");
	
	Button buttonSave = new Button("Save");
	
	Image imageFilter = new Image(getClass().getClassLoader().getResourceAsStream("filter.png"));
	ToggleButton buttonCutMode = new ToggleButton("Cut", new ImageView(imageFilter));
	
	
	public TraceCutter(Model model, RepaintListener listener) {
		this.model = model; 
		this.listener = listener;
		
		field = model.getField();
		
		
		
	}
	
	
	public void clear() {
		
		points = null;
		active = null;
	}
	
	public void init() {
		
		points = new ArrayList<>();
		points.add(field.screenTolatLon(new Point(200, 200)));
		points.add(field.screenTolatLon(new Point(200,-200)));
		points.add(field.screenTolatLon(new Point(-200,-200)));
		points.add(field.screenTolatLon(new Point(-200, 200)));
		
	}
	
	public boolean mousePressed(Point2D point){
		if(points == null) {
			return false;
		}
		
		
		
		List<Point2D> border = getScreenPoligon();
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
		
		List<Point2D> border = getScreenPoligon();
		
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
	
	public void apply(List<Trace> traces, boolean plus, boolean minus) {
		
		List<Point2D> border = getScreenPoligon();
		
		for(Trace trace : traces) {
			
			Point2D p = field.latLonToScreen(trace.getLatLon());
			
			
			boolean ins = inside(p, border);
			
			if(ins && !trace.isActive() && plus ||
			  !ins &&  trace.isActive() && minus){
				  trace.setActive(ins);
			}
		}	
		
	}

	private List<Point2D> getScreenPoligon() {

		List<Point2D> border = new ArrayList<>();
		for(LatLon ll : points) {
			border.add(field.latLonToScreen(ll));
		}
		return border;
	}

	private boolean inside(Point2D p, List<Point2D> border) {
		
		boolean result = false;
		for(int i=0; i<border.size(); i++) {
			Point2D pt1 = border.get(i);
			Point2D pt2 = border.get((i+1) % border.size());
		
			if ((pt1.getY() > p.getY()) != (pt2.getY() > p.getY()) &&
		           (p.getX() < (pt2.getX() - pt1.getX()) * (p.getY() - pt1.getY()) / (pt2.getY()-pt1.getY()) + pt1.getX())) {
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
			buttonCutMode.setDisable(false);
		}
	}

	@Override
	public List<Node> getToolNodes() {
		
		buttonCutMode.setDisable(true);
		buttonSet.setVisible(false);
		buttonSave.setVisible(false);
		
		buttonCutMode.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        
		    	if(buttonCutMode.isSelected()) {
		    		init();
		    		buttonSet.setVisible(true);
		    		buttonSave.setVisible(true);
		    	}else{
		    		clear();
		    		buttonSet.setVisible(false);
		    		buttonSave.setVisible(false);
		    	}
		    	listener.repaint();
		    }
		});

		
		buttonSet.setOnAction(e -> {
		    	apply(model.getFileManager().getTraces(), true, true);
		    	listener.repaint();
			});
		buttonPlus.setOnAction(e -> {
	    	apply(model.getFileManager().getTraces(), true, false);
	    	listener.repaint();
		});
		buttonMinus.setOnAction(e -> {
	    	apply(model.getFileManager().getTraces(), false, true);
	    	listener.repaint();
		});

		
		buttonSave.managedProperty().bind(buttonSave.visibleProperty());
		buttonSave.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	//apply(model.getFileManager().getTraces());
		    	//listener.repaint();
		    	buttonSave.setDisable(true);
		    	
		    	Cursor cursor = buttonSave.getCursor();
		    	buttonSave.setCursor(Cursor.WAIT);
		    	save();
		    	buttonSave.setDisable(false);
		    	buttonSave.setCursor(cursor);
		    }
		});
		//, new Label("LABEL")
		return Arrays.asList(buttonCutMode, /*buttonMinus, buttonPlus,*/ buttonSet,  buttonSave);
	}
	

	private void save() {
		for(SgyFile file : model.getFileManager().getFiles()) {
			int part = 1;
			List<Trace> sublist = new ArrayList<>();
			for(Trace trace : file.getTraces()) {
				
				if(trace.isActive()) {
					sublist.add(trace);
				}else {
					if(!sublist.isEmpty()){					
						savePart(file, part++, sublist);
						sublist.clear();
					}		
				}
			}
			//for last
			if(!sublist.isEmpty()){					
				savePart(file, part++, sublist);
				sublist.clear();
			}		
		}
		
	}

	private void savePart(SgyFile file, int part, List<Trace> sublist) {
		List<Block> blocks = getBlocks(sublist); 

		try {
			String name = file.getFile().getName();
			int pos = name.lastIndexOf(".");
			String onlyname = name.substring(0, pos);
			File nfolder = new File(file.getFile().getParentFile(), onlyname + "_processed");
			nfolder.mkdir();
			File nfile = new File(nfolder, onlyname + "_" + part + name.substring(pos));
			
			
			file.savePart(nfile.getAbsolutePath(), blocks);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Block> getBlocks(List<Trace> sublist) {
		
		List<Block> blocks = new ArrayList<>();
		for(Trace trace : sublist) {
			blocks.add(trace.getHeaderBlock());
			blocks.add(trace.getDataBlock());
		}
		
		return blocks;
	}
	
}
