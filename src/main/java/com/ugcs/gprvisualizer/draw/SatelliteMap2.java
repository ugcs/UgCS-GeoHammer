package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class SatelliteMap2 implements Layer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;

	private Random rand = new Random();
	private Color color = new Color(rand.nextInt(16777215));
	
	public SatelliteMap2(Model model,  RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		BufferedImage _img = img;
		if(_img != null) {
			
			g2.drawImage(_img, 0, 0, null);
		}
		
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		System.out.println("satell");
		if(changed.isFileopened() || changed.isZoom()) {
			
			System.out.println("start");
			
			Thread thread = new Calc();
			thread.start();
			
		}
		
	}
	
	
	class Calc extends Thread {
		public void run() {
			System.out.println("satel run");
			BufferedImage bi = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB); 
			
			Graphics2D g2 = (Graphics2D)bi.getGraphics();
			
			g2.setColor(color);
			for(int i=0; i< 20; i++) {
				g2.fillOval(rand.nextInt(200), rand.nextInt(200), 19, 22);
			}

			try {
				Thread.sleep(100 + rand.nextInt(2000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			img = bi;
			listener.repaint();
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
