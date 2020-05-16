package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;

public interface Command extends BaseCommand {
		
	void execute(SgyFile file);

}
