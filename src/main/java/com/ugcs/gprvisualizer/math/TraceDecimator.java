package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class TraceDecimator {

	public void process(Model model) {
		
		
		for(SgyFile sgyFile : model.getFileManager().getFiles()) {
			
			process(sgyFile);
			
		}
		
		model.getFileManager().clearTraces();
		
		model.init();
		
		//model.initField();
		model.getVField().clear();
		
		AppContext.notifyAll(new WhatChanged(Change.traceCut));
	}

	private void process(SgyFile sgyFile) {
		
		List<Trace> traces = sgyFile.getTraces();
		List<Trace> result = new ArrayList<>();
		
		for(int i=0; i<traces.size()-1; i+=2) {
			
			Trace t1 = traces.get(i);
			Trace t2 = traces.get(i+1);
			
			mergeValues(t1.getNormValues(), t2.getNormValues());
			result.add(t1);
		}
		
		sgyFile.setTraces(result);
		sgyFile.updateInternalIndexes();
		sgyFile.updateInternalDist();
		
		
		
	}

	private void mergeValues(float[] val1, float[] val2) {
		for(int i=0; i< Math.min(val1.length, val2.length); i++) {
			
			val1[i] = Math.abs(val1[i]) > Math.abs(val2[i]) ? val1[i] : val2[i];
		}		
		
	}
	
	
}
