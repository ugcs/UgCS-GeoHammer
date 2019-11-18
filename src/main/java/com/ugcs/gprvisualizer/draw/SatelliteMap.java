package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.ui.BaseCheckBox;
import com.ugcs.gprvisualizer.ui.LayerVisibilityCheckbox;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class SatelliteMap extends BaseLayer {

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;
	private LatLon imgLatLon;

	private Random rand = new Random();
	private Color color = new Color(rand.nextInt(16777215));
	
	private ChangeListener<Boolean> showLayerListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			
			setActive(newValue);
			
			
			if(isActive()) {
				loadMap();
			}else {
				listener.repaint();
			}
		}
	};
	
	private BaseCheckBox showLayerCheckbox = new LayerVisibilityCheckbox("show satellite map", showLayerListener);
	
	private static String GOOGLE_API_KEY;
	static {
		InputStream inputStream = SatelliteMap.class.getClassLoader().getResourceAsStream("googleapikey");
		//StringWriter writer = new StringWriter();
		//IOUtils.copy(inputStream, writer, "UTF-8");
		//GOOGLE_API_KEY = writer.toString();
		java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
		GOOGLE_API_KEY = s.hasNext() ? s.next() : "";		
	}
	
	private Point2D dragPoint = null;
	
	private LatLon click;
	
	public SatelliteMap(Model model,  RepaintListener listener) {
		this.listener = listener;
		this.model = model;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		BufferedImage _img = img;
		if(_img != null && isActive()) {
			
			//System.out.println("imgLatLon  " + imgLatLon.toString());
			//System.out.println("scnLatLon  " + model.getField().getSceneCenter().toString());
			
			Point2D offst = model.getField().latLonToScreen(imgLatLon);
			
			g2.drawImage(_img, 
				(int)offst.getX() -_img.getWidth()/2, 
				(int)offst.getY() -_img.getHeight()/2, 
				null);
		}		
		
		if(click != null) {
			Point2D p = model.getField().latLonToScreen(click);
			
			g2.drawOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
		}
		
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		System.out.println("somethingChanged Satellite");
		
		if(changed.isFileopened() || changed.isZoom()) {
			
			
			loadMap();
			
			
		}
		
	}

	private void loadMap() {
		if(isActive()) {
			new Calc().start();
		}
	}
	
	protected void loadimg() {
		
		BufferedImage img = null;
		
		StaticMap map = new StaticMap(640, 640, GOOGLE_API_KEY);
		
		map.setScale(Field.MAP_SCALE);
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
		
		Graphics2D g2 = (Graphics2D)img.getGraphics();
		int r = 40;
		g2.drawLine(img.getWidth()/2-r, img.getHeight()/2, img.getWidth()/2+r, img.getHeight()/2);
		g2.drawLine(img.getWidth()/2, img.getHeight()/2-r, img.getWidth()/2, img.getHeight()/2+r);
		
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
		
		System.out.println("sat map mousePressed " + click.toString());
		listener.repaint();
		
		return true;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		
		dragPoint = null;
		
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
		
		return Arrays.asList(showLayerCheckbox.produce());
	}
	
}
