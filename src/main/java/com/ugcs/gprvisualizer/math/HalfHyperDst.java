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
	
	SgyFile sgyFile;
	
	//int traceIndex;
	//Trace trace;
	double example;
	int pinnacle_tr;
	int pinnacle_smp;
	
	// +1 | -1
	int side;
	
	int length;
	int smp[] = new int[300];
	double hypergoodsize;
	
	boolean defective = false;
	
	public double analize(int percent) {

		if(sgyFile.getTraces().get(0).edge == null) {
			System.out.println("!!!! edge not prepared");
			return 0;
		}

		
		return Math.max(analize(percent, true), analize(percent, false));
	}
	
	public double analize(int percent, boolean higher) {
		if(defective ) {
			return 0;
		}
		//
		int from = higher ? -1 : 0;
		int to = higher ? 0 : 1;
		
		//
		List<Trace> traces = sgyFile.getTraces();
		
		int sum[] = new int[5];
		
		int checked_length = length * percent / 100;
		for(int i=0; i< checked_length;i++) {
			int index = pinnacle_tr + side * i;
			Trace trace = traces.get(index);
			
			int s = smp[i];

			for(int j=s-from; j<=s+to; j++) {
				sum[trace.edge[j]]++;
			}
		}
		
		// find max edge count (0 is not edge so don`t take it into account)
		int max = Arrays.stream(sum, 1, 5).max().getAsInt();
		
		
		return (double)max / (double)checked_length;
	}
	
	public double[] smpToDst(int smp) {
		
		int grnd = sgyFile.groundProfile != null ? sgyFile.groundProfile.deep[pinnacle_tr] : 0;
		
		double air_smp = Math.min(grnd, smp);
		double grn_smp = smp - air_smp;
		
		double air_dst = air_smp * sampleToCm_air;
		double grn_dst = grn_smp * sampleToCm_grn;
		
		return new double[]{air_dst, grn_dst};
	}
	
	public double getGoodSideDst(int smp) {
		
				
		double r[] = smpToDst(smp);
		double y_cm = r[0] + r[1];
		
		
		return (y_cm * 0.41);
	}
	

//	public double samplToCmKf() {
//		return sampleInCm * AppContext.model.getSettings().hyperkfc/100.0;
//	}
	
	public static HalfHyperDst getHalfHyper(SgyFile sgyFile, int tr, int org_smp, int side, double x_factor) {
		
		List<Trace> traces = sgyFile.getTraces();
		Trace trace = traces.get(tr);
		int maxSamplIndex = trace.getNormValues().length-2;
		
		HalfHyperDst hh = new HalfHyperDst();
		hh.sgyFile = sgyFile;
		hh.pinnacle_tr = tr;		
		hh.pinnacle_smp = org_smp;
		hh.side = side;
		hh.sampleToCm_air = sgyFile.getSamplesToCmAir();
		hh.sampleToCm_grn = sgyFile.getSamplesToCmGrn();
		
		//reduce size not so intensive 
		double goodsizefactor = ( 1+(x_factor-1)/2  );
		hh.hypergoodsize = hh.getGoodSideDst(hh.pinnacle_smp) / goodsizefactor;
		
		int i=0;
		int index=0;
		double r[] = hh.smpToDst(hh.pinnacle_smp);
		double y_cm = r[0] + r[1];
		double x = 0;
		
		while(Math.abs(x) < hh.hypergoodsize && i < 300) {
			index = tr + side * i;
			if(index<0 || index>= traces.size() ) {
				hh.defective = true;
				break;
			}

			x += getXStep(side, traces, i, index) * x_factor;
			
			
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

	
}
