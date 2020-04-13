package com.ugcs.gprvisualizer.app.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class ComplexScan implements Command {

	@Override
	public void execute(SgyFile file) {

		// find edges
		new EdgeFinder().execute(file);
		
		// find HPs
		new HorizontalGroupScan().execute(file);
		
		// lines  
		List<HorizontalProfile> lines = findStraightLines(file);
		
		if(lines.isEmpty()) {
			System.out.println("no top line :(");
			return;
		}
		// select top straight line !
		HorizontalProfile top = HorizontalGroupFilter.getBrightest(lines);
		
		// copy sgyfile
		SgyFile file2 = copy(file); 
		
		// remove noise
		new BackgroundNoiseRemover().execute(file2);

		// find edges
		new EdgeFinder().execute(file2);
		
		// find HPs
		new HorizontalGroupScan().execute(file2);
		
		// select curve lines
		List<HorizontalProfile> lines2 = findCurveLines(file2);
		if(lines2.isEmpty()) {
			System.out.println("no grnd line2 :(");
			return;
		}
		
		// select best curve !
		HorizontalProfile grnd = HorizontalGroupFilter.getBrightest(lines2);
		grnd.finish(file.getTraces());
		
		// create sum HP
		HorizontalProfile mirr = HorizontalGroupFilter.createMirroredLine(file, top, grnd);
		
		// copy to HP to original
		List<HorizontalProfile> result = new ArrayList<>();
		result.add(top);
		result.add(grnd);
		result.add(mirr);
		file.profiles= result;
		file.groundProfile = grnd;
		
	}

	private SgyFile copy(SgyFile file) {
		
		SgyFile file2 = new SgyFile();
		
		file2.setFile(file.getFile());
		
		List<Trace> traces = new ArrayList<>();
		for(Trace org : file.getTraces()){
			
			float[] values = Arrays.copyOf(org.getNormValues(), org.getNormValues().length);
			
			Trace tr = new Trace(org.getBinHeader(), org.getHeader(), values, org.getLatLon());
			traces.add(tr);
		}
		
		
		file2.setTraces(traces);
		
		return file2;
	}

	public List<HorizontalProfile> findStraightLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for(HorizontalProfile hp : file.profiles) {
			if(hp.height <= 3) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	public List<HorizontalProfile> findCurveLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for(HorizontalProfile hp : file.profiles) {
			if(hp.height > 4) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	@Override
	public String getButtonText() {
		
		return "Complex scan";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}
	
	
	

}
