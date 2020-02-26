package com.ugcs.gprvisualizer.gpr;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;

public class MedianScaleBuilder implements ArrayBuilder {

	private Model model;
	
	double[][] scale = null;
//	private float[] maxvalues = new float[1];
//	private float[] avgvalues = new float[1];
//	private double avgcount=0;
	
	public MedianScaleBuilder(Model model) {
		
		this.model = model;

	}
	
	public void clear() {
//		maxvalues = new float[1];
//		avgvalues = new float[1];
//		avgcount = 0;		
		
		scale = null;
	}
	
	@Override
	public double[][] build() {
		
		List<Trace> traces = model.getFileManager().getTraces();
		if(scale != null) {
			return scale;
		}
		
		scale = new double[2][model.getMaxHeightInSamples()];
		
		for(int smp=0; smp<model.getMaxHeightInSamples(); smp++) {
			float[] horizontalValues = new float[traces.size()]; 
			for(int traceIndex=0; traceIndex<traces.size(); traceIndex++) {
				Trace trace = traces.get(traceIndex);
				
				float[] vals = trace.getNormValues();
				horizontalValues[traceIndex] = Math.abs(smp < vals.length ? vals[smp] : 0);
				
			}
			Arrays.sort(horizontalValues);
			float median = horizontalValues[horizontalValues.length*60/70];
			float principal95 = horizontalValues[horizontalValues.length*99/100];

			scale[0][smp] = median;
			scale[1][smp] = 100 / Math.max(0, principal95 - median);
			
		}
		
		return scale;
	}

	int nrm(int i, int max) {
		return Math.max(0, Math.min(max-1, i)  );
	}
}
