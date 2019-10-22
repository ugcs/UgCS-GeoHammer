package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

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
		points.add(field.screenTolatLon(new Point(300,300)));
		points.add(field.screenTolatLon(new Point(300,-300)));
		points.add(field.screenTolatLon(new Point(-300,-300)));
		points.add(field.screenTolatLon(new Point(-300,300)));
		
	}
	
	public boolean mousePressed(MouseEvent event){
		if(points == null) {
			return false;
		}
		
		Point pressPoint = new Point((int)event.getSceneX(), (int)event.getSceneY());
		
		List<Point> border = getScreenPoligon();
		for(int i=0; i<border.size(); i++) {
			Point p = border.get(i);
			if(pressPoint.distance(p) < RADIUS) {
				active = i;
				listener.repaint();
				return true;
			}			
		}
		active = null;
		return false;
	}
	
	public boolean mouseRelease(MouseEvent event) {
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
	
	public boolean mouseMove(MouseEvent event) {
		if(points == null) {
			return false;
		}
		
		if(active == null) {
			return false;
		}
		Point pressPoint = new Point((int)event.getSceneX(), (int)event.getSceneY());
		points.get(active).from(field.screenTolatLon(pressPoint));
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
	
	
}
