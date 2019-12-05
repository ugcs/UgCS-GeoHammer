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
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class Saver implements ToolProducer {

	private Button buttonSave = new Button("Save");
	private Button buttonSaveReload = new Button("Save To New Place");
	private Model model;

	private ProgressTask saveTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("save now");
			List<File> newfiles = saveTheSame();		
			
			listener.progressMsg("load now");
	    	AppContext.loader.load(newfiles, listener);
		}
	};

	private ProgressTask saveReloadTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("save now");

			List<File> newfiles = savePartsToNewPlace();
			
			listener.progressMsg("load now");
	    	AppContext.loader.load(newfiles, listener);
		}
	};

	{
		buttonSave.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				new TaskRunner(null, saveTask).start();
		    	
		    }
		});
		
		buttonSaveReload.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	new TaskRunner(null, saveReloadTask).start();
		    	
		    }
		});
	}
	
	public Saver(Model model) {
		this.model = model;
	}
	
	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(buttonSave, buttonSaveReload);
	}

	
	
	private List<File> saveTheSame() {
		List<File> newfiles = new ArrayList<>();
		
		for(SgyFile file : model.getFileManager().getFiles()) {
			newfiles.add(save(file));
		}
		
		return newfiles;
	}
	
	private List<File> savePartsToNewPlace() {
		List<File> newfiles = new ArrayList<>();
		
		
		File folder = createFolder();
		for(SgyFile file : model.getFileManager().getFiles()) {
			int part = 1;
			List<Trace> sublist = new ArrayList<>();
			for(Trace trace : file.getTraces()) {
				
				if(trace.isActive()) {
					sublist.add(trace);
				}else {
					if(!sublist.isEmpty()){					
						newfiles.add(savePart(file, part++, sublist, folder));
						sublist.clear();
					}		
				}
			}
			//for last
			if(!sublist.isEmpty()){					
				newfiles.add(savePart(file, part++, sublist, folder));
				sublist.clear();
			}		
		}
		
		return newfiles;
	}

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

	private File save(SgyFile sgyFile) {
		List<ByteBufferProducer> blocks = getBlocks(sgyFile.getTraces()); 
		File oldFile = null;
		
		
		try {

			//nfile = new File(nfolder, onlyname + "_" + spart + name.substring(pos));
			oldFile = sgyFile.getFile();
			File nfolder = oldFile.getParentFile();
			
			File tmp = File.createTempFile("tmp", "sgy", nfolder);
			
			sgyFile.savePart(tmp.getAbsolutePath(), blocks);
			
			boolean t = oldFile.delete();
			System.out.println(" deleted " + t);
			
			boolean r = tmp.renameTo(oldFile);
			System.out.println(" renamed " + r);
			
			new MarkupFile().save(sgyFile, oldFile);
			
		} catch (IOException e) {
			e.printStackTrace();
			oldFile = null;
		}
		return oldFile;
	}
	

	private File savePart(SgyFile file, int part, List<Trace> sublist, File nfolder) {
		List<ByteBufferProducer> blocks = getBlocks(sublist); 
		File nfile = null;
		try {
			String name = file.getFile().getName();
			int pos = name.lastIndexOf(".");
			String onlyname = name.substring(0, pos);
			String spart = String.format("%03d", part);
			nfile = new File(nfolder, onlyname + "_" + spart + name.substring(pos));
			
			
			file.savePart(nfile.getAbsolutePath(), blocks);
			new MarkupFile().save(file, nfile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nfile;
	}

	private List<ByteBufferProducer> getBlocks(List<Trace> sublist) {
		
		List<ByteBufferProducer> blocks = new ArrayList<>();
		for(Trace trace : sublist) {
			blocks.add(trace.getHeaderBlock());
			//blocks.add(trace.getDataBlock());
			blocks.add(new ByteBufferHolder(trace));
		}
		
		return blocks;
	}
	
	
	
}
