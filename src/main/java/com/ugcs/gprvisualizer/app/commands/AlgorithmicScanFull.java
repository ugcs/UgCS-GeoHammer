package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;

public class AlgorithmicScanFull implements AsinqCommand {

	@Override
	public void execute(SgyFile file) {

		if(file.groundProfile == null) {
			new LevelScanHP().execute(file);
		}
		
		new EdgeFinder().execute(file);
		
		new EdgeSubtractGround().execute(file);		
		
		new AlgorithmicScan().execute(file);
	}

	@Override
	public String getButtonText() {
		
		return "Algorithmic scan";
	}

	@Override
	public Change getChange() {

		return Change.adjusting;
	}

}
