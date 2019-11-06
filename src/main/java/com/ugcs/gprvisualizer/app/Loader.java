package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Loader {

	private Model model;
	private RepaintListener listener;
	private SmthChangeListener changeListener;
	
	public Loader(Model model, RepaintListener listener, SmthChangeListener changeListener) {
		
		this.model = model;
		this.listener = listener;
		this.changeListener = changeListener;
		
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
            boolean success = false;
            if (db.hasFiles()) {
            	
            	load(db.getFiles());
            	
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        }

    };
    
	public void load(List<File> files) {
		
		System.out.println("load() ");
		try {
			model.getFileManager().processFileList(files);
		
			initField();
			System.out.println("===initField() " + model.getField().getPathCenter());
		}catch(Exception e) {
			e.printStackTrace();
		}

		//AppContext.levelFilter.execute();
		//LevelFilter filt = new LevelFilter(); 		
		//filt.execute(model);
		
		WhatChanged changed = new WhatChanged();
		changed.setFileopened(true);
		//listener.repaint();
		changeListener.somethingChanged(changed );
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
