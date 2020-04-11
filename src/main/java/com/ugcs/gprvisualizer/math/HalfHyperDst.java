package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;

public class HalfHyperDst {
	
	//static final double THRESHOLD = 0.76;

	
	double sampleToCm_air;
	double sampleToCm_grn;
	
	Trace trace;
	double example;
	int pinnacle_tr;
	int pinnacle_smp;
	
	// +1 | -1
	int side;
	
	int length;
	int smp[] = new int[300];
	
	public boolean isGood(List<Trace> traces, double thr) {
		
		double wei = analize(traces);		
		
		return wei > thr;
	}
	
	
	public double analize(List<Trace> traces) {
		
		int sum[] = new int[5];
		
		for(int i=0; i< length;i++) {
			int index = pinnacle_tr + side * i;
			Trace trace = traces.get(index);
			
			int s = smp[i];

			if(trace.edge == null) {
				continue;
			}
			for(int j=s-1; j<=s+1; j++) {
				sum[trace.edge[j]]++;
			}
		}
		
		int max = Arrays.stream(sum, 1, 5).max().getAsInt();
		
		
		
		return (double)max / (double)length;
	}
	
	public double[] smpToDst(int smp) {
		
		double air_smp = Math.min(trace.maxindex, smp);
		double grn_smp = smp - air_smp;
		
		double air_dst = air_smp * sampleToCm_air;
		double grn_dst = grn_smp * sampleToCm_grn;
		
		return new double[]{air_dst, grn_dst};
	}
	
	public double getGoodSideDst(int smp) {
		
				
		double r[] = smpToDst(smp);
		double y_cm = r[0] + r[1];
		
		
		return (y_cm * 0.43);
	}
	

//	public double samplToCmKf() {
//		return sampleInCm * AppContext.model.getSettings().hyperkfc/100.0;
//	}
	
	public static HalfHyperDst getHalfHyper(SgyFile sgyFile, int tr, int org_smp, int side) {
		
		List<Trace> traces = sgyFile.getTraces();
		Trace trace = traces.get(tr);
		int maxSamplIndex = trace.getNormValues().length-2;
		
		HalfHyperDst hh = new HalfHyperDst();
		hh.trace = trace;
		hh.pinnacle_tr = tr;		
		hh.pinnacle_smp = org_smp;
		hh.side = side;
		hh.sampleToCm_air = sgyFile.getSamplesToCmAir();
		hh.sampleToCm_grn = sgyFile.getSamplesToCmGrn();
		
		//double kf = hh.samplToCmKf();		
		int i=0;
		int index=0;
		double hypergoodsize = hh.getGoodSideDst(hh.pinnacle_smp);
		
		//double y = hh.pinnacle_smp - trace.verticalOffset;
		double r[] = hh.smpToDst(hh.pinnacle_smp);
		double y_cm = r[0] + r[1];
		double x = 0;
		
		while(Math.abs(x) < hypergoodsize && i < 300) {
			index = tr + side * i;
			if(index<0 || index>= traces.size() ) {
				break;
			}

			x += getXStep(side, traces, i, index);
			
			
			double c_cm = Math.sqrt(x*x + y_cm*y_cm);
			//
			double f = r[0] / y_cm;
			double c_air_cm = c_cm * f; 
			double c_grn_cm = c_cm * (1-f);
			//
			double  c = c_air_cm / hh.sampleToCm_air + c_grn_cm / hh.sampleToCm_grn;
			
			int smp = (int)c;
			if(smp >= maxSamplIndex) {
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


	public static double getXStep(int side, List<Trace> traces, int i, int index) {
		double xstep=0;
		if(i>0) {
			if(side > 0) {
				//dist to cm
				xstep = traces.get(index).getPrevDist() * 100;
			}else {
				xstep = traces.get(index+1).getPrevDist()* 100;
			}
		}
		return xstep;
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
