package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;

public class HalfHyperDst {
	
	static final double THRESHOLD = 0.76;
	
	double example;
	int pinnacle_tr;
	int pinnacle_smp;
	
	// +1 | -1
	int side;
	
	int length;
	int smp[] = new int[300];
	
	boolean isGood(List<Trace> traces, double thr) {
		
		double wei = analize(traces);		
		
		return wei > thr;
	}
	
	
	public double analize(List<Trace> traces) {
		
		int sum[] = new int[5];
		
		for(int i=0; i< length;i++) {
			int index = pinnacle_tr + side * i;
			Trace trace = traces.get(index);
			
			int s = smp[i];
			
			for(int j=s-1; j<=s+1; j++) {
				sum[trace.edge[j]]++;
			}
		}
		
		int max = Arrays.stream(sum, 1, 5).max().getAsInt();
		
		
		
		return (double)max / (double)length;
	}
	
	
	public static double getGoodSideDst(int smp) {
		//double kf = AppContext.model.getSettings().hyperkfc/100.0;
		
		return ((double)smp * 0.43);
	}
	

	
	public static HalfHyperDst getHalfHyper(List<Trace> traces, int tr, int org_smp, int side) {
		
		Trace trace = traces.get(tr);
		
		HalfHyperDst hh = new HalfHyperDst();
		hh.pinnacle_tr = tr;		
		hh.pinnacle_smp = org_smp;
		hh.side = side;
		
		//double kf = hyperkf;		
		int i=0;
		int index=0;
		double hypergoodsize = getGoodSideDst(hh.pinnacle_smp - trace.verticalOffset);
		
		double y = hh.pinnacle_smp - trace.verticalOffset;
		double x = 0;
		
		while(Math.abs(x) < hypergoodsize && i < 300) {
			index = tr + side * i;
			if(index<0 || index>= traces.size() ) {
				break;
			}

			Trace currentTrace = traces.get(index);
			
			if(i>0) {
				if(side > 0) {
					//dist to cm
					x += currentTrace.getPrevDist() * 100;
				}else {
					x += traces.get(index+1).getPrevDist()* 100;
				}
			}
			
			//double x = i * kf;
			double c = Math.sqrt(x*x+y*y);
			
			float values[] = currentTrace.getNormValues();
			
			int smp = (int)c + trace.verticalOffset;
			if(smp >= values.length-2) {
				break;
			}
			hh.smp[i] = smp;
			hh.length=i;
			i++;
			
		}
		
//		double x = (index-tr) * kf;
//		double c = Math.sqrt(x*x+y*y);		
		//return new TraceSample(index, (int)c);
		
		
		return hh;
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
