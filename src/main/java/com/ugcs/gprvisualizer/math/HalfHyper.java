package com.ugcs.gprvisualizer.math;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;

public class HalfHyper {
	
	static final double THRESHOLD = 0.76;
	
	double example;
	int pinnacle_tr;
	int pinnacle_smp;
	
	// +1 | -1
	int side;
	
	int length;
	int smp[] = new int[300];
	
	
	double oppositeAbovePerc;
	double oppositeBelowPerc;
	
	boolean isGood() {
		
		double mx = Math.max(oppositeAbovePerc, oppositeBelowPerc);
		double mn = Math.min(oppositeAbovePerc, oppositeBelowPerc);
		
		return 
			mx > 0.36 ||
			mx > 0.29 && mn > 0.05 ||
			mx > 0.19 && mn > 0.1 ||
			mx > 0.13 && mn > 0.13;
	}
	
	
	public static int getGoodSideSize(int smp) {
		double kf = AppContext.model.getSettings().hyperkfc/100.0;
		
		return (int)((double)smp * 0.43 / kf);
	}
	

	
	public static HalfHyper getHalfHyper(List<Trace> traces, int tr, int org_smp, float example, int side, double hyperkf) {
		
		
		
		HalfHyper hh = new HalfHyper();
		hh.pinnacle_tr = tr;
		
		hh.pinnacle_smp = org_smp;
		hh.side = side;
		hh.example = example;
		
		double kf = hyperkf;		
		int i=0;
		double bad = 0;
		int index=0;
		
		Trace trace = traces.get(tr);
		int hypergoodsize = getGoodSideSize(hh.pinnacle_smp - trace.verticalOffset);//model.getSettings().hypergoodsize;
		double y = hh.pinnacle_smp - trace.verticalOffset;
		
		while(i<hypergoodsize+1 && bad < 0.2 ) {
			index = tr + side * i;
			if(index<0 || index>= traces.size() ) {
				break;
			}

			double x = i * kf;
			double c = Math.sqrt(x*x+y*y);
			
			float values[] = traces.get(index).getNormValues();
			
			int smp = (int)c + trace.verticalOffset;
			if(smp >= values.length) {
				break;
			}
			float val = values[smp];
			
			if(!similar(example, val)) {
				bad += Math.abs(val/example);
			}
			
			hh.smp[i] = smp;
			hh.length=i;
			i++;
			
		}
		
		double x = (index-tr) * kf;
		double c = Math.sqrt(x*x+y*y);		
		//return new TraceSample(index, (int)c);
		
		
		
		if(hh.length >= hypergoodsize) {
			updateAroundOpposite(traces, hh);
		}
		
		return hh;
	}

	private static void updateAroundOpposite(List<Trace> traces, HalfHyper hh) {
		double example = traces.get(hh.pinnacle_tr).getNormValues()[hh.pinnacle_smp];
		
		int above=0;
		int below=0;
		int mid = hh.length/2;
		for(int i=0 ; i < hh.length; i++){
			int checked_tr = hh.pinnacle_tr + i * hh.side; 
			float []values = traces.get(checked_tr).getNormValues();
			
			int smp = hh.smp[i];
			
			if(i>mid) {
				above += lookingForOpposite(values, smp, example, -1);
			}
			if(i<mid) {
				below += lookingForOpposite(values, smp, example, +1);
			}
		}
		
		hh.oppositeAbovePerc = (double)above/(double)hh.length;
		hh.oppositeBelowPerc = (double)below/(double)hh.length;
	}
	
	
	
	static private boolean similar(float example, float val) {
		
		return (example > 0) == (val > 0);
	}

	static private int lookingForOpposite(float[] values, int smpstart, double example, int side) {
		
		boolean positive = example > 0;
		
		for(int smp=1; smp< 3; smp++) {
			float val = values[smpstart + side*smp];
			
			if(val > 0 != positive) {
				return 1;
			}			
		}		
		return 0;
	}
	
	
	
}
