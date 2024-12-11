package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.event.WhatChanged;

public class AlgorithmicScanFull implements AsinqCommand {

	@Override
	public void execute(SgyFile file, ProgressListener listener) {

		if (file.getGroundProfile() == null) {
			//new LevelScanHP().execute(file);
			new LevelScanner().execute(file, listener);
		}
		
		new EdgeFinder().execute(file, listener);
		
		new EdgeSubtractGround().execute(file, listener);		
		
		new AlgorithmicScan().execute(file, listener);
	}

	@Override
	public String getButtonText() {
		return "Algorithmic scan";
	}

	@Override
	public WhatChanged.Change getChange() {
		return WhatChanged.Change.traceValues;
	}

}
