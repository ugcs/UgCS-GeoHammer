package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class RadarMap implements Layer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	public RadarMap(Model model, RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		BufferedImage _img = img;
		
		if(_img == null) {
			return;
		}
		
		int width = model.getSettings().width; 
		int height = model.getSettings().height;
		g2.drawImage(_img, 0, 0, width, height, null);
		
		
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if(changed.isFileopened() || changed.isZoom() || changed.isAdjusting()) {
			executor.submit(t);
		}		
	}
	
	private BufferedImage createHiRes() {
		
		
		return null;
	}

	private BufferedImage createLowRes() {
		
		
		return null;
	}
	
	
	Thread t = new Thread() {
		public void run() {
			
			img = createLowRes();
			listener.repaint();
			
			if(executor.getQueue().size() > 0) {
				return;
			}
			
			
			img = createHiRes();
			listener.repaint();
						
			
		}

	};

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
