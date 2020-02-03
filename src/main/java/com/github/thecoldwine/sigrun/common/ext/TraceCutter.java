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
import com.ugcs.gprvisualizer.draw.Change;
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
import javafx.scene.layout.Region;

public class TraceCutter implements Layer {

	Field field;
	List<LatLon> points;
	final int RADIUS = 5;
	Integer active = null;
	Model model;
	RepaintListener listener;
	
	Button buttonSet = new Button("cut", ResourceImageHolder.getImageView("scisors3-20.png"));
	
	Button buttonClear = new Button("clear", ResourceImageHolder.getImageView("clear20.png"));
	
	Image imageFilter = new Image(getClass().getClassLoader().getResourceAsStream("select_rect20.png"));
	ToggleButton buttonCutMode = new ToggleButton("select", new ImageView(imageFilter));
	
	
	
	public TraceCutter(Model model, RepaintListener listener) {
		this.model = model; 
		this.listener = listener;
		this.field = model.getField();		
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
	
	public void apply(List<Trace> traces) {
		
		Field fld = new Field(field);
		fld.setZoom(22);
		
		List<Point2D> border = getScreenPoligon(fld);
		
		for(Trace trace : traces) {
			
			Point2D p = fld.latLonToScreen(trace.getLatLon());
			
			
			boolean ins = inside(p, border);
			
			trace.setActive(ins);

		}	
		
	}

	public void clear(List<Trace> traces) {
		
		for(Trace trace : traces) {
			trace.setActive(true);
		}	
		
	}

	private List<Point2D> getScreenPoligon(Field fld) {

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
		
//		if(changed.isFileopened()) {
//			buttonCutMode.setDisable(false);
//		}
	}

	@Override
	public List<Node> getToolNodes() {
		return Arrays.asList();
	}
	
	public List<Node> getToolNodes2() {
		
		//buttonCutMode.setDisable(true);
		buttonSet.setDisable(true);
		
		buttonCutMode.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        
		    	updateBtns();
		    	
		    }

		});

		
		buttonSet.setOnAction(e -> {
		    	apply(model.getFileManager().getTraces());
		    	
		    	buttonCutMode.setSelected(false);
		    	updateBtns();
		    	
		    	AppContext.notifyAll(new WhatChanged(Change.adjusting));
			});
		
		
		buttonClear.setOnAction(e -> {
	    	clear(model.getFileManager().getTraces());

	    	buttonCutMode.setSelected(false);
	    	updateBtns();

	    	AppContext.notifyAll(new WhatChanged(Change.adjusting));
	    	
		});
		
		//, new Label("LABEL")
		
		
		
		return Arrays.asList(buttonCutMode, buttonSet, buttonClear);
	}
	
	private void updateBtns() {
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
