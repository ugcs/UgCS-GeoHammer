package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.ProgressListener;

public class RemoveGroundLevel implements Command {

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		file.setGroundProfile(null);
	}

	@Override
	public String getButtonText() {

		return "Remove ground level";
	}

}
