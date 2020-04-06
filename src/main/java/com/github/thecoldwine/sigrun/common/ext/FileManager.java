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

	public boolean levelCalculated = false;
	
	private List<SgyFile> files;

	private List<Trace> traces = null;

	private File topFolder = null;

	public boolean isActive() {
		return files != null && !files.isEmpty();
	}

	public void processList(List<File> fileList, ProgressListener listener) throws Exception {
		clear();

		Set<File> sf = new TreeSet<File>(fileList);

		for (File fl : sf) {
			if (fl.isDirectory()) {
				processDirectory(fl, listener);
			} else {
				listener.progressMsg("load file " + fl.getAbsolutePath());
				processFile(fl);
			}

		}
	}

	public void clear() {
		levelCalculated = false;
		traces = null;
		files = new ArrayList<>();
		topFolder = null;
	}

	private void processDirectory(File fl, ProgressListener listener) throws Exception {
		if (topFolder == null) {
			topFolder = fl;
		}

		listener.progressMsg("load directory " + fl.getAbsolutePath());

		processFileList(Arrays.asList(fl.listFiles(filter)));

	}

	private void processFile(File fl) throws Exception {
		
		SgyFile sgyFile = new SgyFile();
		sgyFile.open(fl);
		files.add(sgyFile);

		try {	
			new MarkupFile().load(sgyFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processFileList(List<File> fileList) throws Exception {
		for (File fl : fileList) {
			processFile(fl);
		}
	}

	public List<SgyFile> getFiles() {
		return files;
	}

	public void setFiles(List<SgyFile> fl) {
		clear();
		files = fl;
	}

	public List<Trace> getTraces() {
		if (traces == null) {
			traces = new ArrayList<>();

			int traceIndex = 0;
			for (SgyFile file : files) {
				for (Trace trace : file.getTraces()) {
					traces.add(trace);
					trace.indexInSet = traceIndex++;
				}
			}
		}
		return traces;
	}

	public void clearTraces() {
		traces = null;		
	}

	public boolean isUnsavedExists() {
		if(isActive()) {
			for(SgyFile sgyFile : getFiles()) {
				if(sgyFile.isUnsaved()) {
					return true;
				}
			}
		}
		
		return false;
	}

}
