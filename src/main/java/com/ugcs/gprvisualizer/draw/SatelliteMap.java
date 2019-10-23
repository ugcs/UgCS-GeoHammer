package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.SceneAmplitudeMap;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.Settings;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class SatelliteMap implements Layer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;
	private LatLon imgLatLon;

	private Random rand = new Random();
	private Color color = new Color(rand.nextInt(16777215));

	private Point2D dragPoint = null;
	
	private LatLon click;
	
	public SatelliteMap(Model model,  RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		BufferedImage _img = img;
		if(_img != null) {
			
			Point offst = model.getField().latLonToScreen(imgLatLon);
			
			g2.drawImage(_img, 
				(int)offst.getX() -_img.getWidth()/2, 
				(int)offst.getY() -_img.getHeight()/2, 
				null);
		}		
		
		if(click != null) {
			Point p = model.getField().latLonToScreen(click);
			
			g2.drawOval(p.x-3, p.y-3, 7, 7);
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
	
	protected void loadimg() {
		
		BufferedImage img = null;
		
		StaticMap map = new StaticMap(640, 640, "");
		map.setMaptype(Maptype.hybrid);
		
		LatLon midlPoint = model.getField().getSceneCenter();
		map.setLocation(new Location(midlPoint.getLatDgr(), midlPoint.getLonDgr()), model.getField().getZoom()); //40.714, -73.998 
		
		map.setMaptype(Maptype.hybrid);
		
		
		try {
			
			String url = map.toString();
			
			//https://maps.googleapis.com/maps/api/staticmap?size=640x640&center=40.714%2C-73.998&zoom=16&maptype=hybrid&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M
			//https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=40.714%2c%20-73.998&zoom=20&size=1800x1800&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M
			//url = "https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=40.714%2c%20-73.998&zoom=20&size=1800x1800&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M";
			System.out.println(url);
			
			System.setProperty("java.net.useSystemProxies", "true");
			img = ImageIO.read(new URL(url));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//drawTrack(img);
		
		this.img = img;
		this.imgLatLon = midlPoint;
		
	}
	
	class Calc extends Thread {
		public void run() {
			System.out.println("satel run");
			
			loadimg();
			
			listener.repaint();
		}
	}

	@Override
	public boolean mousePressed(Point2D point) {
		
		dragPoint = point;
		
		
		click = model.getField().screenTolatLon(dragPoint);
		listener.repaint();
		System.out.println("sat map mousePressed");
		return true;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		
		dragPoint = null;
		
		System.out.println("sat map mouseRelease");
		
		return true;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		
		if(dragPoint == null) {
			return false;
		}

		Point2D p = new Point2D.Double(
			dragPoint.getX() - point.getX(), 
			dragPoint.getY() - point.getY());
		LatLon sceneCenter = model.getField().screenTolatLon(p);
		dragPoint = point;
		
		model.getField().setSceneCenter(sceneCenter);
		
		listener.repaint();
		
		return true;
	};

	@Override
	public List<Node> getToolNodes() {
		
		return Collections.EMPTY_LIST;
	}
	
}
