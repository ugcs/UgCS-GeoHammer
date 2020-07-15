package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class LevelManualSetter implements Command {

	private Model model = AppContext.model;
	
	@Override
	public String getButtonText() {		
		return "Set ground level";
	}

	@Override
	public Change getChange() {
		return Change.traceValues;
	}

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		
		HorizontalProfile levelProfile = new HorizontalProfile(file.size());
		
		int level = model.getSettings().layer;
		
		for (int i = 0; i < file.size(); i++) {
			levelProfile.deep[i] = level;
		}
		
		levelProfile.finish(file.getTraces());
		
		file.groundProfile = levelProfile;
		
	}

}
