package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.AmplitudeMatrix;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;

/**
 * Find ground level. Put ground value to trace.maxindex
 * @author Kesha
 *
 */
public class LevelScanner implements Command {

	@Override
	public void execute(SgyFile file) {
		
		List<Trace> lst = file.getTraces();

		AmplitudeMatrix am = new AmplitudeMatrix();
		am.init(lst);
		file.groundProfile = am.findLevel();
	}

	@Override
	public String getButtonText() {

		return "Find ground level";
	}

	@Override
	public Change getChange() {
		return Change.traceValues;
	}

	
	
}
