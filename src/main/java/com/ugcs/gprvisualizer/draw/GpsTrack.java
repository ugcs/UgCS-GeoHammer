package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

public class GpsTrack extends BaseLayer {

	private RepaintListener listener;
	
	private Model model;
	
	
	private BufferedImage img;
	private LatLon imgLatLon;
	
	
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			setActive(showLayerCheckbox.isSelected());
			
			listener.repaint();				
		}
	};
	
	private ToggleButton showLayerCheckbox = 
			new ToggleButton("", ResourceImageHolder.getImageView("path_20.png"));
	
	{
		showLayerCheckbox.setTooltip(new Tooltip("Toggle GPS track layer"));
		showLayerCheckbox.setSelected(true);
		showLayerCheckbox.setOnAction(showMapListener);
	}
	
	
	
	public GpsTrack(Dimension parentDimension, Model model, RepaintListener listener) {
		
		super();
		this.model = model;
		this.listener = listener;	
		
		setDimension(parentDimension);
	}
	
	@Override
	public void draw(Graphics2D g2) {
		if (model.getField().getSceneCenter() == null || !isActive()) {
			return;
		}
		
		if (img == null) {
			updateImg();
		}
		
		drawToScr(g2, model.getField(), img);
		//MapField field = new MapField(model.getField());
		//draw(g2, field);
	}

	public void drawToScr(Graphics2D g2, MapField field, BufferedImage tmpImg) {
		
		if (tmpImg == null) {
			return;
		}
		
		Point2D offst = field.latLonToScreen(imgLatLon);
		
		g2.drawImage(tmpImg, 
			(int) offst.getX() - tmpImg.getWidth() / 2, 
			(int) offst.getY() - tmpImg.getHeight() / 2, 
			null);
	}

	public void updateImg() {
		img = new BufferedImage(
				(int) getDimension().getWidth(), 
				(int) getDimension().getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		
		imgLatLon = model.getField().getSceneCenter();
		
		
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.translate(img.getWidth() / 2,  img.getHeight() / 2);
		draw(g2, model.getField());
		
		//Sout.p("GpsTrack cr img");
		
	}
	
	public void draw(Graphics2D g2, MapField field) {
		
		
		g2.setStroke(new BasicStroke(1.0f));		
		
		
		double sumdist = 0;
		
		LatLon ll = field.screenTolatLon(
				new Point2D.Double(0, 5));
		
		// meter to cm
		double threshold =				
				field.getSceneCenter().getDistance(ll) * 100.0;
		
		//
		g2.setColor(Color.RED);
		
		for (SgyFile sgyFile : model.getFileManager().getFiles()) {
			Point2D prevPoint = null;
			List<Trace> traces = sgyFile.getTraces();
	
			for (int tr = 0; tr < traces.size(); tr++) {
				Trace trace =  traces.get(tr);
				
				if (prevPoint == null) {
					
					prevPoint = field.latLonToScreen(
							trace.getLatLon());
					sumdist = 0;
					
				} else {
					//prev point exists
					
					sumdist += trace.getPrevDist();
					
					if (sumdist >= threshold) {
						
						Point2D pointNext = field.latLonToScreen(trace.getLatLon());
								
						g2.drawLine((int) prevPoint.getX(),
								(int) prevPoint.getY(),
								(int) pointNext.getX(),
								(int) pointNext.getY());
						
						prevPoint = pointNext;
						sumdist = 0;
					}
				}
			}
		}
	}
	
	@Override
	public boolean isReady() {
		
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if (changed.isTraceCut() 
				|| changed.isTraceValues() 
				|| changed.isFileopened() 
				|| changed.isZoom() 
				|| changed.isAdjusting() 
				|| changed.isMapscroll() 
				|| changed.isWindowresized()) {
			
			img = null;
		}		

	}

	@Override
	public boolean mousePressed(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(showLayerCheckbox);
	}
	
}
