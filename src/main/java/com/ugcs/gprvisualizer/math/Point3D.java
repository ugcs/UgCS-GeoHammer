package com.ugcs.gprvisualizer.math;

import Jama.Matrix;

public class Point3D {

	public double x;
	public double y;
	public double z;
	
	public Point3D(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(Matrix resV) {
		this(
			resV.getArray()[0][0], 
			resV.getArray()[1][0], 
			resV.getArray()[2][0]);
	}

	public Matrix getMatrixVector() {
		double[][] dronPos = 
			{
				{ x },
				{ y }, 
				{ z },
				{ 1 }
			};
		Matrix shotPointV = new Matrix(dronPos);
		return shotPointV;
	}

	public double distance(Point3D point3d) {
		
		double dx = point3d.x-x;
		double dy = point3d.y-y;
		double dz = point3d.z-z;
		return Math.sqrt(dx*dx + dy*dy+dz*dz);
	}

//	@Override
//	public boolean equals(Object obj){
//		Point3D p2 = (Point3D)obj;
//		
//		//Math.
//		
//		return false;
//	}
	
}
