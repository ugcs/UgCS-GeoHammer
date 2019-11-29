package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.impl.AvalonLogger;

import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.gpr.ScanBuilder;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

public class FileManager {
	static public FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".sgy");
		}
	};

	private List<SgyFile> files;

	private List<Trace> traces = null;
	
	private File topFolder = null;

	
	public boolean isActive() {
		return files != null && !files.isEmpty();
	}


	public void processList(List<File> fileList, ProgressListener listener) {
		traces = null;
		files = new ArrayList<>();
		topFolder = null;
		
		Set<File> sf = new TreeSet<File>(fileList);
		
		for (File fl : sf) {
			if(fl.isDirectory()) {
				processDirectory(fl, listener);
			}else {
				listener.progressMsg("load file " + fl.getAbsolutePath());
				processFile(fl);
			}
			
		}
	}
	
	private void processDirectory(File fl, ProgressListener listener) {
		if(topFolder == null) {
			topFolder = fl;
		}
		
		listener.progressMsg("load directory " + fl.getAbsolutePath());
		
		processFileList(Arrays.asList(fl.listFiles(filter)));
		
	}

	private void processFile(File fl) {
		try {
			SgyFile sgyFile = new SgyFile();
			sgyFile.open(fl);
			files.add(sgyFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processFileList(List<File> fileList) {
		for (File fl : fileList) {
			processFile(fl);			
		}
		System.out.println("stop files.size(): " + files.size() );
	}

	public List<SgyFile> getFiles() {
		return files;
	}

	public List<Trace> getTraces() {
		if (traces == null) {
			traces = new ArrayList<>();
			for (SgyFile file : files) {
				traces.addAll(file.getTraces());
			}
		}
		return traces;
	}

}
