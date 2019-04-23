package com.ugcs.gprvisualizer.math;

import Jama.Matrix;

public class Rotation {

	public static Matrix getAttitudeFullRotationMatrix(Attitude attitude) {
		// '-' так как ось Z у нас вверх, а для Рыскания (Yaw) вниз 
		Matrix zRotation = getZRotationMatrix(-attitude.yaw);			
		// '-' так как ось Y у нас влево от курса, а для Тангажа(Pitch) вправо
		Matrix yRotation = getYRotationMatrix(-attitude.tangage);			
		
		Matrix xRotation = getXRotationMatrix(attitude.roll);
		
		Matrix fullrotationMatr = zRotation.times(yRotation).times(xRotation);
		return fullrotationMatr;
	}
	
	public static Matrix getViewMatrix(double longtitude, double altitude) {
		// '-' так как ось Z у нас вверх, а для Рыскания (Yaw) вниз 
		Matrix zRotation = getZRotationMatrix(-longtitude);			
		// '-' так как ось Y у нас влево от курса, а для Тангажа(Pitch) вправо
		Matrix yRotation = getXRotationMatrix(-altitude);			
		
		//Matrix xRotation = getXRotationMatrix(attitude.roll);
		
		Matrix fullrotationMatr = zRotation.times(yRotation);//.times(xRotation)
		return fullrotationMatr;
	}	
	
	public static Matrix getTransitionMatrix(Point3D p) {
		double[][] xArray =  
			{{1, 0,	0, p.x},
			 {0, 1,	0, p.y},
			 {0, 0,	1, p.z},
			 {0, 0, 0, 1  }};
		Matrix xRotation = new Matrix(xArray);
		return xRotation;
	}
	
	public static Matrix getXRotationMatrix(double ang) {
		double cos = Math.cos(ang);
		double sin = Math.sin(ang);
		double[][] xArray =  
			{{1, 0, 	0		,0},
			 {0, cos, 	-sin	,0},
			 {0, sin, 	cos		,0},
			 {0,0,0,1}};
		Matrix xRotation = new Matrix(xArray);
		return xRotation;
	}

	public static Matrix getYRotationMatrix(double ang) {
		double cos = Math.cos(ang);
		double sin = Math.sin(ang);		
		double[][] yArray =  
			{{cos,	0,	sin, 0 },
			 {0, 	1, 	0, 0	},
			 {-sin, 0, 	cos, 0	},
			 {0,0,0,1}};
		Matrix yRotation = new Matrix(yArray);
		return yRotation;
	}

	public static Matrix getZRotationMatrix(double ang) {
		double cos = Math.cos(ang);
		double sin = Math.sin(ang);
		double[][] zArray = 
			{{cos	, -sin	, 0,0},
			 {sin	, cos	, 0,0},
			 {0		, 0		, 1,0},
			 {0,0,0,1}};
		Matrix zRotation = new Matrix(zArray);
		return zRotation;
	}
	
	
}
