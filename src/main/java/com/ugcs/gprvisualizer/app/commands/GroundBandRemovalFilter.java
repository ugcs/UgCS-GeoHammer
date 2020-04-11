package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.ArrayMath;

public class GroundBandRemovalFilter implements Command {

	static final int from = -10;
	static final int to = 12;
	static final int deep = to-from;

	
	@Override
	public void execute(SgyFile file) {
		
		float[] avg = prepareNoiseProfileForBand(file.getTraces());
	
		subtractProfileForBand(file.getTraces(), avg);
	}
	
	private float[] prepareNoiseProfileForBand(List<Trace> lst) {
		
		
		float avg[] = new float[deep];

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);
			float []values = trace.getNormValues();
			
			for(int i=0; i<deep; i++) {
				
				int valindex = trace.maxindex+i+from;
				avg[i] += values[valindex]; 
			}			
		}

		ArrayMath.arrayDiv(avg, lst.size());
		return avg;
	}
	
	private void subtractProfileForBand(List<Trace> lst, float[] avg) {
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float values[] = trace.getNormValues();
			//ArrayMath.arraySub(normval, avg);
			//trace.setNormValues(normval);
			
			for(int i=0; i<deep; i++) {
				int valindex = trace.maxindex+i+from;
				values[valindex] -= avg[i]; 
			}			
			
		}
	}
	
	

	@Override
	public String getButtonText() {

		return "Ground band removal";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}

}
