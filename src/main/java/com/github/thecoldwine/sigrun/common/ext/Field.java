package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Field {

	private LatLon pathCenter;
	private LatLon sceneCenter;
	private int zoom;

	
	public static void main(String[] args) {
		
		
		new Field().test();
	}
	
	
	protected void tst(LatLon ll) {
		Point2D p = latLonToScreenD(ll);
		LatLon ll2 = screenTolatLon(p);
		
		System.out.println(ll + " -> " + p + " -> " + ll2);
		
	}
	
	protected void test() {
		setSceneCenter(new LatLon(15, 40));
		setZoom(19);		
		tst(new LatLon(15, 40));
		tst(new LatLon(15.001, 40.001));
		tst(new LatLon(15.002, 40.002));
		
	}
	
	public Point latLonToScreen(LatLon latlon) {
		
		Point2D p2d = latLonToScreenD(latlon);
		Point result = new Point((int)p2d.getX(), (int)p2d.getY());
		
		return result;		
	}
	
	public Point2D latLonToScreenD(LatLon latlon) {
		if(latlon == null) {
			System.out.println(" latlon == null ");
		}
		
		Point2D result = latLonToPixels(
				sceneCenter.getLatDgr(), sceneCenter.getLonDgr(),
				latlon.getLatDgr(), latlon.getLonDgr(), 
				zoom);
		
		
		return result;		
	}
	
	public LatLon screenTolatLon(Point2D point) {
		
		LatLon result = pixelsToLatLon(point.getX(), point.getY(), zoom);
		
		return result;		
	}
	
	public void scroll(int dx, int dy) {
		Point point = new Point(dx, dy); 
		LatLon newLL = screenTolatLon(point);
		
		setSceneCenter(newLL);
	}
	
	
	public static final int MAP_SCALE = 2;
	double tileSize = 256 * MAP_SCALE;
	double R = 6378137;
	double initialResolution = 2 * Math.PI * R / tileSize;
	//# 156543.03392804062 for tileSize 256 pixels
	double originShift = 0;//2 * Math.PI * 6378137 / 2.0;
	
	private Point2D latLonToMeters_(double  lat, double lon) {
		//"Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913"

		double mx = lon * originShift / 180.0;
		double my = Math.log( Math.tan(  ((90 + lat) * Math.PI / 360.0 ))   ) / (Math.PI / 180.0);

        my = my * originShift / 180.0;
        
        return new Point2D.Double(mx, my);
	}

	double resolution(double zoom) { 
		return initialResolution / (Math.pow(2, zoom));
	}
	
	private Point2D metersToPixels(Point2D meters, double zoom) {
	        //"Converts EPSG:900913 to pyramid pixel coordinates in given zoom level"

		double res = resolution( zoom );
		double px = meters.getX() / res;
		double py = meters.getY() / res;
        return new Point2D.Double(px, py);
		
	}
	
	private Point2D pixelsToMeters(double px, double py, double zoom) {
	    //"Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"

		double res = resolution(zoom);
		double mx = px * res;
		double my = py * res;
	    return new Point2D.Double(mx, my);
	}

	private LatLon metersToLatLon(Point2D meters) {
	    //"Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum"

		double lat = sceneCenter.getLatDgr() - meters.getY() * 180.0 / (R * Math.PI);
		
		
		double r = R * Math.cos(toRad(sceneCenter.getLatDgr()));
		double lon = sceneCenter.getLonDgr() + meters.getX() * 180.0 / (r * Math.PI);		
		
		
		
		//double lat = (meters.getY() / originShift) * 180.0;
		//lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
		
	    return new LatLon(lat, lon);
	}
	
	private Point2D latLonToPixels_(double lat, double lon, double zoom) {
		return metersToPixels(latLonToMeters_(lat, lon), zoom);
	}

	private Point2D latLonToPixels(double cntlat, double cntlon, double lat, double lon, double zoom) {
		double x = measure(cntlat, cntlon, cntlat, lon) * Math.signum(lon - cntlon);
		double y = -measure(cntlat, cntlon, lat, cntlon) * Math.signum(lat - cntlat);
		
		return metersToPixels(new Point2D.Double(x, y), zoom);

	}
	
	private LatLon pixelsToLatLon(double px, double py, double zoom) {
		return metersToLatLon(pixelsToMeters(px, py, zoom));
	}
	
	private double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	private double measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
		double  R = 6378.137; // Radius of earth in KM
		double  dLat = toRad(lat2) - toRad(lat1);
		double  dLon = toRad(lon2) - toRad(lon1);
		
		double  a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
			Math.sin(dLon/2) * Math.sin(dLon/2);
		
		double  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double  d = R * c;
	    return d * 1000; // meters
	}	

	
	private double measureHor(double lat1, double lon1, double lon2){  // generally used geo measurement function
		// lon - долгота - горизонталь
		// lat - шиорта - вертикаль
		double  R = 6378.137; // Radius of earth in KM
		double  dLon = toRad(lon2) - toRad(lon1);
		
		double  a = 
			Math.cos(toRad(lat1)) * Math.cos(toRad(lat1)) *
			Math.sin(dLon/2) * Math.sin(dLon/2);
		
		double  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double  d = R * c;
	    return d * 1000; // meters
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
