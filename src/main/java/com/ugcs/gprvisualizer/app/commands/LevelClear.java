package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;

public class LevelClear implements Command {

	@Override
	public void execute(SgyFile file) {
		
		for(Trace trace : file.getTraces()) {
			trace.maxindex = 0;
		}
		
	}

	@Override
	public String getButtonText() {

		return "X";
	}

	@Override
	public Change getChange() {
		
		return Change.justdraw;
	}

}
