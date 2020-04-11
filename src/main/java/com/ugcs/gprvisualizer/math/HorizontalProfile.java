package com.ugcs.gprvisualizer.math;

import java.awt.Color;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;

public class HorizontalProfile {

	// in smp
	public int deep[];
	
	public int minDeep;
	public int maxDeep;
	public int height;
	public double avgval;
	
	public Color color = new Color(50, 200, 255);
	
	public HorizontalProfile(int size){
		deep = new int[size];
	}
	
	public void finish(List<Trace> list) {
		
		minDeep = deep[0];
		maxDeep = deep[0];
		
		double valsum = 0;
		
		for(int i=0; i<deep.length; i++) {
			
			minDeep = Math.min(deep[i], minDeep);
			maxDeep = Math.max(deep[i], maxDeep);
			
			float[] values = list.get(i).getNormValues();
			int d = deep[i];
			valsum+= values[d];
		}
		
		avgval = valsum / deep.length;
		height = maxDeep - minDeep;
	}
	
	
}
