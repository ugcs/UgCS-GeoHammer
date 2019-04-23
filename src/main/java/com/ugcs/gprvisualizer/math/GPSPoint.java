package com.ugcs.gprvisualizer.math;

import java.util.Scanner;

import Jama.Matrix;

public class GPSPoint {

	public GPSPoint(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;		
		
	}
	
	public GPSPoint(String gpsPointProperty) {
		
		Scanner sc = new Scanner(gpsPointProperty);
		sc.useDelimiter(",");
		
		x = sc.nextLong();
		y = sc.nextLong();
		z = sc.nextLong(); 
	}

	public double x;
	public double y;
	public double z;
	
	public Matrix gpsPointAsMatrix(){
		return new Point3D(x, y, z).getMatrixVector();
	}
	
	
}
