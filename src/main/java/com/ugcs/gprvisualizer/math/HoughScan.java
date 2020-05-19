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

	public static final int DISCRET_SIZE = 22;
	public static final double DISCRET_FROM = 0.5;
	public static final double DISCRET_TO = 1.5;

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

		new LevelScanner().execute(file);
		new EdgeFinder().execute(file);
		new EdgeSubtractGround().execute(file);

		//
		int maxSmp = Math.min(AppContext.model.getSettings().layer 
				+ AppContext.model.getSettings().hpage,
				file.getMaxSamples() - 2);

		double threshold = model.getSettings().hyperSensitivity.doubleValue();

		for (int pinTr = 0; pinTr < file.size(); pinTr++) {
			if (pinTr % 300 == 0) {
				Sout.p("tr " + pinTr + "/" + file.size());
			}
			Trace tr = file.getTraces().get(pinTr);
			tr.good = new int[file.getMaxSamples()];

			for (int pinSmp = AppContext.model.getSettings().layer; 
					pinSmp < maxSmp; pinSmp++) {
				double s = scan(file, pinTr, pinSmp);

				if (s > threshold) {
					tr.good[pinSmp] = 3;
				}
			}
		}
		
		new ScanGood().execute(file);
	}	

	public double scan(SgyFile sgyFile, int pinTrace, int pinSmpl) {
		
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
		
		HoughScanPinncaleAnalizer analizer = new HoughScanPinncaleAnalizer();
		
		if (isPrintLog) {
			
			analizer.additionalPreparator = 
					new HoughImgPreparator(workingRect, 
						model.getSettings().printHoughAindex.intValue());
		}		
		
		HoughArray[] stores = analizer.processRectAroundPin(sgyFile, workingRect);
		
		int[][] aux = new int[5][2];
		double[] best = getMax(stores, aux);
		
		int mxIndex = bestOfTheBestestIndex(best);
		
		if (isPrintLog) {
			Sout.p("res " + best[mxIndex]);
			
			hd = new HoughDraw(analizer.additionalPreparator.getImage(), sgyFile, 
					workingRect.getTraceFrom(), workingRect.getTraceTo() + 1,
					workingRect.getSmpFrom(), workingRect.getSmpTo() + 1);
			
			hd.resedge = aux[mxIndex][0];
			hd.resindex = aux[mxIndex][1];
			hd.res = best[mxIndex];
			
		}
		return best[mxIndex];
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

	public double[] getMax(HoughArray[] stores, int[][] aux) {
		double[] best = new double[5];
		for (int i = 1; i < stores.length; i++) {

			int index = stores[i].getMaxIndex();
			if (index > 2) {
				best[i] = stores[i].getMax();
			}

			if (isPrintLog) {
			
				aux[i][0] = i;
				aux[i][1] = index;
				
				StringBuilder sb = new StringBuilder();
				for (int z = 0; z < stores[i].ar.length; z++) {
					sb.append(String.format(" %.2f", stores[i].ar[z]));
				}
				
				Sout.p(String.format("edge %2d ind %3d - maxval %.2f = ", 
						i, index, stores[i].getMax()) 
						+ sb.toString());
			}
		}
		return best;
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
	

//	public static void main(String [] args) throws Exception {
//		
//		SgyFile file = new SgyFile();
//		
//		file.open(new File(""));
//		
//		new LevelScanner().execute(file);
//		
//		new HoughScan().execute(file);
//		
//		new PrismDrawer(AppContext.model).;
//		
//	}

//	public static void main2(String[] args) {
//
//		Sout.p("start");
//		SgyFile sgyFile = new SgyFile();
//
//		// sgyFile.getBinaryHeader().getSampleInterval() / 1000.0;
//		sgyFile.setBinaryHeader(new BinaryHeader());
//		sgyFile.getBinaryHeader().setSampleInterval((short) 104);
//		sgyFile.setTraces(new ArrayList<>());
//		for (int i = 0; i < 110; i++) {
//			Trace t = new Trace(new byte[200], null, new float[200], 
//					new LatLon(10, 10));
//			t.setPrevDist(1.25);
//			t.setOriginalValues(new float[200]);
//			sgyFile.getTraces().add(t);
//		}
//		sgyFile.groundProfile = new HorizontalProfile(110);
//		Arrays.fill(sgyFile.groundProfile.deep, 65);
//		sgyFile.groundProfile.finish(sgyFile.getTraces());
//
//		int pinTr = 50;
//		int pinSmp = 100;
//
//		HoughScan hs = new HoughScan();
//
//		for (int smp = 100; smp <= 145; smp++) {
//
//			StringBuilder sb = new StringBuilder();
//			for (int tr = 0; tr <= 100; tr++) {
//
//				double xf1 = hs.getFactorX(sgyFile, pinTr, pinSmp, tr, smp + 0.01);
//				double xf2 = hs.getFactorX(sgyFile, pinTr, pinSmp, tr, smp + 0.99);
//
//				int xfd1 = hs.discret(xf1);
//				int xfd2 = hs.discret(xf2);
//
//				String s = String.format("[%2d %2d]", xfd1, xfd2);
//				sb.append(s);
//
//			}
//			Sout.p(sb.toString());
//		}
//
//		Sout.p("finisih");
//	}

	
	
	
}
