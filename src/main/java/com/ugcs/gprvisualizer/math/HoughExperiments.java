package com.ugcs.gprvisualizer.math;

import java.util.HashSet;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;

public class HoughExperiments {

	
	private SgyFile file;
	private int traceFrom;
	private int traceTo;
	
	private int smpFrom;
	private int smpTo;
	
	private int tracePin;
	private int smpPin;	
	private double xf;
	
	//1 stacking so dist in traces = dist in samples
	
	 
	static public HoughExperiments f(int tr, int smp, double xf) {
		HoughExperiments he = new HoughExperiments();
		
		//todo:
		
		
		
		return he;
	}
	
	int[] leftStart;
	int[] leftFinish;
	int[] rightStart;
	int[] rightFinish;
	
	//0 - no, 1 - yes
	int[] columnExists;

	//getRealSizeTraces()
	//getRealSizeRelative()
/*	
	public void addPoint(int tr, int smp) {
		count++;
		if (inside()) {
			columnExists[tr] = 1;
		}
	}	
	*/
}
