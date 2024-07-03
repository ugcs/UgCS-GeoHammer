package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;

public class RemoveGroundLevel implements Command {

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		file.setGroundProfile(null);
	}

	@Override
	public String getButtonText() {

		return "Remove ground level";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}

}
