package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.io.File;
import java.util.List;

import com.ugcs.gprvisualizer.app.kml.KmlReader;
import com.ugcs.gprvisualizer.app.parcers.GeoCoordinates;
import com.ugcs.gprvisualizer.app.parcers.csv.CSVParsersFactory;
import com.ugcs.gprvisualizer.app.parcers.csv.CsvParser;

import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ConstPointsFile;
import com.github.thecoldwine.sigrun.common.ext.GprFile;
import com.github.thecoldwine.sigrun.common.ext.PositionFile;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.StretchArray;

import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

@Component
public class Loader {

	private final Model model;
	private final Status status; 
	private final Broadcast broadcast;

	public Loader(Model model, Status status, Broadcast broadcast) {
		this.model = model;
		this.status = status;
		this.broadcast = broadcast;
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
        	
         	final List<File> files = db.getFiles();  
         	
			if (isConstPointsFile(files)) {
				
				openConstPointFile(files);				
				return;				
			} 
			
			if (isPositionFile(files)) {
				model.setLoading(true);
				openPositionFile(files);
				return;
			}

			if (isKmlFile(files)) {

				openKmlFile(files);
				return;
			}

			if (model.stopUnsaved()) {
        		return;
        	}

        	
        	ProgressTask loadTask = new ProgressTask() {
				@Override
				public void run(ProgressListener listener) {
					try {  
						
						loadWithNotify(files, listener);
				
					} catch (Exception e) {
						e.printStackTrace();
						
						MessageBoxHelper.showError(
							"Can`t open files", 
							"Probably file has incorrect format");
						
						model.getFileManager().getFiles().clear();
						model.getChartsContainer().getChildren().clear();
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

		private void openConstPointFile(final List<File> files) {
			ConstPointsFile cpf = new ConstPointsFile();
			cpf.load(files.get(0));
			
			for (SgyFile sgyFile : 
				model.getFileManager().getFiles()) {
				
				cpf.calcVerticalCutNearestPoints(
						sgyFile);
			}
			
			model.updateAuxElements();
		}

		private void openPositionFile(List<File> files) {
			//if (model.getFileManager().getFiles().size() == 0) {
				
			//	MessageBoxHelper.showError(
			//			"Can`t open position file", 
			//			"Open GPR file at first");
				
			//	return;
			//}
			//if (model.getFileManager().getFiles().size() > 1) {
			//	MessageBoxHelper.showError(
			//			"Can`t open position file", 
			//			"Only one GPR file must be opened");
				
			//	return;
			//}
			//if (files.size() > 1) {
			//	MessageBoxHelper.showError(
			//			"Can`t open position file", 
			//			"Only one position file must be opened");
				
			//	return;
			//}
				
			try {
				//SgyFile sgyFile = model.getFileManager().getFiles().size() > 0 ? 
				//	model.getFileManager().getFiles().get(0) : new GprFile();

				SgyFile sgyFile = new GprFile();
	
				new PositionFile(model.getFileManager().getFileTemplates())
					.load(sgyFile, files.get(0));
				model.getFileManager().getFiles().add(sgyFile);	

				//model.init();			
		
				//when open file by dnd (not after save)
				model.initField();

				model.initChart(files.get(0), broadcast);

			} catch (Exception e) {
				e.printStackTrace();
				MessageBoxHelper.showError(
						"Can`t open file", e.getMessage() != null ? e.getMessage() :
						"Probably file has incorrect format");
			}
			
			broadcast.notifyAll(new WhatChanged(Change.updateButtons));				
			broadcast.notifyAll(new WhatChanged(Change.fileopened));
			//broadcast.notifyAll(new WhatChanged(Change.justdraw));
		}

    };

	private void openKmlFile(List<File> files) {
		if (model.getFileManager().getFiles().size() == 0) {

			MessageBoxHelper.showError(
				"Can`t open kml file",
				"Open GPR file at first");

			return;
		}
		if (files.size() > 1) {
			MessageBoxHelper.showError(
				"Can`t open position file",
				"Only one position file must be opened");

			return;
		}

		try {
			new KmlReader().read(files.get(0), model);
		} catch (Exception e) {

			e.printStackTrace();
			MessageBoxHelper.showError(
				"Can`t open position file",
				"Probably file has incorrect format");
		}

		broadcast.notifyAll(new WhatChanged(Change.updateButtons));
		broadcast.notifyAll(new WhatChanged(Change.justdraw));

	}



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
		
		
		//
		
		SgyFile file = model.getFileManager().getFiles().get(0);
		if (file.getSampleInterval() < 105) {
			model.getSettings().hyperkfc = 25;
			
		} else {
			double i = file.getSampleInterval() / 104.0;
			
			model.getSettings().hyperkfc = (int) (25.0 + i * 1.25);
		}
		
	}

	private boolean isConstPointsFile(final List<File> files) {
		return files.size() == 1 
				&& files.get(0).getName().endsWith(".constPoints");
	}
	
	private boolean isPositionFile(final List<File> files) {
		return !files.isEmpty() 
				&& files.get(0).getName().toLowerCase().endsWith(".csv");
	}

	private boolean isKmlFile(final List<File> files) {
		return !files.isEmpty()
			&& files.get(0).getName().toLowerCase().endsWith(".kml");
	}
}
