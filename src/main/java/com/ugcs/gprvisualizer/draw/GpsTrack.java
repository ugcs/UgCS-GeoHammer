package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
public class GpsTrack extends BaseLayer implements InitializingBean {

	//private RepaintListener listener;
	
	@Autowired
	private Model model;
	
	@Autowired
	private Dimension wndSize;

	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			setActive(showLayerCheckbox.isSelected());
			
			getRepaintListener().repaint();				
		}
	};
	
	private ToggleButton showLayerCheckbox = 
			new ToggleButton("", ResourceImageHolder.getImageView("path_20.png"));
	
	{
		showLayerCheckbox.setTooltip(new Tooltip("Toggle GPS track layer"));
		showLayerCheckbox.setSelected(true);
		showLayerCheckbox.setOnAction(showMapListener);
	}
	
	ThrQueue q;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		q = new ThrQueue(model) {
			protected void draw(BufferedImage backImg, MapField field) {
				
				Graphics2D g2 = (Graphics2D) backImg.getGraphics();
				
				g2.translate(backImg.getWidth() / 2, backImg.getHeight() / 2);
				
				drawTrack(g2, field);
			}
			
			public void ready() {
				getRepaintListener().repaint();
			}			
			
		};
		
		q.setWindowSize(wndSize);
	}
		
	@Override
	public void draw(Graphics2D g2, MapField currentField) {
		if (currentField.getSceneCenter() == null || !isActive()) {
			return;
		}
		

		q.drawImgOnChangedField(g2, currentField, q.getFront());
	
	}

//	public void drawToScr(Graphics2D g2, MapField field, BufferedImage tmpImg) {
//		
//		if (tmpImg == null) {
//			return;
//		}
//		
//		Point2D offst = field.latLonToScreen(imgLatLon);
//		
//		g2.drawImage(tmpImg, 
//			(int) offst.getX() - tmpImg.getWidth() / 2, 
//			(int) offst.getY() - tmpImg.getHeight() / 2, 
//			null);
//	}

//	public void updateImg(MapField currentField) {
//		img = new BufferedImage(
//				(int) getDimension().getWidth(), 
//				(int) getDimension().getHeight(),
//				BufferedImage.TYPE_INT_ARGB);
//		
//		imgLatLon = currentField.getSceneCenter();
//		
//		
//		Graphics2D g2 = (Graphics2D) img.getGraphics();
//		g2.translate(img.getWidth() / 2,  img.getHeight() / 2);
//		drawTrack(g2, currentField);
//		
//		//Sout.p("GpsTrack cr img");
//		
//	}
	
	public void drawTrack(Graphics2D g2, MapField field) {
		
		
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
			
			q.add();
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
