package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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

import org.apache.commons.lang3.StringUtils;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

public class SatelliteMap extends BaseLayer {

	private RepaintListener listener;
	
	private BufferedImage img;
	private LatLon imgLatLon;
	private int imgZoom;

	private Random rand = new Random();
	private Color color = new Color(rand.nextInt(16777215));
	
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			setActive(showLayerCheckbox.isSelected());
			if(isActive()) {
				loadMap();
			}else {
				listener.repaint();
			}
				
		}
	};
	
	private ToggleButton showLayerCheckbox = new ToggleButton("", ResourceImageHolder.getImageView("gmap-20.png"));
	{
		boolean apiExists = StringUtils.isNotBlank(GOOGLE_API_KEY);
		
		showLayerCheckbox.setDisable(!apiExists);
		showLayerCheckbox.setSelected(apiExists);
		showLayerCheckbox.setOnAction(showMapListener);
	}
	
	private static String GOOGLE_API_KEY;
	static {
		try {
			InputStream inputStream = SatelliteMap.class.getClassLoader().getResourceAsStream("googleapikey");
			java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
			GOOGLE_API_KEY = s.hasNext() ? s.next() : "";
		}catch(Exception e) {
			System.out.println("no google api key -> no googlemaps");
			
		}
	}
	
	
	
	private LatLon click;
	
	public SatelliteMap(Dimension parentDimension, Model model,  RepaintListener listener) {
		super(parentDimension, model);
		
		this.listener = listener;
		
	}
	
	@Override
	public void draw(Graphics2D g2) {
		BufferedImage _img = img;
		if(_img != null && isActive()) {
			
			Point2D offst = model.getField().latLonToScreen(imgLatLon);
			
			double scale = Math.pow(2, model.getField().getZoom() - imgZoom);
			
			g2.drawImage(_img, 
				(int)(offst.getX() -_img.getWidth()/2*scale), 
				(int)(offst.getY() -_img.getHeight()/2*scale),
				(int)(_img.getWidth()*scale),
				(int)(_img.getHeight()*scale),
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
		
		
		if(changed.isFileopened() || changed.isZoom()) {
			
			
			loadMap();
			
			
		}
		
	}

	private void loadMap() {
		if(isActive() && StringUtils.isNotBlank(GOOGLE_API_KEY)) {
			new Calc().start();
		}
	}
	
	protected void loadimg() {
		
		BufferedImage img = null;
		
		StaticMap map = new StaticMap(640, 640, GOOGLE_API_KEY);
		
		map.setScale(Field.MAP_SCALE);
		map.setMaptype(Maptype.hybrid);
		
		LatLon midlPoint = model.getField().getSceneCenter();
		int imgZoom = model.getField().getZoom();
		map.setLocation(new Location(midlPoint.getLatDgr(), midlPoint.getLonDgr()), imgZoom); //40.714, -73.998 
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
		this.imgZoom = imgZoom;
		
	}
	
	class Calc extends Thread {
		public void run() {
		
			loadimg();
			
			listener.repaint();
		}
	}

	Field dragField = null;
	
	@Override
	public boolean mousePressed(Point2D point) {
		
		if(!model.getFileManager().isActive()) {
			return false;
		}
		
		dragField = new Field(model.getField());
		
		click = model.getField().screenTolatLon(point);
		
		AppContext.statusBar.showGPSPoint(click);
		
		//System.out.println("sat map mousePressed " + click.toString());
		listener.repaint();
		
		return true;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		
		dragField = null;
		click = null;
		
		AppContext.notifyAll(new WhatChanged(Change.mapscroll));
		
		return true;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		
		if(dragField == null) {
			return false;
		}

		LatLon newCenter = dragField.screenTolatLon(point);		
		double lat = dragField.getSceneCenter().getLatDgr() + click.getLatDgr() - newCenter.getLatDgr();
		double lon = dragField.getSceneCenter().getLonDgr() + click.getLonDgr() - newCenter.getLonDgr();
		
		model.getField().setSceneCenter(new LatLon(lat, lon));
		
		listener.repaint();
		
		return true;
	};

	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(showLayerCheckbox);
	}
	
}
