package com.ugcs.gprvisualizer.math;

public class EcefLla {
	/*
	*
	*  ECEF - Earth Centered Earth Fixed
	*   
	*  LLA - Lat Lon Alt
	*
	*  ported from matlab code at
	*  https://gist.github.com/1536054
	*     and
	*  https://gist.github.com/1536056
	*/
	 
	// WGS84 ellipsoid constants
	private static final double a = 6378137*100; // radius cm
	private static final double e = 8.1819190842622e-2;  // eccentricity
	 
	private static final double asq = Math.pow(a,2);
	private static final double esq = Math.pow(e,2);
	 
	public static GPSPoint llaToGpsPoint(double[] lla) {
		
		double [] ecef =  EcefLla.lla2ecef(lla);
		
		GPSPoint gps = new GPSPoint(ecef[0], ecef[1], ecef[2]);
		return gps;
	}
	
	
	
	public static double[] ecef2lla(double[] ecef){
	  double x = ecef[0];
	  double y = ecef[1];
	  double z = ecef[2];
	 
	  double b = Math.sqrt( asq * (1-esq) );
	  double bsq = Math.pow(b,2);
	  double ep = Math.sqrt( (asq - bsq)/bsq);
	  double p = Math.sqrt( Math.pow(x,2) + Math.pow(y,2) );
	  double th = Math.atan2(a*z, b*p);
	 
	  double lon = Math.atan2(y,x);
	  double lat = Math.atan2( (z + Math.pow(ep,2)*b*Math.pow(Math.sin(th),3) ), (p - esq*a*Math.pow(Math.cos(th),3)) );
	  double N = a/( Math.sqrt(1-esq*Math.pow(Math.sin(lat),2)) );
	  double alt = p / Math.cos(lat) - N;
	 
	  // mod lat to 0-2pi
	  lon = lon % (2*Math.PI);
	 
	  // correction for altitude near poles left out.
	 
	  double[] ret = {lat, lon, alt};
	 
	  return ret;
	}
	 
	 
	public static double[] lla2ecef(double[] lla){
	  double lat = lla[0];
	  double lon = lla[1];
	  double alt = lla[2];
	 
	  double N = a / Math.sqrt(1 - esq * Math.pow(Math.sin(lat),2) );
	  
	  double x = (N+alt) * Math.cos(lat) * Math.cos(lon);
	  double y = (N+alt) * Math.cos(lat) * Math.sin(lon);
	  double z = ((1-esq) * N + alt) * Math.sin(lat);
	 
	  double[] ret = {x, y, z};
	  return ret;
	}
	
	public static double degreeMinToRadians(String str){
		//5545.07821,
		//0123456789
		//03740.64899

		//55 45.07821
		
		int p = str.indexOf(".");
		String degree = str.substring(0, p-2); 
		String minutes = str.substring(p-2);
		
		return (Double.valueOf(degree) + Double.valueOf(minutes) / 60.0) / 180.0 * Math.PI;		
	}

	public static double[] ecefToLonLat(GPSPoint point){
		double a = 6378137; //radius
		double e = 8.1819190842622e-2;  // eccentricity
		double asq = a * a;
		double esq = e * e;
		  
		double x = point.x;
		double y = point.y;
		double z = point.z;
		  
		double longtitude = Math.atan2(y, x);
		  
		double b = Math.sqrt( asq * (1-esq) );
		double bsq = b * b;
		double ep = Math.sqrt( (asq - bsq)/bsq);
		double p = Math.sqrt( x * x + y * y );
		double th = Math.atan2(a*z, b*p);
		  
		
		double latitude = Math.atan2( 
				(z + ep * ep * b * Math.pow(Math.sin(th), 3)), 
				(p - esq * a * Math.pow(Math.cos(th), 3)) );

		double [] result = {longtitude, latitude};
		return result;
	}

	
}
