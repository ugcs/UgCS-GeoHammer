package com.ugcs.gprvisualizer.gpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.Sout;

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
		
		double[][] underconstruction = new double[2][model.getMaxHeightInSamples()];
		
		for(int smp=0; smp<model.getMaxHeightInSamples(); smp++) {

			//all edge values of sample layer
			List<Float> all = new ArrayList<>();
			
			for(int traceIndex=0; traceIndex<traces.size(); traceIndex++) {
				Trace trace = traces.get(traceIndex);
				
				float[] vals = trace.getNormValues();
				
				if(trace.edge[smp] >= 3) {
					all.add(Math.abs(smp < vals.length ? vals[smp] : 0));
				}
				
				//horizontalValues[traceIndex] = Math.abs(smp < vals.length ? vals[smp] : 0);
				
			}
			if(all.isEmpty()) {
				underconstruction[0][smp] = 0;
				underconstruction[1][smp] = 100 / 1000;
			}else {
				Collections.sort(all);
				float median = all.get(all.size()*35/70);
				float principal95 = all.get(all.size()*98/100);
	
				//threshold
				underconstruction[0][smp] = median;
				//kf
				underconstruction[1][smp] = 100 / Math.max(0.5, principal95 - median);
				
				
				//Sout.p(" autogain sc " + smp + "   " + underconstruction[0][smp] + "   " + underconstruction[1][smp] +
				//		"   alsize " + all.size() + "   prncpl95 " + principal95);
				
			}

			

		}
		
		
		
		
		scale = underconstruction;
		
		return scale;
	}

	int nrm(int i, int max) {
		return Math.max(0, Math.min(max-1, i)  );
	}
}
