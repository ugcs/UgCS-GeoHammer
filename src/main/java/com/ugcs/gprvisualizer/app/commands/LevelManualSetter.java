package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class LevelManualSetter implements Command {

	private Model model = AppContext.model;
	
	@Override
	public String getButtonText() {		
		return "Set ground level";
	}

	@Override
	public WhatChanged.Change getChange() {
		return WhatChanged.Change.traceValues;
	}

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		
		HorizontalProfile levelProfile = new HorizontalProfile(file.size());
		
		int level = model.getProfileField().getProfileSettings().getLayer();
		
		for (int i = 0; i < file.size(); i++) {
			levelProfile.deep[i] = level;
		}
		
		levelProfile.finish(file.getTraces());
		
		file.setGroundProfile(levelProfile);		
	}

}
