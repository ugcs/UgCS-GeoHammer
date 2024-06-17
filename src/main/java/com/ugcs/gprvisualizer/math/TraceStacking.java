package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.commands.Command;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;

public class TraceStacking implements Command {

	Model model = AppContext.model;
	

	
	public void execute(SgyFile sgyFile, ProgressListener listener) {
		
		double sampleDist = sgyFile.getSamplesToCmGrn();
		
		// to meters
		double stackDist = sampleDist;		
		System.out.println(" sample length: " + stackDist);
		
		
		List<Trace> traces = sgyFile.getTraces();
		List<Trace> result = new ArrayList<>();
		List<Trace> stack = new ArrayList<>();
		double stackSumDist = 0;
		
		for (int i = 0; i < traces.size(); i++) {
			
			Trace t1 = traces.get(i);
			stack.add(t1);
			stackSumDist += t1.getPrevDist();
			
			if (stackSumDist >= stackDist) {
				
				result.add(merge(stack));
				
				stack.clear();
				stackSumDist = 0;				
			}
		}
		
		sgyFile.setTraces(result);
		sgyFile.updateInternalIndexes();
		sgyFile.updateInternalDist();		
		
	}


	private Trace merge(List<Trace> stack) {
		Trace example = stack.get(stack.size() / 2);
		float[] res = new float[example.getNormValues().length];
		
		for (int i = 0; i < res.length; i++) {
			
			res[i] = mergeVal(stack, i);
			
		}
		
		Trace combined = new Trace(example.getFile(), example.getBinHeader(), example.getHeader(),
				res, example.getLatLon());
		
		combined.verticalOffset = example.verticalOffset; 
		
		return combined;
	}


	private float mergeVal(List<Trace> stack, int i) {
		float max = stack.get(0).getNormValues()[i];
		float min = stack.get(0).getNormValues()[i];
		int positiveCount = 0;
		int negativeCount = 0;
		
		for (Trace t : stack) {
			float v = t.getNormValues()[i];
			
			max = Math.max(max, v);
			min = Math.min(min, v);
			if (v > 0) {
				positiveCount++;
			} else {
				negativeCount++;
			}			
		}
		
		int limit = stack.size() / 4;
		if (positiveCount < limit) {
			return min;
		}
		if (negativeCount < limit) {
			return max;
		}
		
		return Math.abs(max) > Math.abs(min) ? max : min;
	}


	@Override
	public String getButtonText() {

		return "Stacking";
	}


	@Override
	public Change getChange() {

		model.getFileManager().clearTraces();
		
		model.init();

		model.getProfileField().clear();
		
		return Change.traceCut;
	}
	
}
