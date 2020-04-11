package com.ugcs.gprvisualizer.app.commands;

import java.util.function.Consumer;
import java.util.function.Function;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class CommandRegistry {
	

	public static void runForFiles(Command command) {
		
		for(SgyFile sgyFile : AppContext.model.getFileManager().getFiles()) {
			command.execute(sgyFile);
		}
		
	}

	public static Button createButton(Command command) {
		return createButton(command, null);
	}
	
	public static Button createButton(Command command, Consumer finish) {
		Button button = new Button(command.getButtonText());
		
		button.setOnAction(e -> {
			runForFiles(command);
			
			if(finish != null) {
				finish.accept(null);
			}
			
			AppContext.notifyAll(new WhatChanged(command.getChange()));			
			
		});
		
		return button;
	}
	
	public static Button createButton(String title, EventHandler<ActionEvent> action) {
		
		Button button = new Button(title);
		button.setOnAction( action);
		
		return button;
	}
	
	
	
}
