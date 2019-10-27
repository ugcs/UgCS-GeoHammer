package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class GpsTrack implements Layer{

	private RepaintListener listener;
	private Model model;
	
	public GpsTrack(Model model, RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		
		drawGPSPath(g2);
	}

	private void drawGPSPath(Graphics2D g2) {
		g2.setStroke(new BasicStroke(1.1f));		
		Point2D pPrev = null;
		
		double sumdist = 0;
		double threshold =
				model.getField().getSceneCenter().getDistance(
				model.getField().screenTolatLon(new Point2D.Double(0, 3)));// meter
		
		for (Trace trace : model.getFileManager().getTraces()) {

			sumdist += trace.getPrevDist();
			if(sumdist < threshold && !trace.isEnd()) {				
				continue;
			}
			sumdist = 0;
				
			Point2D p = model.getField().latLonToScreen(trace.getLatLon());
			if(trace.isActive()) {
				if (pPrev != null) {			
					g2.setColor(Color.RED);
					g2.drawLine((int)pPrev.getX(), (int)pPrev.getY(), (int)p.getX(), (int)p.getY());
				}
				
				if(trace.isEnd()) {
					pPrev = null;
				}else {
					pPrev = p;
				}
			}else{
				//dot
				g2.setColor(Color.GRAY);
				g2.drawLine((int)p.getX(), (int)p.getY(), (int)p.getX(), (int)p.getY());
				pPrev = null;
			}			
		}
	}
	
	@Override
	public boolean isReady() {
		
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if(changed.isFileopened() || changed.isZoom() || changed.isAdjusting()) {
			
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
		
		return Collections.EMPTY_LIST;
	}
	
}
