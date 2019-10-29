package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;

public class LevelFilter {

	public void execute(Model model) {
		
		List<Trace> lst = model.getFileManager().getTraces();
		
		
		float avg[] = new float[lst.get(100).getOriginalValues().length];
		
		
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			
			arraySum(avg, trace.getOriginalValues());
		}
		
		arrayDiv(avg, lst.size());
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			
			float normval[] = Arrays.copyOf(trace.getOriginalValues(), trace.getOriginalValues().length);
			arraySub(normval, avg);
			
			trace.setNormValues(normval);
		}

		
		int maxindex[] = new int[lst.size()];
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			//maxindex[index] = getMaxIndex(trace);
			trace.maxindex = getMaxIndex(trace);
		}
		
		
	}

	private void arraySum(float avg[], float add[]) {
		for(int i = 0; i<avg.length && i<add.length; i++) {
			avg[i] += add[i];
		}
	}

	private void arraySub(float avg[], float add[]) {
		for(int i = 0; i<avg.length && i<add.length; i++) {
			avg[i] -= add[i];
		}
	}

	private void arrayDiv(float avg[], float divider) {
		for(int i = 0; i<avg.length; i++) {
			avg[i] /= divider;
		}
	}

	private int getMaxIndex(Trace trace) {
		int maxIndex = -1;
		float [] values = trace.getNormValues();
		for(int i=0; i<values.length; i++) {
			if(maxIndex == -1 || values[maxIndex] < values[i] ) {
				maxIndex = i;
			}			
		}
		
		return maxIndex;
	}
	
}
