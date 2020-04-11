package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;

public interface Command {
		
	void execute(SgyFile file);
	
	String getButtonText();
	
	Change getChange();

}
