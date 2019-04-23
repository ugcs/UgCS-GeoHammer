package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.math.DronMath;
import com.ugcs.gprvisualizer.math.EcefLla;
import com.ugcs.gprvisualizer.math.GPSPoint;
import com.ugcs.gprvisualizer.math.Point3D;

import Jama.Matrix;

public class ScanBuilder {
	private GPSPoint origin = null;
	private double originLongtitude; 
	private double originLatitude;
	private Double minX, maxX, minY, maxY;
	private List<Scan> scans = new ArrayList<>();
	
	
	public Rectangle2D.Double getBounds(){
		
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
		
	}
	
	public List<Scan> getScans() {
		return scans;
	}
	
	public void put(double lon, double lat, float[] values){
		
		//System.out.println(String.format(" %.6f,  %.6f  ", lon, lat));
		if(Math.abs(lon) < 0.1 && Math.abs(lat) < 0.1) {
			return;
		}
		
		//prism:  65.3063232422°N	 40.0569335938°W
		//        65.3063232421875  -40.05693359375 
		
		double rlon = convertDegreeFraction(lon); 
		double rlat = convertDegreeFraction(lat);

		//putInt( lon / 100.0 * Math.PI / 180.0, lat / 100.0 * Math.PI / 180.0 , values );
		putInt( rlon , rlat , values );
		
	}
	
	private double convertDegreeFraction(double org) {
		org = org / 100.0;
		int dgr = (int)org;
		double fract = org - dgr;
		double rx = dgr + fract / 60.0 * 100.0;
		return rx;
	}
	
	
	private void putInt(double lon_dgr, double lat_dgr, float[] values ){
		
		//depthSpectrum.analyze(values);
		
		Scan scan = new Scan();
		scan.lon_dgr = lon_dgr;
		scan.lat_dgr = lat_dgr;
		
		
		double lon = lon_dgr * Math.PI / 180.0; 
		double lat = lat_dgr * Math.PI / 180.0;
		
		// long			lat
		//-4003.4102    6518.38
		
		double[] lla = new double[] {lat, lon, 6378137*100.0};
		GPSPoint pnt = EcefLla.llaToGpsPoint(lla);

		if(origin == null){
			origin = pnt;
			originLatitude = lat;
			originLongtitude = lon;			
		}
		
		Matrix dronRelativeOrigin = pnt.gpsPointAsMatrix().minus( origin.gpsPointAsMatrix());
		
		//
		Matrix dronNorm = DronMath.getDronNorm(dronRelativeOrigin, -originLongtitude, originLatitude - Math.PI/2);
		
		Point3D p = new Point3D(dronNorm);
		p = new Point3D(p.y, p.x, p.z);
		
		scan.point = p; 
		scan.values = normalize(values);
		scans.add(scan);
		
		minX = (minX == null || minX > p.x) ? p.x : minX;
		minY = (minY == null || minY > p.y) ? p.y : minY;
		maxX = (maxX == null || maxX < p.x) ? p.x : maxX;
		maxY = (maxY == null || maxY < p.y) ? p.y : maxY;

		
	}

	private float[] normalize(float[] values) {
		float[] avgvalues = avg(values);
		
		
		
		float[] nrmvalues = new float[values.length];
		
		for(int i=0; i<values.length; i++) {
			nrmvalues[i] = Math.abs(values[i] - avgvalues[i]);
		}
		
		
		// remove small values 
		float[] hi = new float[values.length];
		for(int i=0; i<values.length; i++) {
			float mx  = 0;
			for(int j = i-2; j <= i+2; j++) {

				mx = Math.max(mx, nrmvalues[norm(j, values.length)]);
			}
			
			hi[i] = mx;
		}
		
		
		
		return avgabs(hi);
	}
	
	private float[] avgabs(float[] values) {
		for (int c = 0; c < 10; c++) {
			float[] r = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				r[i] = 0.1f * values[norm(i - 1, values.length)] + 0.8f * values[i] + 0.1f * values[norm(i + 1, values.length)];
			}
			values = r;
		}

		return values;
	}

	
	private float[] avg(float[] values) {
		for (int c = 0; c < 40; c++) {
			float[] r = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				r[i] = 0.2f * values[norm(i - 1, values.length)] + 0.6f * values[i] + 0.2f * values[norm(i + 1, values.length)];
			}
			values = r;
		}

		return values;
	}

	int norm(int i, int max){
		return i < 0 ? 0 : (i >= max ? max-1 : i);
	}

}
