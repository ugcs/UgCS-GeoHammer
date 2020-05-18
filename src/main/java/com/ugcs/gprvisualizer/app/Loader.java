package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ConstPointsFile;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

@Component
public class Loader {

	@Autowired
	private Model model;
	
	@Autowired
	private Status status; 
	
	@Autowired
	private Broadcast broadcast;
	
	public Loader() {		
		
	}
	
	public EventHandler<DragEvent> getDragHandler() {
		return dragHandler;
	}
	
	public EventHandler<DragEvent> getDropHandler() {
		return dropHandler;
	}
	
	private EventHandler<DragEvent> dragHandler = new EventHandler<DragEvent>() {

        @Override
        public void handle(DragEvent event) {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        }
    };
    
    private EventHandler<DragEvent> dropHandler = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
        	
        	Dragboard db = event.getDragboard();
        	if (!db.hasFiles()) {
        		return;
        	}
        	
        	if (model.stopUnsaved()) {
        		return;
        	}        	
        	
        	
        	final List<File> files = db.getFiles();
        	
        	ProgressTask loadTask = new ProgressTask() {
				@Override
				public void run(ProgressListener listener) {
					try {
					
						if (isConstPointsFile(files)) {
							
							ConstPointsFile cpf = new ConstPointsFile();
							cpf.load(files.get(0));
							
							for (SgyFile sgyFile : 
								model.getFileManager().getFiles()) {
								
								cpf.calcVerticalCutNearestPoints(
										sgyFile);
							}
							
							model.updateAuxElements();
							
						} else {
							
							loadWithNotify(files, listener);
							
						}
					} catch (Exception e) {
						e.printStackTrace();
						
						MessageBoxHelper.showError(
							"Can`t open files", 
							"Probably file has incorrect format");
						
						model.getFileManager().getFiles().clear();
						model.updateAuxElements();
						model.initField();
						model.getVField().clear();
						
						
						broadcast.notifyAll(
								new WhatChanged(Change.fileopened));
					}
				}
        	};
        	
			new TaskRunner(status, loadTask).start();
        	System.out.println("start completed");
        	
            event.setDropCompleted(true);
            event.consume();
        }

    };

    
	public void loadWithNotify(final List<File> files, ProgressListener listener) 
			throws Exception {
		
		load(files, listener);
		
		model.getVField().clear();
		
		broadcast.notifyAll(new WhatChanged(Change.fileopened));
	}

    
	public void load(final List<File> files, ProgressListener listener) 
			throws Exception {
		try {
			model.setLoading(true);
			
			load2(files, listener);
			
			
		} finally {
			model.setLoading(false);
		}
		
		status.showProgressText("loaded " 
				+ model.getFileManager().getFiles().size() + " files");
	}        		
    
	public void load2(List<File> files, ProgressListener listener) throws Exception {
		/// clear
		model.getAuxElements().clear();
		model.getChanges().clear();
		
		listener.progressMsg("load");

		model.getFileManager().processList(files, listener);
	
		model.init();			
		
		//when open file by dnd (not after save)
		model.initField();		
	}

	private boolean isConstPointsFile(final List<File> files) {
		return files.size() == 1 
				&& files.get(0).getName().endsWith(".constPoints");
	}

	
}
