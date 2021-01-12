package com.github.thecoldwine.sigrun.common.ext;

import java.awt.geom.Point2D;

import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.draw.HereMapProvider;
import com.ugcs.gprvisualizer.draw.MapProvider;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

public class MapField {

	private LatLon pathCenter;
	private LatLon pathLt; 
	private LatLon pathRb;
	
	
	private LatLon sceneCenter;
	private int zoom;
	
	MapProvider mapProvider = new HereMapProvider();//new GoogleMapProvider();

	public MapProvider getMapProvider() {
		return mapProvider;
	}
	
	public MapField() {
		
	}
	
	public MapField(MapField field) {
		this.pathCenter = field.pathCenter;
		this.sceneCenter = field.sceneCenter;
		this.zoom = field.zoom;
		
		this.pathLt = field.pathLt;
		this.pathRb = field.pathRb;
	}

	public boolean isActive() {
		return pathCenter != null; 
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
			(p2d.getX() - psc.getX()) * mapProvider.getMapScale(), 
			(p2d.getY() - psc.getY()) * mapProvider.getMapScale());
		
		return result;		
	}
	
	public LatLon screenTolatLon(Point2D point) {
		
		Point2D psc = GoogleCoord.createInfoWindowContent(getSceneCenter(), getZoom());
		Point2D p = new Point2D.Double(
			psc.getX() + point.getX() / mapProvider.getMapScale(), 
			psc.getY() + point.getY() / mapProvider.getMapScale());
		
		return GoogleCoord.llFromP(p, getZoom());
	}
	
	//public static final int MAP_SCALE = 1;
	private double getTileSize() {
		return 256 * mapProvider.getMapScale();
	}
	private static final double R = 6378137;
	private double getInitialResolution() {
		return 2 * Math.PI * R / getTileSize();
	}

	double resolution(double zoom) { 
		return getInitialResolution() / (Math.pow(2, zoom));
	}
	
	private static double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void setZoom(int zoom) {
		this.zoom = Math.max(0, Math.min(mapProvider.getMaxZoom(), zoom));
	}

	public LatLon getSceneCenter() {
		return sceneCenter;
	}

	public void setSceneCenter(LatLon sceneCenter) {
		if (isActive()) {
			this.sceneCenter = sceneCenter;
		} else {
			this.sceneCenter = null;
		}
	}	

	public LatLon getPathCenter() {
		return pathCenter;
	}

	public void setPathCenter(LatLon pathCenter) {
		this.pathCenter = pathCenter;
	}	

	
	public void setPathEdgeLL(LatLon lt, LatLon rb) {
		this.pathLt = lt;
		this.pathRb = rb;
	}

	public LatLon getPathLeftTop() {
		return pathLt;
	}
	
	public LatLon getPathRightBottom() {
		return pathRb;
	}
	
	public void adjustZoom(int screenWidth, int screenHeight) {
		
		int zoom = 28;
		setZoom(zoom);
		Point2D scr = latLonToScreen(pathRb);
		
		//Sout.p("t " + scr.getX() + "  " +  scr.getY() + " z " + zoom );
		
		while (zoom > 2
				&& (Math.abs(scr.getX()) > screenWidth / 2
				|| Math.abs(scr.getY()) > screenHeight / 2)) {
			zoom--;
			setZoom(zoom);
			
			scr = latLonToScreen(pathRb);
			
			//Sout.p("t " + scr.getX() + "  " +  scr.getY() + " z " + zoom );
		}
	}

	
}
