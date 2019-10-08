package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.Settings;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;

public class SatelliteMap implements Layer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;

	private Random rand = new Random();
	private Color color = new Color(rand.nextInt(16777215));
	
	public SatelliteMap(Model model,  RepaintListener listener) {
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
	
	protected BufferedImage loadimg() {
		
		BufferedImage img = null;
		
		StaticMap map = new StaticMap(640, 640, "AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M");
		map.setMaptype(Maptype.hybrid);
		
		Point2D midlPoint = model.getSettings().middleLatLonDgr;
		map.setLocation(new Location(midlPoint.getX(), midlPoint.getY()), ZOOM); //40.714, -73.998 
		
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
		
		drawTrack(img);
		
		return img;
		
	}
	
	int ZOOM = 19;
	int ofs = 320;
	private void drawGPSPath(Graphics2D g2) {
		g2.setStroke(new BasicStroke(1.1f));

		g2.setColor(Color.RED);

		Point2D p2 = null;
		Point2D midlPoint = model.getSettings().middleLatLonDgr;
		g2.translate(ofs, ofs);
		int i=0;
		for (Scan scan : model.getScans()) {

						
			
//			Point2D p = latLonToPixels(
//					scan.getLatDgr() - midlPoint.getX(), 
//					scan.getLonDgr() - midlPoint.getY(), ZOOM);

			Point2D p = latLonToPixels(
					midlPoint.getX(), midlPoint.getY(),
					scan.getLatDgr(), 
					scan.getLonDgr(), ZOOM);
			
			
			if (p2 != null) {				
				if(i % 50 == 0) {
					System.out.println("" + ((int)p.getX()) + "  " + ((int)p.getY()) + "    " + scan.getLatDgr()  + " " + midlPoint.getX() );
				}
				i++;
				
				g2.drawLine((int)p2.getX(), (int)p2.getY(), (int)p.getX(), (int)p.getY());
			}
			p2 = p;
		}
	}
	
	
	private void drawTrack(BufferedImage img2) {
		
		Graphics2D g2 = (Graphics2D)img2.getGraphics();
		
		//model.getSettings().
		drawGPSPath(g2);
		
		
		
	}

	double tileSize = 256;
	double initialResolution = 2 * Math.PI * 6378137 / tileSize;
	//# 156543.03392804062 for tileSize 256 pixels
	double originShift = 0;//2 * Math.PI * 6378137 / 2.0;
	
	public Point2D latLonToMeters(double  lat, double lon) {
		//"Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913"

		double mx = lon * originShift / 180.0;
		double my = Math.log( Math.tan(  ((90 + lat) * Math.PI / 360.0 ))   ) / (Math.PI / 180.0);

        my = my * originShift / 180.0;
        
        return new Point2D.Double(mx, my);
	}

	double resolution(double zoom) { 
		return initialResolution / (Math.pow(2, zoom));
	}
	
	public Point2D metersToPixels(Point2D meters, double zoom) {
	        //"Converts EPSG:900913 to pyramid pixel coordinates in given zoom level"

		double res = resolution( zoom );
		double px = (meters.getX() + originShift) / res;
		double py = (meters.getY() + originShift) / res;
        return new Point2D.Double(px, py);
		
	}
	
	Point2D pixelsToMeters(double px, double py, double zoom) {
	    //"Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"

		double res = resolution(zoom);
		double mx = px * res - originShift;
		double my = py * res - originShift;
	    return new Point2D.Double(mx, my);
	}

	public Point2D metersToLatLon(Point2D meters) {
	    //"Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum"

		double lon = (meters.getX() / originShift) * 180.0;
		double lat = (meters.getY() / originShift) * 180.0;
		lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
		
	    return new Point2D.Double(lat, lon);
	}
	
	public Point2D latLonToPixels(double lat, double lon, double zoom) {
		return metersToPixels(latLonToMeters(lat, lon), zoom);
		
	}

	
	int i=0;
	public Point2D latLonToPixels(double cntlat, double cntlon, double lat, double lon, double zoom) {
		//return metersToPixels(latLonToMeters(lat, lon), zoom);
		
		//lat |     lon -
		
		double x = measure(cntlat, cntlon, cntlat, lon) * Math.signum(lon - cntlon);
		double y = -measure(cntlat, cntlon, lat, cntlon) * Math.signum(lat - cntlat);
		
//		if(i % 50 == 0) {
//			System.out.println("" + x + "  " + y);
//		}
//		i++;
		
		
		return metersToPixels(new Point2D.Double(x, y), zoom);
		//return new Point2D.Double(x*25, y*25);
	}
	
	public Point2D pixelsToLatLon(double px, double py, double zoom) {
		return metersToLatLon(pixelsToMeters(px, py, zoom));
	}
	
	double measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
		double  R = 6378.137; // Radius of earth in KM
		double  dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
		double  dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
		double  a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
		double  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double  d = R * c;
	    return d * 1000; // meters
	}	
	
	class Calc extends Thread {
		public void run() {
			System.out.println("satel run");
			
			img = loadimg();
			
			
			
			
			listener.repaint();
		}

		private void drawTrack(BufferedImage img) {
			// TODO Auto-generated method stub
			
		}
	};

}
