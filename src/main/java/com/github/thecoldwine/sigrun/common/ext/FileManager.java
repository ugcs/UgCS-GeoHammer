package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.impl.AvalonLogger;

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

	public void load(File folder) {

		// load
		processFolder(folder);


		//

	}

	public void processFolder(File folder) {
		traces = null;
		processFileList(Arrays.asList(folder.listFiles(filter)));

	}

	public void processFileList(List<File> fileList) {
		traces = null;
		files = new ArrayList<>();
		for (File fl : fileList) {

			//String path = fl.getAbsolutePath();

			try {
				SgyFile sgyFile = new SgyFile();
				sgyFile.open(fl);
				files.add(sgyFile);

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("files.size(): " + files.size() );
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
