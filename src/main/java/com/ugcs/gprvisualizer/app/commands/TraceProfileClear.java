package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;

public class TraceProfileClear implements Command {

	@Override
	public void execute(SgyFile file) {
		
		file.profiles = null;	    

		
	}

	@Override
	public String getButtonText() {

		return "Clear";
	}

	@Override
	public Change getChange() {
		
		return Change.justdraw;
	}

}
