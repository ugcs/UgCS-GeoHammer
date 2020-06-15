package com.ugcs.gprvisualizer.app.commands;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.Broadcast;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.ProgressTask;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.TaskRunner;
import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

@Component
public class CommandRegistry {
	
	@Autowired
	private Model model;

	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private Status status; 
	
	private static final ProgressListener emptyListener = new ProgressListener() {
		
		@Override
		public void progressPercent(int percent) {
			
		}
		
		@Override
		public void progressMsg(String msg) {
			
		}
	};
	
	public void runForFiles(Command command) {
		
		runForFiles(command, emptyListener);
	}
	
	public void runForFiles(Command command, ProgressListener listener) {
	
		for (SgyFile sgyFile : model.getFileManager().getFiles()) {
			
			try {
				listener.progressMsg("process file '" 
						+ sgyFile.getFile().getName() + "'");
				
				command.execute(sgyFile);
				
			} catch (Exception e) {
				e.printStackTrace();
				
				listener.progressMsg("error");
			}
		}
		
		Change ch = command.getChange();
		if (ch != null) {
			AppContext.notifyAll(new WhatChanged(ch));
		}
		
		listener.progressMsg("process finished '" + command.getButtonText() + "'");
		
		//System.out.println("finished command " + command.getButtonText() );
	}
	
	
	public Button createAsinqTaskButton(AsinqCommand command) {
		return createAsinqTaskButton(command, null);
	}
	
	public Button createAsinqTaskButton(AsinqCommand command, Consumer<Object> finish) {
		
		Button button = new Button(command.getButtonText());
		
		ProgressTask task = new ProgressTask() {
			
			@Override
			public void run(ProgressListener listener) {
				runForFiles(command, listener);				
				
				if (finish != null) {
					finish.accept(null);
				}
			}
		};
		
		button.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				
				new TaskRunner(status, task).start();
		    	
		    }
		});
		
		return button;
	}

	public Button createAsinqTaskButton(SingleCommand command, Consumer<Object> finish) {
		
		Button button = new Button(command.getButtonText());
		
		ProgressTask task = new ProgressTask() {
			
			@Override
			public void run(ProgressListener listener) {
				try {
					listener.progressMsg("start processing");
					
                    command.execute(AppContext.model.getFileManager().getFiles(),
							listener);
					
				} catch (Exception e) {
					e.printStackTrace();
					
					listener.progressMsg("error");
				}
				
				Change ch = command.getChange();
				if (ch != null) {
					broadcast.notifyAll(new WhatChanged(ch));
				}
				
				listener.progressMsg("process finished '" 
						+ command.getButtonText() + "'");
					
				if (finish != null) {
					finish.accept(null);
				}
			}
		};
		
		button.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				
				new TaskRunner(status, task).start();
		    	
		    }
		});
		
		return button;
	}
	

	public Button createButton(Command command) {
		return createButton(command, null);
	}
	
	public Button createButton(Command command, Consumer<Object> finish) {
		Button button = new Button(command.getButtonText());
		
		button.setOnAction(e -> {
			runForFiles(command);
			
			if (finish != null) {
				finish.accept(null);
			}
		});
		
		return button;
	}
	
	public static Button createButton(String title, EventHandler<ActionEvent> action) {
		
		Button button = new Button(title);
		button.setOnAction(action);
		
		return button;
	}
	
	public static Button createButton(String title, Node img, String tooltip, EventHandler<ActionEvent> action) {
		
		Button button = new Button(title, img);
		button.setTooltip(new Tooltip(tooltip));
		button.setOnAction(action);
		
		return button;
	}
	
	
}
