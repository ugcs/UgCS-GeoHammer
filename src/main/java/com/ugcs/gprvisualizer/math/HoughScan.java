package com.ugcs.gprvisualizer.math;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.auxcontrol.RulerTool;
import com.ugcs.gprvisualizer.app.commands.AsinqCommand;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;

@Component
@Scope(value = "prototype")
public class HoughScan implements AsinqCommand {


	private static final double MAX_GAP_SIZE = 0.25;

	public boolean isPrintLog = false;

	@Autowired
	private Model model;

	private HoughDraw hd = null;

	public HoughScan() {
		
	}

	public HoughScan(Model model) {
		this.model = model;
	}
	
	@Override
	public void execute(SgyFile file) {

		if (file.groundProfile == null) {
			new LevelScanner().execute(file);
		}
		new EdgeFinder().execute(file);
		new EdgeSubtractGround().execute(file);

		//
		int maxSmp = Math.min(AppContext.model.getSettings().layer 
				+ AppContext.model.getSettings().hpage,
				file.getMaxSamples() - 2);

		double threshold = model.getSettings().hyperSensitivity.doubleValue();

		for (int pinTr = 0; pinTr < file.size(); pinTr++) {
			//log progress
			if (pinTr % 300 == 0) {
				Sout.p("tr " + pinTr + " / " + file.size());
			}
			
			Trace tr = file.getTraces().get(pinTr);
			tr.good = new int[file.getMaxSamples()];

			for (int pinSmp = AppContext.model.getSettings().layer; 
					pinSmp < maxSmp; pinSmp++) {
				
				boolean isGood = scan(file, pinTr, pinSmp, threshold);

				if (isGood) {
					tr.good[pinSmp] = 3;
				}
			}
		}
		
		new ScanGood().execute(file);
	}	

	public boolean scan(SgyFile sgyFile, 
			int pinTrace, int pinSmpl, double threshold) {
		
		if (sgyFile.groundProfile == null 
				|| pinSmpl < sgyFile.groundProfile.deep[pinTrace]) {
			return false;
		}
		
		WorkingRect workingRect = makeWorkingRect(sgyFile, pinTrace, pinSmpl);
		
		HoughScanPinncaleAnalizer analizer = new HoughScanPinncaleAnalizer(
				sgyFile, workingRect, threshold);
		
		_makeAuxImgPreparator(workingRect, analizer);
		
		HoughArray[] stores = analizer.processRectAroundPin();
		
		//
		_makeAuxDrawer(sgyFile, workingRect, analizer, stores);
		
		for (int bestEdge = 1; bestEdge <= 4; bestEdge++) {
			
			HoughArray store = stores[bestEdge];
			
			int bestDiscr = store.getLocalMaxIndex();
			double bestVal = store.getLocalMax();
			
			if (bestDiscr < HoughDiscretizer.DISCRET_GOOD_FROM
					|| bestVal < threshold) {
				continue;
			}
	
			if (!checkGap(analizer, workingRect, bestEdge, store)) {
				continue;
			}
			
			if (analizer.fullnessAnalizer.getBorderWeakness() > 0.6) {
				continue;
			}			
			
			//TODO: check top and below
			if (analizer.fullnessAnalizer.getOutsideCount() > 6
					|| analizer.fullnessAnalizer.getInsideCount() > 6) {
				continue;
			}			
			
			
			
			if (isPrintLog) {
				hd.resedge = bestEdge;
				hd.resindex = bestDiscr;
				hd.res = bestVal;
				
				//lastHoughAindex = bestDiscr;
			}
			return true;
		}
		
		//		
		return false;
	}

	public void _makeAuxDrawer(SgyFile sgyFile, WorkingRect workingRect,
			HoughScanPinncaleAnalizer analizer, HoughArray[] stores) {
		if (isPrintLog) {
			printMatix(stores);
			prepareDrawer(sgyFile, workingRect, analizer);
		}
	}

	public void _makeAuxImgPreparator(WorkingRect workingRect, HoughScanPinncaleAnalizer analizer) {
		if (isPrintLog) {
			Sout.p("------");
			analizer.additionalPreparator =
					new HoughImgPreparator(workingRect,
						lastHoughAindex != null 
						? lastHoughAindex
						: model.getSettings().printHoughAindex.intValue());
		}
	}

	public WorkingRect makeWorkingRect(SgyFile sgyFile, int pinTrace, int pinSmpl) {
		double goodCm = HalfHyperDst.getGoodSideDstPin(sgyFile, pinTrace, pinSmpl);
		double vertDstToPinCm = RulerTool.distanceCm(sgyFile,
				pinTrace, pinTrace, 0, pinSmpl);
		
		double goodDiagCm = Math.sqrt(
				goodCm * goodCm + vertDstToPinCm * vertDstToPinCm);		
		
		int trFrom = sgyFile.getLeftDistTraceIndex(pinTrace, goodCm);
		int trTo = sgyFile.getRightDistTraceIndex(pinTrace, goodCm);		
		int smpFrom = pinSmpl;
		int smpTo = Math.min(sgyFile.getMaxSamples() - 1,
				RulerTool.diagonalToSmp(sgyFile, pinTrace, pinSmpl, goodDiagCm));
		
		WorkingRect workingRect = new WorkingRect(sgyFile, trFrom, trTo, 
				smpFrom, smpTo, pinTrace, pinSmpl);
		return workingRect;
	}
	
	
	boolean checkGap(HoughScanPinncaleAnalizer analizer, 
			WorkingRect workingRect, 
			int bestEdge, 
			HoughArray store
		) {
		
		int bestDiscr = store.getLocalMaxIndex();
		double bestVal = store.getLocalMax();

		
		double horizontalSize = getHorSizeForGap(workingRect, bestDiscr);
		
		analizer.fullnessAnalizer = new HoughHypFullness(
				(int) horizontalSize,
				bestDiscr, 
				bestEdge, 
				isPrintLog);

		/// run again
		analizer.processRectAroundPin();
		///
		
		
		double maxgap = analizer.fullnessAnalizer.getMaxGap();
		
		// 0.15 - 0.35
		double gapThreshold = 0.35 - store.getClearness() * 0.2;

		if (isPrintLog) {
			Sout.p( "  e " + bestEdge  + "  i " + bestDiscr + "  v " + bestVal
					+ " clrns: " + store.getClearness()
					+ " gap: " + maxgap 
					+ " gpThsh: " + gapThreshold
					+ " outs: " +  analizer.fullnessAnalizer.getOutsideCount() 
					+ " ins: " +  analizer.fullnessAnalizer.getInsideCount()
					+ " brdweak: " + analizer.fullnessAnalizer.getBorderWeakness()
				);
		}
		
		if (maxgap > gapThreshold 
				|| analizer.fullnessAnalizer.getBorderWeakness() > 0.6) {
			
			return false;
		}
		
		return true;
	}

	public double getHorSizeForGap(WorkingRect workingRect, int bestDiscr) {
		int maxHorizSize = workingRect.getTracePin() - workingRect.getTraceFrom();
		double horizontalSize = (double) maxHorizSize
				/ HoughArray.FACTOR[bestDiscr] * HoughArray.REDUCE[bestDiscr];
		return horizontalSize;
	}

	private int bestOfTheBestestIndex(HoughArray[] stores, int[] bestMaxIndexes) {
		int res = 1;
		for (int edge = 1; edge < 5; edge++) {
			double val = stores[edge].ar[bestMaxIndexes[edge]];
			if (val > stores[res].ar[bestMaxIndexes[res]]) {
				res = edge;
			}
		}
		
		return res;
	}

	Integer lastHoughAindex = null;
	
	public void prepareDrawer(SgyFile sgyFile, WorkingRect workingRect,
			HoughScanPinncaleAnalizer analizer) {

		double horSize = getHorSizeForGap(workingRect,
			model.getSettings().printHoughAindex.intValue());
		
		Sout.p("------ " + horSize + "   horSize " + horSize + "  lastHoughAindex " + lastHoughAindex );

		
		
		hd = new HoughDraw(analizer.additionalPreparator.getImage(), sgyFile, 
				workingRect.getTraceFrom(), workingRect.getTraceTo() + 1,
				workingRect.getSmpFrom(), workingRect.getSmpTo() + 1);
		
		hd.horizontalSize = horSize;
	}

	public int bestOfTheBestestIndex(double[] best) {
		int mxIndex = 1;
		for (int i = 1; i < 5; i++) {
		
			if (best[i] > best[mxIndex]) {
				mxIndex = i;
			}
		}
		return mxIndex;
	}

	private void print(HoughArray[] stores) {

		if (!isPrintLog) {
			return;
		}

		for (int i = 1; i < stores.length; i++) {
			HoughArray s = stores[i];
			Sout.p("edge " + i + " = " + Arrays.toString(s.ar));
		}
	}

	//public Map<Integer, Integer> getMaxIndexes(HoughArray[] stores) {
	public void printMatix(HoughArray[] stores) {
		
		for (int i = 1; i < stores.length; i++) {

			int index = stores[i].getLocalMaxIndex();

			if (isPrintLog) {
				StringBuilder sb = new StringBuilder();
				for (int z = 0; z < stores[i].ar.length; z++) {
					if (z == HoughDiscretizer.DISCRET_GOOD_FROM) {
						sb.append("|");
					}
					
					if (z == index) {
						sb.append("[");
					} else {
						sb.append(" ");
					}
					
					sb.append(String.format("%4.1f", stores[i].ar[z]));

					if (z == index) {
						sb.append("]");
					} else {	
						sb.append(" ");
					}
				}
				
				Sout.p(String.format("edge %2d ind %3d - maxval %5.1f = ", 
						i, index, stores[i].ar[index]) 
						+ sb.toString());
			}
		}
		
	}


	@Override
	public String getButtonText() {

		return "Hough scan";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}

	public HoughDraw getHoughDrawer() {
		return hd;
	}

}
