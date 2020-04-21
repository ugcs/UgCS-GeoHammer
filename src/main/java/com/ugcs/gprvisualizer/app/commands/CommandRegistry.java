package com.ugcs.gprvisualizer.app.commands;

import java.util.function.Consumer;
import java.util.function.Function;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.ProgressTask;
import com.ugcs.gprvisualizer.app.TaskRunner;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class CommandRegistry {
	
	private static final ProgressListener emptyListener = new ProgressListener() {
		
		@Override
		public void progressPercent(int percent) {
			
		}
		
		@Override
		public void progressMsg(String msg) {
			
		}
	} ;
	
	public static void runForFiles(Command command) {
		
		runForFiles(command, emptyListener);
	}
	
	public static void runForFiles(Command command, ProgressListener listener) {
		System.out.println("runForFiles command " + command.getButtonText() );
		
		for(SgyFile sgyFile : AppContext.model.getFileManager().getFiles()) {
			
			try {
				listener.progressMsg("process file '" + sgyFile.getFile().getName() + "'");
				
				command.execute(sgyFile);
				
			}catch(Exception e) {
				e.printStackTrace();
				
				listener.progressMsg("error");
			}
		}
		
		Change ch = command.getChange();
		if(ch != null) {
			AppContext.notifyAll(new WhatChanged(ch));
		}
		
		listener.progressMsg("process finished '" + command.getButtonText() + "'");
		
		System.out.println("finished command " + command.getButtonText() );
	}
	
	
	public static Button createAsinqTaskButton(AsinqCommand command) {
		
		Button button = new Button(command.getButtonText());
		
		ProgressTask task = new ProgressTask() {
			
			@Override
			public void run(ProgressListener listener) {
				runForFiles(command, listener);				
			}
		};
		
		button.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				
				new TaskRunner(null, task).start();
		    	
		    }
		});
		
		return button;
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
			
						
			
		});
		
		return button;
	}
	
	public static Button createButton(String title, EventHandler<ActionEvent> action) {
		
		Button button = new Button(title);
		button.setOnAction( action);
		
		return button;
	}
	
	
	
}
