package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;

public class LevelGround implements Command {

	@Override
	public void execute(SgyFile file) {

		int minlev = file.getTraces().get(0).maxindex;
		int maxlev = file.getTraces().get(0).maxindex;
		
		for(Trace trace : file.getTraces()) {
			minlev = Math.min(minlev, trace.maxindex);
			maxlev = Math.max(maxlev, trace.maxindex);
		}
		
		int level = (minlev+maxlev)/2;
		
		for (Trace trace : file.getTraces()) {

			float values[] = trace.getNormValues();
			float n_values[] = new float[values.length];
			int src_start = Math.max(0, trace.maxindex-level);
			int dst_start = Math.max(0, level-trace.maxindex);
			
			System.arraycopy(
				values, src_start, 
				n_values, dst_start, 
				values.length - Math.abs(trace.maxindex-level));
			
			
			trace.setNormValues(n_values);
			trace.verticalOffset = level-trace.maxindex;
			trace.maxindex = 0;
			
		}
		
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
