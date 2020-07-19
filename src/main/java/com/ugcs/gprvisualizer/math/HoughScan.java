package com.ugcs.gprvisualizer.math;

import java.awt.Color;
import java.awt.Graphics2D;
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
import com.ugcs.gprvisualizer.app.ProgressListener;
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
	public void execute(SgyFile file, ProgressListener listener) {

		if (file.groundProfile == null) {
			new LevelScanner().execute(file, listener);
		}
		new EdgeFinder().execute(file, listener);
		new EdgeSubtractGround().execute(file, listener);

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
			tr.good = new byte[file.getMaxSamples()];

			for (int pinSmp = AppContext.model.getSettings().layer; 
					pinSmp < maxSmp; pinSmp++) {
				
				boolean isGood = scan(file, pinTr, pinSmp, threshold);

				if (isGood) {
					tr.good[pinSmp] = 3;
				}
			}
		}
		
		new ScanGood().execute(file, listener);
	}	

	public boolean scan(SgyFile sgyFile, 
			int pinTrace, int pinSmpl, double threshold) {
		
		if (sgyFile.groundProfile == null 
				|| pinSmpl < sgyFile.groundProfile.deep[pinTrace]) {
			return false;
		}
		
		////
		if (isPrintLog) {
			
			StringBuilder sb = new StringBuilder();
			
			double goodCm = HalfHyperDst.getGoodSideDstPin(sgyFile, pinTrace, pinSmpl);;
			
			
			
			sb.append(" pinSmpl: ").append(pinSmpl);
			//sb.append(" goodDiagCm: ").append(Sout.d(goodDiagCm));
			
			for (int i = 0; i < HoughDiscretizer.DISCRET_SIZE; i++) {
			
				double goodSpecific = goodCm / HoughArray.FACTOR[i];
				
				double goodDiagCm = getGoodHeightCm(sgyFile, pinTrace, pinSmpl, goodSpecific);
				
				
		
				int smpTo = Math.min(sgyFile.getMaxSamples() - 1,
						RulerTool.diagonalToSmp(sgyFile, pinTrace, pinSmpl, goodDiagCm));
				
				
				sb.append(i).append(":");
				sb.append(Sout.d(goodSpecific));
				sb.append(Sout.d(goodDiagCm));
				
				sb.append(" | ");
			}
			Sout.p(sb.toString());
		}
		
		////
		WorkingRect workingRect = makeWorkingRect(sgyFile, pinTrace, pinSmpl);
		
		HoughScanPinncaleAnalizer analizer = new HoughScanPinncaleAnalizer(
				sgyFile, workingRect, threshold,
				100.0 / workingRect.getWidth()
				);
		
		_makeAuxImgPreparator(workingRect, analizer);
		
		HoughArray[] stores = analizer.processRectAroundPin();
		
		//
		_makeAuxDrawer(sgyFile, workingRect, analizer, stores);
		
		for (int bestEdge = 1; bestEdge <= 4; bestEdge++) {
			
			HoughArray store = stores[bestEdge];
			
			int bestDiscr = store.getLocalMaxIndex();
			
			//
			double bestVal = store.getLocalMax();
			
			if (bestDiscr < HoughDiscretizer.DISCRET_GOOD_FROM
					|| bestVal < threshold) {
				continue;
			}
	
			double horizontalSize = getHorSizeForGap(workingRect, bestDiscr);
			analizer.fullnessAnalizer = new HoughHypFullness(
					(int) horizontalSize,
					bestDiscr, bestEdge, isPrintLog,
					analizer.getNormFactor());
			/// run again
			analizer.processRectAroundPin();
			///
			
			_logStoreResults(analizer, bestEdge, store, bestDiscr, bestVal);
			
			
			double distantPointsCount = analizer.fullnessAnalizer.getDistantPointsCount();
			if (bestVal - distantPointsCount < threshold ) {
				return false;
			}			
			
			if (!checkGap(analizer, workingRect, bestEdge, store)) {
				continue;
			}
			
			if (analizer.fullnessAnalizer.getBorderWeakness() > 0.6) {
				continue;
			}
			
			if (analizer.fullnessAnalizer.getOutsideCount() / bestVal > 0.35
				|| analizer.fullnessAnalizer.getInsideCount() / bestVal > 0.35) {
				
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

	public void _logStoreResults(HoughScanPinncaleAnalizer analizer, int bestEdge, HoughArray store,
			int bestDiscr, double bestVal) {
		if (isPrintLog) {
			Sout.p( "  e " + bestEdge  
					+ "  i " + bestDiscr 
					+ " val: " + Sout.d(store.getLocalMax())
					+ " clr: " + Sout.d(store.getClearness())
					+ " outs: " + Sout.d(analizer.fullnessAnalizer.getOutsideCount() / bestVal)
					
					+ " ins: " + Sout.d(analizer.fullnessAnalizer.getInsideCount() / bestVal)
					+ " brdweak: " + Sout.d(analizer.fullnessAnalizer.getBorderWeakness())
				);
		}
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
			analizer.additionalPreparator =
					new HoughImgPreparator(workingRect,
						lastHoughAindex != null 
						? lastHoughAindex
						: model.getSettings().printHoughAindex.intValue());
		}
	}

	public WorkingRect makeWorkingRect(SgyFile sgyFile, int pinTrace, int pinSmpl) {
		double goodCm = HalfHyperDst.getGoodSideDstPin(sgyFile, pinTrace, pinSmpl);
		int trFrom = sgyFile.getLeftDistTraceIndex(pinTrace, goodCm);
		int trTo = sgyFile.getRightDistTraceIndex(pinTrace, goodCm);		

		double goodDiagCm = getGoodHeightCm(sgyFile, pinTrace, pinSmpl, goodCm);		
		
		int smpTo = Math.min(sgyFile.getMaxSamples() - 1,
				RulerTool.diagonalToSmp(sgyFile, pinTrace, pinSmpl, goodDiagCm));
		
		
		//Sout.p(goodDiagCm + " <=> " + RulerTool.distanceCm(sgyFile, trFrom, trFrom, 0, smpTo));
		
		int smpFrom = pinSmpl;
		WorkingRect workingRect = new WorkingRect(sgyFile, trFrom, trTo, 
				smpFrom, smpTo, pinTrace, pinSmpl);
		return workingRect;
	}

	public double getGoodHeightCm(SgyFile sgyFile, int pinTrace, int pinSmpl, double goodCm) {
		double vertDstToPinCm = RulerTool.distanceCm(sgyFile,
				pinTrace, pinTrace, 0, pinSmpl);
		
		double goodDiagCm = Math.sqrt(
				goodCm * goodCm + vertDstToPinCm * vertDstToPinCm);
		return goodDiagCm;
	}
	
	
	boolean checkGap(HoughScanPinncaleAnalizer analizer, 
			WorkingRect workingRect, 
			int bestEdge, 
			HoughArray store
		) {
		
		
		double maxgap = analizer.fullnessAnalizer.getMaxGap();
		// 0.15 - 0.35
		double gapThreshold = 0.35 - store.getClearness() * 0.2;
		
		if (//maxgap > gapThreshold 
			//	|| 
				analizer.fullnessAnalizer.getBorderWeakness() > 0.6) {
			
			return false;
		}
		
		
		///
		
		//left/right balance check
		double l = analizer.fullnessAnalizer.getLeftCount();
		double r = analizer.fullnessAnalizer.getRightCount();		
		if (l > r * 2 || r > l * 2) {
			return false;
		}
		
		return true;
	}

	public double getHorSizeForGap(WorkingRect workingRect, int bestDiscr) {
		int maxHorizSize = workingRect.getTracePin() - workingRect.getTraceFrom();
		double horizontalSize = (double) maxHorizSize
				//* HoughDiscretizer.FACTORX_FROM
				/ HoughArray.FACTOR[bestDiscr];// * HoughArray.REDUCE[bestDiscr];
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
		
		//Sout.p("------ " + horSize + "   horSize " + horSize + "  lastHoughAindex " + lastHoughAindex );

		
		
		////
		
		BufferedImage img = analizer.additionalPreparator.getImage();
		Graphics2D g2 = (Graphics2D)img.getGraphics(); 
		g2.setColor(Color.BLACK);
		
		double goodCm = HalfHyperDst.getGoodSideDstPin(sgyFile, workingRect.getTracePin(), workingRect.getSmpPin());
		
		
		for (int i = 0; i < HoughDiscretizer.DISCRET_SIZE; i++) {
		
			double goodSpecific = goodCm / HoughArray.FACTOR[i];
			
			double goodDiagCm = getGoodHeightCm(sgyFile, workingRect.getTracePin(), workingRect.getSmpPin(), goodSpecific);
			
			
	
			int smp = Math.min(sgyFile.getMaxSamples() - 1,
					RulerTool.diagonalToSmp(sgyFile, workingRect.getTracePin(), workingRect.getSmpPin(), goodDiagCm)) - workingRect.getSmpPin();
			
			int x = sgyFile.getLeftDistTraceIndex(workingRect.getTracePin(), goodSpecific);
			g2.setColor(Color.BLACK);
			g2.drawLine(x, smp, x, smp);

			x = sgyFile.getRightDistTraceIndex(workingRect.getTracePin(), goodSpecific) - workingRect.getTraceFrom();
			g2.setColor(Color.WHITE);
			g2.drawLine(x, smp, x, smp);
			
		}
		
		
		
		hd = new HoughDraw(img, sgyFile, 
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

		return "old Hough scan";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}

	public HoughDraw getHoughDrawer() {
		return hd;
	}

}
