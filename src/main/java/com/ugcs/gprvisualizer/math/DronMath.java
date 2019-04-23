package com.ugcs.gprvisualizer.math;

import Jama.Matrix;

public class DronMath {

	
	public static Matrix getDronNorm(Matrix dronRelativeOrigin, double zAng, double xAng) {
		Matrix longtitRotMatr = Rotation.getZRotationMatrix(zAng);			
		Matrix zRotatedV = longtitRotMatr.times(dronRelativeOrigin);		
		
		Matrix latitRotMatr = Rotation.getYRotationMatrix(xAng);			
		Matrix result = latitRotMatr.times(zRotatedV);
		
		Matrix Z180RotMatr = Rotation.getZRotationMatrix(Math.PI);
		result = Z180RotMatr.times(result);
		
		return result;
	}

	
}
