package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.List;

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
			        load(files, listener);
				}        		
        	};
        	
			new TaskRunner(null, loadTask).start();
        	
            event.setDropCompleted(true);
            event.consume();
        }

    };
    
	public void load(List<File> files, ProgressListener listener) {
		/// clear
		
		model.getFoundIndexes().clear();
		model.getFoundTrace().clear();
		model.getChanges().clear();
		//model.getSettings().
		///
		
		listener.progressMsg("load");
		try {
			model.getFileManager().processList(files, listener);
		
			initField();
			System.out.println("===initField() " + model.getField().getPathCenter());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//
		for(SgyFile sgyFile : model.getFileManager().getFiles()) {
			try {
				new MarkupFile().load(sgyFile, model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		

		//set index of traces
		for(int i=0; i<model.getFileManager().getTraces().size(); i++ ) {
			model.getFileManager().getTraces().get(i).indexInSet = i;
		}
		
		model.updateAuxElements();

		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				AppContext.notifyAll(new WhatChanged(Change.fileopened));
			}
		});
		
	}

	private void initField() {
		// center
		MinMaxAvg lonMid = new MinMaxAvg();
		MinMaxAvg latMid = new MinMaxAvg();
		for (Trace trace : model.getFileManager().getTraces()) {
			if(trace == null || trace.getLatLon() == null) {
				System.out.println("null");
				continue;
			}
			
			latMid.put(trace.getLatLon().getLatDgr());
			lonMid.put(trace.getLatLon().getLonDgr());
		}
		
		  
		model.getField().setPathCenter(new LatLon(latMid.getMid(), lonMid.getMid()));

		model.getField().setSceneCenter(new LatLon(latMid.getMid(), lonMid.getMid()));
		
		model.getField().setZoom(18);
	}
    
	
}
