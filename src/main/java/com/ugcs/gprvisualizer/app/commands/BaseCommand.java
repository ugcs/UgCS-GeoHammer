package com.ugcs.gprvisualizer.app.commands;

import com.ugcs.gprvisualizer.draw.Change;

public interface BaseCommand {

	String getButtonText();
	
	Change getChange();

}
