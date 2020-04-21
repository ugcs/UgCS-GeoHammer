package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class LevelGround implements Command {

	@Override
	public void execute(SgyFile file) {

//		int minlev = file.getTraces().get(0).maxindex;
//		int maxlev = file.getTraces().get(0).maxindex;
//		
//		for(Trace trace : file.getTraces()) {
//			minlev = Math.min(minlev, trace.maxindex);
//			maxlev = Math.max(maxlev, trace.maxindex);
//		}
		
//		int level = (minlev+maxlev)/2;
		
		HorizontalProfile hp = file.groundProfile;
		int level = (file.groundProfile.minDeep + file.groundProfile.maxDeep)/2;
		
		for (int i=0; i<file.getTraces().size(); i++ ) {
			
			Trace trace = file.getTraces().get(i);
			

			float values[] = trace.getNormValues();
			float n_values[] = new float[values.length];
			int src_start = Math.max(0, hp.deep[i] -level);
			int dst_start = Math.max(0, level-hp.deep[i]);
			
			System.arraycopy(
				values, src_start, 
				n_values, dst_start, 
				values.length - Math.abs(hp.deep[i]-level));
			
			
			trace.setNormValues(n_values);
			trace.verticalOffset = level-hp.deep[i];
			
		}
		file.groundProfile = null;
		file.setUnsaved(true);
	}

	@Override
	public String getButtonText() {

		return "Level ground";
	}

	@Override
	public Change getChange() {

		return Change.traceValues;
	}

}
