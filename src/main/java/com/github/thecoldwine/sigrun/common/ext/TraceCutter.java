package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
	
	public TraceCutter(Model model, RepaintListener listener) {
		this.model = model; 
		this.listener = listener;
		
		field = model.getField();
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
		
		
		
		List<Point> border = getScreenPoligon();
		for(int i=0; i<border.size(); i++) {
			Point p = border.get(i);
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
		
		List<Point> border = getScreenPoligon();
		
		for(int i=0; i<border.size(); i++) {
			
			Point p1 = border.get(i);
			Point p2 = border.get((i+1) % border.size());
			
			g2.setColor(Color.YELLOW);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		
		for(int i=0; i<border.size(); i++) {
			Point p1 = border.get(i);			
			
			g2.setColor(Color.RED);
			g2.fillOval(p1.x - RADIUS, p1.y - RADIUS, 2*RADIUS, 2*RADIUS);
			if(active != null && active == i) {
				g2.setColor(Color.BLUE);
				g2.drawOval(p1.x - RADIUS, p1.y - RADIUS, 2*RADIUS, 2*RADIUS);
			}			
		}		
	}
	
	public void apply(List<Trace> traces) {
		
		List<Point> border = getScreenPoligon();
		
		for(Trace trace : traces) {
			
			Point p = field.latLonToScreen(trace.getLatLon());
			
			trace.setActive(inside(p, border));
		}	
		
	}

	private List<Point> getScreenPoligon() {

		List<Point> border = new ArrayList<>();
		for(LatLon ll : points) {
			border.add(field.latLonToScreen(ll));
		}
		return border;
	}

	private boolean inside(Point p, List<Point> border) {
		
		boolean result = false;
		for(int i=0; i<border.size(); i++) {
			Point pt1 = border.get(i);
			Point pt2 = border.get((i+1) % border.size());
		
			if ((pt1.y > p.y) != (pt2.y > p.y) &&
		           (p.x < (pt2.x - pt1.x) * (p.y - pt1.y) / (pt2.y-pt1.y) + pt1.x)) {
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
				
	}

	@Override
	public List<Node> getToolNodes() {
		Image imageFilter = new Image(getClass().getClassLoader().getResourceAsStream("filter.png"));		
		Button button2 = new Button("Cut", new ImageView(imageFilter));
		
		button2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        
		    	init();
		    	listener.repaint();
		    }
		});
		
		return Arrays.asList(button2,  new Label("LABEL"));
	}
	
	
}
