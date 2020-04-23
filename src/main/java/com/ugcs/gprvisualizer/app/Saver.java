package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ByteBufferHolder;
import com.github.thecoldwine.sigrun.common.ext.ByteBufferProducer;
import com.github.thecoldwine.sigrun.common.ext.MarkupFile;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Saver implements ToolProducer {

	private Button buttonSave = new Button("Save");
	private Button buttonSaveTo = new Button("Save to");
	private Model model;
	private Stage stage;
	private File folder;
	
	private ProgressTask saveTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("save now");
			
			
			List<File> newfiles = saveTheSame();		
			
			
			listener.progressMsg("load now");			
			try {
				AppContext.loader.load(newfiles, listener);
				
			}catch(Exception e) {
				MessageBoxHelper.showError("error reopening files", "");
			}
		    	
	    	AppContext.notifyAll(new WhatChanged(Change.fileopened));
		    	
		    AppContext.statusBar.showProgressText("saved " + model.getFileManager().getFiles().size() + " files");
		}
	};

	private ProgressTask saveAsTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("save now");

			List<File> newfiles = saveAs(folder);
			
			listener.progressMsg("load now");
			
			try {
				AppContext.loader.load(newfiles, listener);
			}catch(Exception e) {
				MessageBoxHelper.showError("error reopening files", "");
			}
				
				
	    	
	    	AppContext.notifyAll(new WhatChanged(Change.fileopened));
	    	
	    	AppContext.statusBar.showProgressText("saved " + model.getFileManager().getFiles().size() + " files");
		}
	};

	{
		buttonSave.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				new TaskRunner(AppContext.stage, saveTask).start();
		    	
		    }
		});
		
		buttonSaveTo.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	DirectoryChooser dir_chooser = new DirectoryChooser(); 
		    	if(!model.getFileManager().getFiles().isEmpty()) {
		    		dir_chooser.setInitialDirectory(model.getFileManager().getFiles().get(0).getFile().getParentFile());
		    	}
		    	folder = dir_chooser.showDialog(stage); 
		    	  
                if(folder != null) { 
                	new TaskRunner(AppContext.stage, saveAsTask).start();
                } 		    	
		    	
		    	
		    	
		    }
		});
	}
	
	public Saver(Model model, Stage stage) {
		this.model = model;
		this.stage = stage;
	}
	
	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(buttonSave, buttonSaveTo);
	}

	
	
	private List<File> saveTheSame() {
		List<File> newfiles = new ArrayList<>();
		
		for(SgyFile file : model.getFileManager().getFiles()) {
			newfiles.add(save(file));
		}
		
		return newfiles;
	}
	
	private List<File> saveAs(File folder) {
		List<File> newfiles = new ArrayList<>();
		
		//File folder = createFolder();
		for(SgyFile file : model.getFileManager().getFiles()) {
			newfiles.add(save(file, folder));
		}
		
		return newfiles;
	}

//	private List<List<Trace>> splitFile(SgyFile file) {
//		List<List<Trace>> splitList = new ArrayList<>();
//		List<Trace> sublist = new ArrayList<>();
//		for(Trace trace : file.getTraces()) {
//			
//			
//			
//			if(trace.isActive()) {
//				sublist.add(trace);
//			}else {
//				if(!sublist.isEmpty()){
//					splitList.add(sublist);
//					sublist = new ArrayList<>();
//											
//				}		
//			}
//		}
//		//for last
//		if(!sublist.isEmpty()){					
//			splitList.add(sublist);
//		}
//		return splitList;
//	}

	private File createFolder() {
		File someFile = model.getFileManager().getFiles().get(0).getFile();
		File nfolder;
		int cnt=0;
		do {
			cnt++;
			String name = String.format("processed_%03d", cnt);
			nfolder = new File(someFile.getParentFile(), name);
		}while(nfolder.exists());
		
		nfolder.mkdir();
		return nfolder;
	}

	private File save(SgyFile sgyFile, File folder) {

		File newFile = null;
		
		try {
			File oldFile = oldFile = sgyFile.getFile();
			
			newFile = new File(folder, oldFile.getName());
			
			
			sgyFile.save(newFile);
			new MarkupFile().save(sgyFile, newFile);
			
		} catch (Exception e) {
			e.printStackTrace();
			newFile = null;
		}
		return newFile;
	}
	
	private File save(SgyFile sgyFile) {

		File oldFile = null;
		
		try {
			oldFile = sgyFile.getFile();
			File nfolder = oldFile.getParentFile();
			
			File tmp = File.createTempFile("tmp", "sgy", nfolder);
			
			sgyFile.save(tmp);
			
			boolean t = oldFile.delete();
			
			boolean r = tmp.renameTo(oldFile);
			
			new MarkupFile().save(sgyFile, oldFile);
			
		} catch (Exception e) {
			e.printStackTrace();
			oldFile = null;
		}
		return oldFile;
	}
}
