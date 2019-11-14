package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class RadarMap implements Layer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;
	private LatLon imgLatLon;
	
	private int width = 800;
	private int height = 800;
	
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
		
		//System.out.println(" draw radar" + (_img != null ? "+" : "-"));
		
		if(_img == null) {
			return;
		}
		
		//int width = model.getSettings().width; 
		//int height = model.getSettings().height;
		
		Point2D offst = model.getField().latLonToScreen(imgLatLon);
		
		g2.drawImage(_img, 
			(int)offst.getX() -_img.getWidth()/2, 
			(int)offst.getY() -_img.getHeight()/2, 
			null);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if(changed.isFileopened() || changed.isZoom() || changed.isAdjusting()) {
			System.out.println(" radar start thread");
			executor.submit(t);
		}		
	}
	
	private BufferedImage createHiRes() {
		
		
		return createLowRes();
	}

	int r = 5;
	private BufferedImage createLowRes() {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		
		Graphics2D g2 = (Graphics2D)img.getGraphics();
		g2.translate(width/2, height/2);
		g2.setColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
		for(SgyFile sf : model.getFileManager().getFiles()) {
			
			
			drawTrace(g2, sf.getTraces().get(0));
			
			drawTrace(g2, sf.getTraces().get(sf.getTraces().size()-1));
		}
		
		return img;
	}

	private void drawTrace(Graphics2D g2, Trace trace) {
		Point2D p = model.getField().latLonToScreen(trace.getLatLon());
		g2.fillOval((int)p.getX() - r, (int)p.getY()-r, 2*r, 2*r);
	}
	
	
	Thread t = new Thread() {
		public void run() {
			try {
			
				System.out.println(" thread 1");
				img = createLowRes();
				imgLatLon = model.getField().getSceneCenter();
				
				System.out.println(" thread 2");
				
				listener.repaint();
				
				System.out.println(" thread 3");
				
				if(executor.getQueue().size() > 0) {
					return;
				}
				
				System.out.println(" thread 4");
				
				img = createHiRes();
				imgLatLon = model.getField().getSceneCenter();
				
				listener.repaint();
							
				System.out.println(" thread 5");
			}catch(Exception e) {
				e.printStackTrace();
			}
			
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
