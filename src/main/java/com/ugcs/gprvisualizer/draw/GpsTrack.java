package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.Settings;

public class GpsTrack implements Layer{

	private RepaintListener listener;
	private Model model;
	
	public GpsTrack(Model model, RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		
		
		int width = model.getSettings().width; 
		int height = model.getSettings().height;
		//g2.drawImage(_img, 0, 0, width, height, null);
		
		
	}

	private void drawGPSPath(Graphics2D g2) {
		g2.setStroke(new BasicStroke(1.1f));

		g2.setColor(Color.GRAY);

		Integer x = null;
		Integer y = null;
		for (LocalScan scan : model.getLocalScans()) {

			if (x != null && scan.isBeginOfTrack()) {

				g2.drawLine(x, y, scan.getLocalX(), scan.getLocalY());
			}

			x = scan.getLocalX();
			y = scan.getLocalY();
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
	
}
