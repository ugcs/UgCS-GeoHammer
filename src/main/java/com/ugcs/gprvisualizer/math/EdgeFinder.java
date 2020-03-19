package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class EdgeFinder {

	public void process(Model model) {
		
		
		for(SgyFile sgyFile : model.getFileManager().getFiles()) {
			
			process(sgyFile);
			
		}
		
//		model.getFileManager().clearTraces();
//		
//		model.init();
//		
//		//model.initField();
//		model.getVField().clear();
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
	}

	
	private static double SPEED_SM_NS_VACUUM = 30.0;
	private static double SPEED_SM_NS_SOIL = SPEED_SM_NS_VACUUM / 3.0;
	
	private void process(SgyFile sgyFile) {
		
		
		List<Trace> traces = sgyFile.getTraces();
		
		for(int i=0; i<traces.size(); i++) {
			Trace trace = traces.get(i);
			float[] values = trace.getNormValues();
			trace.edge = new int[values.length];
			
			int mxind = 0;
			for(int s=1; s<values.length; s++) {
				
				int s1 = (int)Math.signum(values[s-1]);
				int s2 = (int)Math.signum(values[s]);
				
				if(s1 != s2) {
					trace.edge[s] = s1 > s2 ? 1 : 2;
					trace.edge[mxind] = values[mxind] < 0 ? 3 : 4;
					mxind = s;
				}
				
				if(Math.abs(values[mxind]) < Math.abs(values[s]) ) {
					mxind = s;
				}
				
				
			}
			
		}
		
		
	}


	private Trace merge(List<Trace> stack) {
		Trace example = stack.get(stack.size()/2);
		float[] res = new float[example.getNormValues().length];
		
		for(int i=0; i< res.length; i++) {
			
			res[i] = mergeVal(stack, i);
			
		}
		
		Trace combined = new Trace(example.getBinHeader(), example.getHeader(), res, example.getLatLon());
		
		combined.verticalOffset = example.verticalOffset; 
		
		return combined;
	}


	private float mergeVal(List<Trace> stack, int i) {
		float max = stack.get(0).getNormValues()[i];
		float min = stack.get(0).getNormValues()[i];
		int positiveCount = 0;
		int negativeCount = 0;
		
		for(Trace t : stack) {
			float v = t.getNormValues()[i];
			
			max = Math.max(max, v);
			min = Math.min(min, v);
			if(v > 0) {
				positiveCount++;
			}else {
				negativeCount++;
			}			
		}
		
		int limit = stack.size()/4;
		if(positiveCount < limit) {
			return min;
		}
		if(negativeCount < limit) {
			return max;
		}
		
		return Math.abs(max) > Math.abs(min) ? max : min;
	}
	
}
