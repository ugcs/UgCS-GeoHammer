package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class LevelGround implements Command {

	@Override
	public void execute(SgyFile file, ProgressListener listener) {

		HorizontalProfile hp = file.groundProfile;
		int level = (file.groundProfile.minDeep + file.groundProfile.maxDeep) / 2;
		
		for (int i = 0; i < file.getTraces().size(); i++) {
			
			Trace trace = file.getTraces().get(i);
			

			float[] values = trace.getNormValues();
			float[] newValues = new float[values.length];
			int srcStart = Math.max(0, hp.deep[i] - level);
			int dstStart = Math.max(0, level - hp.deep[i]);
			
			System.arraycopy(
				values, srcStart, 
				newValues, dstStart, 
				values.length - Math.abs(hp.deep[i] - level));
			
			trace.setNormValues(newValues);
			trace.verticalOffset = level - hp.deep[i];
			
		}
		file.groundProfile = null;
		file.setUnsaved(true);
	}

	@Override
	public String getButtonText() {

		return "Level ground";
	}

	@Override
	public Change getChange() {

		return Change.traceValues;
	}

}
