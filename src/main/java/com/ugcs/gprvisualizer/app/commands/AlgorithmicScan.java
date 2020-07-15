package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.math.HalfHyperDst;
import com.ugcs.gprvisualizer.math.ScanProfile;

public class AlgorithmicScan implements AsinqCommand {

	public static final double X_FACTOR_FROM = 0.90;
	public static final double X_FACTOR_TO = 1.71;
	public static final double X_FACTOR_STEP = 0.1;
	public static final int MARGIN = 3;
	
	
	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		
		//clear
		for (Trace t : file.getTraces()) {
			t.good = new byte[t.getNormValues().length];			
		}
		
		processSgyFile(file);			
	}

	private void processSgyFile(SgyFile sf) {
		List<Trace> traces = sf.getTraces();
		int height = traces.get(0).getNormValues().length;
		byte[][] good = new byte[traces.size()][height];
		
		if (sf.groundProfile == null) {
			System.out.println("!!!!groundProfile == null");
			return;
		}
		
		for (int i = 0; i < traces.size(); i++) {
			processTrace(sf, i, good); 
		}
		
		sf.algoScan = saveResultToTraces(traces, good);
	}

	private ScanProfile saveResultToTraces(List<Trace> traces, byte[][] good) {
		
		ScanProfile hp = new ScanProfile(traces.size()); 
		
		for (int i = 0; i < traces.size(); i++) {
			hp.intensity[i] = cleversumdst(good, i);
			
			if (traces.get(i).good == null) {
				traces.get(i).good = new byte[good[i].length];
			}
			
			//put to trace.good
			for (int z = 0; z < good[i].length; z++) {
				traces.get(i).good[z] = good[i][z];
			}
		}
		hp.finish();
		
		return hp;
	}

	private int cleversumdst(byte[][] good, int tr) {
		double sum = 0;		
		
		double maxsum = 0;
		int emptycount = 0;
		double bothCount = 1;
		double singleCount = 1; 
		for (int i = 0; i < good[tr].length; i++) {
			
			// 0 1-left 2-right   3-both
			int val = getAtLeastOneGood(good, tr, MARGIN, i);
			
			
			if (val != 0) {
				emptycount = 0;

				if (val == 3) {
					bothCount++;
					sum += 20.0 / bothCount;
				} else {
					singleCount++;
					sum += 1.0 / singleCount;
				}
			} else {
				emptycount++;
				if (emptycount > 5) {
					maxsum = Math.max(maxsum, sum);
					bothCount = 1;
					singleCount = 1;
					sum = 0;
					emptycount = 0;
		
				}
			}			
		}
		
		maxsum = Math.max(maxsum, sum);
		return (int) (maxsum * 5);
	}

	
	
	private int getAtLeastOneGood(byte[][] good, int tr, int margin, int smp) {
		int r = 0;
		
		for (int chtr = Math.max(0, tr - margin); 
				chtr < Math.min(good.length, tr + margin + 1); 
				chtr++) {
			r = r | good[chtr][smp]; //0 1 2 3				
		}
		
		return r;
	}

	private double processTrace(SgyFile sgyFile, int tr, byte[][] good) {
		
		double thr = getThreshold();
		
		int goodSmpCnt = 0;
		int maxSmp =
				Math.min(
					AppContext.model.getSettings().layer
					+ AppContext.model.getSettings().hpage,
					
					sgyFile.getTraces().get(tr).getNormValues().length - 2);
		
		// test all samples to fit hyperbola
		
		for (int smp = AppContext.model.getSettings().layer;				
			smp < maxSmp; smp++) {			
			
			byte exists = checkAllVariantsForPoint(sgyFile, tr, thr, smp);
			
			good[tr][smp] = (byte) (good[tr][smp] | exists);
		}
		
		return goodSmpCnt;
	}

	public static byte checkAllVariantsForPoint(SgyFile sgyFile, int tr, double thr, int smp) {
		int exists = 0;
		// reduce x distance for hyperbola calculation
		for (double factorX = X_FACTOR_FROM;
				factorX <= X_FACTOR_TO;
				factorX += X_FACTOR_STEP) {

			exists = exists | processHyper3(sgyFile, tr, smp, factorX, thr);
			if (exists == 3) {
				break;
			}
		}
		return (byte) exists;
	}

	public double getThreshold() {
		double thr = (double) AppContext.model.getSettings()
				.hyperSensitivity.intValue() / 100.0;
		return thr;
	}
	
	public static int processHyper3(SgyFile sgyFile, int tr, int smp, 
			double factorX, double thr) {
		
		HalfHyperDst left = HalfHyperDst.getHalfHyper(
				sgyFile, tr, smp, -1, factorX);		
		
		HalfHyperDst right = HalfHyperDst.getHalfHyper(
				sgyFile, tr, smp, +1, factorX);
		
		double left100 = left.analize(100); 
		//double left20 = left.analize(40);
		double right100 = right.analize(100);
		//double right20 = right.analize(40);
		
		return 
			(left100 > thr ? 1 : 0) 
				| 
			(right100 > thr ? 2 : 0);
	}
	
	@Override
	public String getButtonText() {
	
		return "internal algorithmic scan";
	}

	public Change getChange() {
		return Change.traceValues;
	}
	
}
