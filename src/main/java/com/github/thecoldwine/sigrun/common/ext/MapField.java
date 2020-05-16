package com.github.thecoldwine.sigrun.common.ext;

import java.awt.geom.Point2D;

public class MapField {

	private LatLon pathCenter;
	private LatLon sceneCenter;
	private int zoom;

	public MapField() {
		
	}
	
	public MapField(MapField field) {
		this.pathCenter = field.pathCenter;
		this.sceneCenter = field.sceneCenter;
		this.zoom = field.zoom;
	}


	public static void main(String[] args) {
		
		
		new MapField().test();
	}
	
	
	protected void tst(LatLon ll) {
		Point2D p = latLonToScreen(ll);
		LatLon ll2 = screenTolatLon(p);
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
	private static final double tileSize = 256 * MAP_SCALE;
	private static final double R = 6378137;
	private static final double initialResolution = 2 * Math.PI * R / tileSize;

	double resolution(double zoom) { 
		return initialResolution / (Math.pow(2, zoom));
	}
	
	private static double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void setZoom(int zoom) {
		this.zoom = Math.max(0, Math.min(30, zoom));
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
