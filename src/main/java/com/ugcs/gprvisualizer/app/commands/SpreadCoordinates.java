package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;

public class SpreadCoordinates implements Command {
	
	@Override
	public String getButtonText() {
		return "Spread coordinates";
		//return "Spread coordinates (recommended)";
	}

	@Override
	public Change getChange() {
		return Change.mapscroll;
	}

	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		
		new DistCalculator().execute(file, null);	
		
		//Sout.p("prolong");
		prolongDistances(file.getTraces());
		
		new DistCalculator().execute(file, null);		
		
		new DistancesSmoother().execute(file, null);

		
	}

	private void prolongDistances(List<Trace> traces) {
		double[] dst = new double[traces.size()]; 
		for (int i = 0; i < dst.length; i++) {
			dst[i] = traces.get(i).getPrevDist();
		}
		
		double[] dst2 = new double[traces.size()]; 
		
		int lastGoodIndex = 0;
		double thr = 0.0001;
		LatLon ll1 = traces.get(0).getLatLon();
		for (int i = 0; i < dst.length; i++) {
			
			if (dst[i] <= thr) {
				//pass
				//lastZeroIndex = i;
			} else {
				
				double count = (double) (i - lastGoodIndex);
				double partDst = dst[lastGoodIndex] / count;
				
				
				LatLon ll2 = traces.get(lastGoodIndex).getLatLon();
				for (int j = lastGoodIndex; j < i; j++) {
					dst2[j] = partDst;
					
					traces.get(j).setLatLon(new LatLon(
							ll1.getLatDgr() + (ll2.getLatDgr() - ll1.getLatDgr()) * (j - lastGoodIndex) / count, 
							ll1.getLonDgr() + (ll2.getLonDgr() - ll1.getLonDgr()) * (j - lastGoodIndex) / count));
					
				}
				ll1 = ll2;
				thr = dst[i] / 10;
				
				lastGoodIndex = i;
			}
		}
		
	}

	public static boolean isSpreadingNecessary(SgyFile file) {
		int zeroCount = 0;
		for (Trace trace : file.getTraces()) {
			if (trace.getPrevDist() < 0.001) {
				zeroCount++;				
			}
		}
		//Sout.p(zeroCount + " > " +  file.size());
		return zeroCount > file.size() / 2;
	}

	public static boolean isSpreadingNecessary(List<SgyFile> files) {
		for (SgyFile file : files) {
			if (isSpreadingNecessary(file)) {
				return true;
			}
		}
		return false;
	}

}
