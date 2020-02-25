package com.ugcs.gprvisualizer.math;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Scan;

public class ManuilovFilter {

	static final int RANGE = 2;
	
	
	public void filter(List<Trace> list) {
		
		
		double ll[][] = new double[2][];
		ll[0] = new double[list.size()];
		ll[1] = new double[list.size()];
		
		for(int i = 0; i < list.size(); i++){			
			ll[0][i] = list.get(i).getLatLon().getLatDgr();
			ll[1][i] = list.get(i).getLatLon().getLonDgr();			
		}
		
		ll = filt(ll);
		
		for(int i = 0; i < list.size(); i++){			
			list.get(i).setLatLon(new LatLon(ll[0][i], ll[1][i])) ;
		}	
		
	}
	
	public double[][] filt(double[][] src){

		int size = src[0].length;
		double[][] result = new double[2][];
		result[0] = new double[size];
		result[1] = new double[size];
		
		for(int i=0; i<size; i++ ) {
			result[0][i] = avg(i, src[0], size); 
			result[1][i] = avg(i, src[1], size);			
			//result[0][i] = src[0][i];//avg(i, src[0]); 
			//result[1][i] = src[1][i];//result[1][i] = avg(i, src[1]);			

		}
				
		return result;
	}

	private double avg(int i, double[] ds, int size) {
		
		double weight = 0;
		double sum = 0;
		//don`t move points near edges
		if(i<RANGE || i> size-1-RANGE) {
			return ds[i];
		}
		
		for(int j= Math.max(0, i-RANGE); j< Math.min(size-1, i+RANGE); j++) {
			double dst = Math.abs(j - i);
			double kf = RANGE - dst;
			
			weight += kf;
			sum += (ds[j] * kf);
		}
		
		return sum / weight;
	}
	
}
