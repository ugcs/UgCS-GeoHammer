package com.ugcs.gprvisualizer.gpr;

import com.ugcs.gprvisualizer.math.Point3D;

public class Scan {

	double lon_dgr;
	double lat_dgr;
	public Point3D point;
	
	public int localX;
	public int localY;
	
	public float[] values;

	public double getLonDgr() {
		return lon_dgr;
	}

	public void setLonDgr(double val) {
		lon_dgr = val;;
	}
	
	public double getLatDgr() {
		return lat_dgr;
	}
	
	public void setLatDgr(double val) {
		lat_dgr = val;
	}

}
