package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Project {

	enum State {
		SINGLE_FILE,
		
		
		ORIGINAL_FILE_IN_PROJECT,
		//-cut
		
		SLICED_FILES_VERSION
		//-save
		//-save as
		
	}
	
	private State state;
	
	File originalFile;
	File projectFolder;
	File slicedVersionFolder;
	
	
	public void open(File file) throws Exception {
		if(file.isDirectory()) {
			openSlicedVersion(file);
		}else{
			if(!checkInProjectFolder(file)) {
				//ask to create project
				
				file = createProject(file);				
			}
			
			openOriginalSgyFile(file);
			
		}
		
	}
	
	private boolean checkInProjectFolder(File file) {
		
		File folder = file.getParentFile();
		
		return folder.getName().equalsIgnoreCase(clearExtension(file.getName()));
	}

	public File createProject(File file) throws IOException {
		//create project structure
		
		File folder = file.getParentFile();
		File projectFolder = new File(folder, clearExtension(file.getName()));
		projectFolder.mkdir();

		//copy
	    File dest = new File(projectFolder, file.getName());

	    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		
	    return dest; 
	}	

	private String clearExtension(String name) {
		
		int p = name.indexOf(".sgy");
		if(p < 0) {
			throw new RuntimeException("bad extension");
		}
		return name.substring(0, p);
	}

	public void saveFile() {
		
	}
	
	public void saveSlicedVersion() {
		if(state == State.ORIGINAL_FILE_IN_PROJECT) {
			//create
			
		}else{
			//save
		}
		
		
	}
	
	public void saveAsSlicedVersion() {
		
	}
	
	public void clearSlicedVersion() {
		if(state != State.SLICED_FILES_VERSION) {
			throw new RuntimeException("state != State.SLICED_FILES_VERSION");
		}
		
		//ask
		//delete processed
		//copy raw to processed
		//open processed
		
	}
	
	
	public void openOriginalSgyFile(File file) {
		
		state = State.ORIGINAL_FILE_IN_PROJECT;
		
	}
	
	
	public void openSlicedVersion(File folder) {
		//folder  XXX_001
		
		//check internal structure
		File raw = new File(folder, "raw");
		File processed = new File(folder, "processed");
		File markup = new File(folder, "markup");
		
		if(!raw.exists() || !processed.exists() || !markup.exists()) {
			throw new RuntimeException("bad structure");
		}
		
		//open processed files
		
		//open markup files
		
		state = State.SLICED_FILES_VERSION;
	}
}
