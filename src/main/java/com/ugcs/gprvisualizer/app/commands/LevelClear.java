package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.LevelFilter;

public class LevelClear implements Command {

	private final LevelFilter levelFilter;

	public LevelClear(LevelFilter levelFilter) {
		this.levelFilter = levelFilter;
	}

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		if (levelFilter.getUndoFiles() == null) {
			return;
		}

		SgyFile undoFile = levelFilter.getUndoFiles().get(0);

		file.setTraces(undoFile.getTraces());
		levelFilter.setUndoFiles(null);

		file.setGroundProfile(undoFile.getGroundProfile());
	}

	@Override
	public String getButtonText() {
		return "Undo flattening";
		//return "X";
	}

	@Override
	public Change getChange() {
		return Change.traceValues;
	}

}
