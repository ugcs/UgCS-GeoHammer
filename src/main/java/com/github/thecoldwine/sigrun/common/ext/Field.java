package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.ugcs.gprvisualizer.math.CoordinatesMath;

public class Field {

	private LatLon pathCenter;
	private LatLon sceneCenter;
	private int zoom;

	
	public static void main(String[] args) {
		
		
		new Field().test();
	}
	
	
	protected void tst(LatLon ll) {
		Point2D p = latLonToScreen(ll);
		LatLon ll2 = screenTolatLon(p);
		
		System.out.println(ll + " -> " + p + " -> " + ll2);
		
	}
	protected void tst2(Point2D p1) {

		LatLon ll = screenTolatLon(p1);
		Point2D p2 = latLonToScreen(ll);
		
	}
	
	protected void test() {
		setSceneCenter(new LatLon(15, 40));
		setZoom(19);		
		tst(new LatLon(15, 40));
		tst(new LatLon(15.001, 40.001));
		tst(new LatLon(15.002, 40.002));
		
		tst2(new Point2D.Double(0, 0));
		tst2(new Point2D.Double(10, 10));
		tst2(new Point2D.Double(20, 20));
		tst2(new Point2D.Double(40, 40));
		tst2(new Point2D.Double(100, 100));
	}
	
	public Point2D latLonToScreen(LatLon latlon) {
		
		Point2D psc = GoogleCoord.createInfoWindowContent(getSceneCenter(), getZoom());
		Point2D p2d = GoogleCoord.createInfoWindowContent(latlon, getZoom());
		
		Point2D result = new Point2D.Double(
			(p2d.getX() - psc.getX()) * MAP_SCALE, 
			(p2d.getY() - psc.getY()) * MAP_SCALE);
		
		return result;		
	}
	
	public LatLon screenTolatLon(Point2D point) {
		
		Point2D psc = GoogleCoord.createInfoWindowContent(getSceneCenter(), getZoom());
		Point2D p = new Point2D.Double(
			psc.getX() + point.getX() / MAP_SCALE, 
			psc.getY() + point.getY() / MAP_SCALE);
		
		return GoogleCoord.llFromP(p, getZoom());
	}
	
	public static final int MAP_SCALE = 2;
	private double tileSize = 256 * MAP_SCALE;
	private double R = 6378137;
	private double initialResolution = 2 * Math.PI * R / tileSize;

//	private double originShift = 0;//2 * Math.PI * 6378137 / 2.0;
	
	double resolution(double zoom) { 
		return initialResolution / (Math.pow(2, zoom));
	}
	
//	private Point2D metersToPixels(Point2D meters, double zoom) {
//	        //"Converts EPSG:900913 to pyramid pixel coordinates in given zoom level"
//
//		double res = resolution( zoom );
//		double px = meters.getX() / res;
//		double py = meters.getY() / res;
//        return new Point2D.Double(px, py);
//		
//	}
//	
//	private Point2D pixelsToMeters(double px, double py, double zoom) {
//	    //"Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"
//
//		double res = resolution(zoom);
//		double mx = px * res;
//		double my = py * res;
//	    return new Point2D.Double(mx, my);
//	}
//
//	private LatLon metersToLatLon(Point2D meters) {
//	    //"Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum"
//
//		double lat = sceneCenter.getLatDgr() - meters.getY() * 180.0 / (R * Math.PI);
//		
//		
//		double r = R * Math.cos(toRad(sceneCenter.getLatDgr()));
//		double lon = sceneCenter.getLonDgr() + meters.getX() * 180.0 / (r * Math.PI);		
//		
//		
//		
//		//double lat = (meters.getY() / originShift) * 180.0;
//		//lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
//		
//	    return new LatLon(lat, lon);
//	}
	
//	private Point2D latLonToPixels_(double lat, double lon, double zoom) {
//		return metersToPixels(latLonToMeters_(lat, lon), zoom);
//	}

//	private Point2D latLonToPixels(double cntlat, double cntlon, double lat, double lon, double zoom) {
//		double x = CoordinatesMath.measure(cntlat, cntlon, cntlat, lon) * Math.signum(lon - cntlon);
//		double y = -CoordinatesMath.measure(cntlat, cntlon, lat, cntlon) * Math.signum(lat - cntlat);
//		
//		return metersToPixels(new Point2D.Double(x, y), zoom);
//
//	}
//	
//	private LatLon pixelsToLatLon(double px, double py, double zoom) {
//		return metersToLatLon(pixelsToMeters(px, py, zoom));
//	}
	
	private static double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public LatLon getSceneCenter() {
		return sceneCenter;
	}

	public void setSceneCenter(LatLon sceneCenter) {
		this.sceneCenter = sceneCenter;
	}	

	public LatLon getPathCenter() {
		return pathCenter;
	}

	public void setPathCenter(LatLon pathCenter) {
		this.pathCenter = pathCenter;
	}	
	
}
