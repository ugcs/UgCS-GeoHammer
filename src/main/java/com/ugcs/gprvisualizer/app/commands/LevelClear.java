package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;

public class LevelClear implements Command {

	@Override
	public void execute(SgyFile file) {
		
		file.groundProfile = null;
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
