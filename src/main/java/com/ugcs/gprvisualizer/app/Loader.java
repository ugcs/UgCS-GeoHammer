package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ConstPointsFile;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MarkupFile;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;
import com.ugcs.gprvisualizer.math.ManuilovFilter;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Loader {

	private Model model;
	
	public Loader(Model model) {
		
		this.model = model;
	}
	
	public EventHandler<DragEvent> getDragHandler(){
		return dragHandler;
	}
	
	public EventHandler<DragEvent> getDropHandler(){
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
        	
        	final List<File> files = db.getFiles();
        	
        	ProgressTask loadTask = new ProgressTask() {
				@Override
				public void run(ProgressListener listener) {
					try {
					
						if(files.size() == 1 && files.get(0).getName().endsWith(".constPoints")) {
							
							ConstPointsFile cpf = new ConstPointsFile();
							cpf.load(files.get(0));
							
							for(SgyFile sgyFile : model.getFileManager().getFiles()) {
								cpf.calcVerticalCutNearestPoints(sgyFile);
							}
							
							model.updateAuxElements();
							
						}else {
							try {
								model.setLoading(true);
								load(files, listener);
								
								//when open file by dnd (not after save)
								model.initField();
								model.getVField().clear();
								
								AppContext.notifyAll(new WhatChanged(Change.fileopened));
								
								AppContext.statusBar.showProgressText("loaded " + model.getFileManager().getFiles().size() + " files");
							}finally {
								model.setLoading(false);
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
						
						MessageBoxHelper.showError("error opening files", "");
						
						model.getFileManager().getFiles().clear();
						model.updateAuxElements();
						model.initField();
						model.getVField().clear();
						
						
					}
				}        		
        	};
        	
			new TaskRunner(null, loadTask).start();
        	System.out.println("start completed");
        	
            event.setDropCompleted(true);
            event.consume();
        }

    };
    
	public void load(List<File> files, ProgressListener listener) {
		/// clear
		System.out.println("start load");
		
		model.getAuxElements().clear();
		model.getChanges().clear();
		
		listener.progressMsg("load");
		try {
			model.getFileManager().processList(files, listener);
		
			model.init();
			
			
			for(SgyFile sgyFile : model.getFileManager().getFiles()) {
				new ManuilovFilter().filter(sgyFile.getTraces());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			
			
		}
		
		
		
	}


	
}
