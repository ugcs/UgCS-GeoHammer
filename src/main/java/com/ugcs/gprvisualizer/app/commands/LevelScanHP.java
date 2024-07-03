package com.ugcs.gprvisualizer.app.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

/**
 * find ground level 2 (from HorizontalProfile). 
 *
 */
public class LevelScanHP implements Command {

	@Override
	public void execute(SgyFile file, ProgressListener listener) {

		
		// find edges
		new EdgeFinder().execute(file, listener);
		
		// find HPs
		new HorizontalGroupScan().execute(file, listener);
		
		// lines  
//		List<HorizontalProfile> lines = findStraightLines(file);
//		
//		if(lines.isEmpty()) {
//			System.out.println("no top line :(");
//			return;
//		}
//		// select top straight line !
//		HorizontalProfile top = HorizontalGroupFilter.getBrightest(lines);
		
		// copy sgyfile
		SgyFile file2 = file.copy(); 
		
		// remove noise
		new BackgroundNoiseRemover().execute(file2, listener);

		// find edges
		new EdgeFinder().execute(file2, listener);
		
		// find HPs
		new HorizontalGroupScan().execute(file2, listener);
		
		// select curve lines
		List<HorizontalProfile> lines2 = findCurveLines(file2);
		if (lines2.isEmpty()) {
			System.out.println("no grnd line2 :(");
			return;
		}
		
		// select best curve !
		HorizontalProfile grnd = HorizontalGroupFilter.getBrightest(lines2);
		grnd.finish(file.getTraces());
		grnd.color = new Color(200, 100, 100);
		
		// create sum HP
		//HorizontalProfile mirr = 
		//HorizontalGroupFilter.createMirroredLine(file, top, grnd);
		
		// copy to HP to original
		List<HorizontalProfile> result = new ArrayList<>();
		result.add(grnd);
		file.profiles = result;
		file.setGroundProfile(grnd);
	
		//aux tasks 
		new EdgeFinder().execute(file, listener);
		new EdgeSubtractGround().execute(file, listener);		
		
	}

	public List<HorizontalProfile> findStraightLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for (HorizontalProfile hp : file.profiles) {
			if(hp.height <= 3) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	public List<HorizontalProfile> findCurveLines(SgyFile file) {
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		for (HorizontalProfile hp : file.profiles) {
			if (hp.height > 4) {
				tmpStraight.add(hp);
			}
		}
		
		return tmpStraight;
	}

	@Override
	public String getButtonText() {
		
		return "Find ground level v2";
	}

	@Override
	public Change getChange() {

		return Change.traceValues;
	}

}
