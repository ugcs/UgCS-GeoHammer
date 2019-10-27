package com.ugcs.gprvisualizer.math;

public class CoordinatesMath {

	public static double measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
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

	
	private static double toRad(double degree) {
		return degree * Math.PI / 180;
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

}
