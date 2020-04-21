package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.ArrayBuilder;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.GHUtils;
import com.ugcs.gprvisualizer.math.ScanProfile;

public class RadarMapScan implements Command{

	Model model = AppContext.model;
	
	double [][]scaleArray;
	
	public RadarMapScan(ArrayBuilder builder) {
		scaleArray = builder.build();
	}
	
//	public void execute() {
//		for(SgyFile file : model.getFileManager().getFiles()) {
//			execute(file);
//		}
//		
//	}
	
	public void execute(SgyFile file) {

		if(file.amplScan == null) {
			file.amplScan = new ScanProfile(file.size());
		}		
		
		int start = GHUtils.norm(model.getSettings().layer, 0, model.getMaxHeightInSamples());
		int finish = GHUtils.norm(model.getSettings().layer + model.getSettings().hpage, 0, model.getMaxHeightInSamples());
		
		for(int i=0; i<file.size(); i++) {
			
			Trace trace = file.getTraces().get(i);
			
			double alpha = calcAlpha(trace.getNormValues(), trace.edge, start, finish);
			
			file.amplScan.intensity[i] = alpha;
		}		
		
	}

	private double calcAlpha(float[] values, int[] edge, int start, int finish) {
		double mx = 0;
		double threshold = scaleArray[0][start];
		double factor = scaleArray[1][start];

		start = GHUtils.norm(start, 0, values.length);
		finish = GHUtils.norm(finish, 0, values.length);
		
		for (int i = start; i < finish; i++) {
			if(edge[i] != 0 ) {
				mx = Math.max(mx, Math.abs(values[i]));
			}
		}

		double val = Math.max(0, mx - threshold) * factor;

		return Math.max(0, Math.min(val, 200));

	}

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
	
	
}
