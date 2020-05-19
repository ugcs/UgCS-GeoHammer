package com.ugcs.gprvisualizer.math;

import Jama.Matrix;

public class DronMath {

	
	public static Matrix getDronNorm(Matrix dronRelativeOrigin, 
			double zaxisAng, double xaxisAng) {
		Matrix longtitRotMatr = Rotation.getZRotationMatrix(zaxisAng);			
		Matrix zaxisRotateMtrx = longtitRotMatr.times(dronRelativeOrigin);
		
		Matrix latitRotMatr = Rotation.getYRotationMatrix(xaxisAng);			
		Matrix result = latitRotMatr.times(zaxisRotateMtrx);
		
		Matrix zaxis180RotateMtrx = Rotation.getZRotationMatrix(Math.PI);
		result = zaxis180RotateMtrx.times(result);
		
		return result;
	}

	
}
