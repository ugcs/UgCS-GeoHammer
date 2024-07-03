package com.ugcs.gprvisualizer.math;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.commands.Command;
import com.ugcs.gprvisualizer.draw.Change;

public class ScanGood implements Command {

	@Override
	public String getButtonText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Change getChange() {
		// TODO Auto-generated method stub
		return null;
	}

	class TraceResult {
		int goodCount;
		double maxAmp;
	}
	
	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		
		// | ___ |
		
		int fromSmp = AppContext.model.getSettings().getLayer();
		int toSmp = Math.min(AppContext.model.getSettings().getLayer() 
				+ AppContext.model.getSettings().hpage,
				file.getMaxSamples() - 2);
		
		//int[] goodCount = new int[file.size()];
		//double[] maxAmp = new double[file.size()];
		
		ScanProfile scanProfile = 
				new ScanProfile(file.size(), true);
		
		TraceResult resHld = new TraceResult();
		
		for (int tr = 0; tr < file.size(); tr++) {
		
			
			
			scanTraceGood(file, tr, resHld, fromSmp, toSmp);
		
			if (resHld.goodCount > 0) {
				
				int radius = (int) (12 
						+ 7.0 * Math.log((double) resHld.goodCount));
				
				scanProfile.radius[tr] = radius;
				
				scanProfile.intensity[tr] = resHld.maxAmp / 25.0;
			}
		}		
		
		file.algoScan = scanProfile;

	}

	private void scanTraceGood(SgyFile file, int tr, TraceResult resHld, int fromSmp, int toSmp) {
		resHld.goodCount = 0;
		resHld.maxAmp = 0;
		Trace trace = file.getTraces().get(tr);
		for (int smp = fromSmp; smp < toSmp; smp++) {
			
			//dblside good hyperbola
			if (trace.good[smp] == 3) {
				
				resHld.goodCount++;
				
				float val = 0; 
//				if (trace.edge[smp] >= 3) {
//					val = Math.abs(trace.getNormValues()[smp]);
//				} else {
				for (int tri = Math.max(0, tr-TR); 
					tri <= Math.min(file.size(), tr+TR); 
					tri++) {
					
					val = Math.max(val, getMaxAround(file.getTraces().get(tri).getNormValues(), smp, toSmp));
				}
				
				resHld.maxAmp = Math.max(resHld.maxAmp, val);
			}			
		}
		
	}

	private static final int R = 4;
	private static final int TR = 4;
	
	private float getMaxAround(float[] values, int smp, int maxSmp) {

		int smpFrom = Math.max(0, smp - R);
		int smpTo = Math.min(maxSmp, smp + R);
		float max = 0;
		for (int i = smpFrom; i <= smpTo; i++) {
			max = Math.max(max, Math.abs(values[i]));
		}
		
		return max;
	}

//	public static void main(String[] args) {
//		for (double i = 1; i < 50; i++) {
//			Sout.p(i + " " + Math.log(i));
//		}
//	}
	
}
