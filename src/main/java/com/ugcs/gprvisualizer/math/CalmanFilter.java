package com.ugcs.gprvisualizer.math;

import java.util.List;

import com.ugcs.gprvisualizer.gpr.Scan;

public class CalmanFilter {

	
	public void filter(List<Scan> list) {
		
		
		double ll[][] = new double[2][];
		ll[0] = new double[list.size()];
		ll[1] = new double[list.size()];
		
		for(int i = 0; i < list.size(); i++){			
			ll[0][i] = list.get(i).getLatDgr();
			ll[1][i] = list.get(i).getLonDgr();			
		}
		
		ll = new CalmanFilter().filt(ll);
		
		for(int i = 0; i < list.size(); i++){			
			list.get(i).setLatDgr(ll[0][i]);
			list.get(i).setLonDgr(ll[1][i]);
			
//			double[] lla = new double[3];
//			lla[0] = ll[0][i];
//			lla[1] = ll[1][i];
//			lla[2] = 0;
//			EcefLla.lla2ecef(lla);
			//list.get(i).gpsPoint = EcefLla.llaToGpsPoint(lla);
		}	
		
	}
	
	private double[] update(double mean1, double var1, double mean2, double var2){
		
		double new_mean = (var2 * mean1 + var1 * mean2) / (var1 + var2);
		double new_var = 1 / (1 / var1 + 1 / var2);
	    return new double[]{new_mean, new_var};
	}
	
	private double[] predict(double mean1, double var1, double mean2, double var2){
		double new_mean = mean1 + mean2;
		double new_var = var1 + var2;
	    return new double[]{new_mean, new_var};
	}
	
	public double[][] filt(double[][] src){
		
		//positions = [[float(p.get('lon')), float(p.get('lat'))] for p in points];
		double [] longitudes = src[0];//position[0] for position in positions];
		double [] latitudes = src[1];//[position[1] for position in positions];

		double[][] result = new double[2][];
		result[0] = new double[longitudes.length];
		result[1] = new double[longitudes.length];
		
		
		double motion_sig = 1.0;
		double measurement_sig = 10.0;

		double lng = longitudes[0];
		double lat = latitudes[0];
		double sigLng = 1.0;
		double sigLat = 1.0;


		for(int i = 0; i< longitudes.length; i++ ){
		     double[] r = update(lng, sigLng, longitudes[i], measurement_sig);
		     lng = r[0]; 
		     sigLng = r[1];
		    
		    r = predict(lng, sigLng, 0, motion_sig);
		    lng = r[0];
		    sigLng = r[1];
		    result[0][i] = lng;

		    r = update(lat, sigLat, latitudes[i], measurement_sig);
		    lat = r[0]; 
		    sigLat = r[1];		    
		    
		    r = predict(lat, sigLat, 0, motion_sig);
		    lat = r[0]; 
		    sigLat = r[1];
		    
		    result[1][i] = lat;
		}

		return result;
	}
	
	
}
