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
	private static final double DISCRET_FROM = 0.5;
	private static final double DISCRET_TO = 1.5;

	public boolean isPrintLog = false;

	@Autowired
	private Model model;

	HoughDraw hd = null;

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

	public double scan(SgyFile sgyFile, int pinTr, int pinSmp) {
		
		double goodCm = HalfHyperDst.getGoodSideDstPin(sgyFile, pinTr, pinSmp);
		double vertDstToPinCm = RulerTool.distanceCm(sgyFile, pinTr, pinTr, 0, pinSmp);
		
		double goodDiagCm = Math.sqrt(
				goodCm * goodCm + vertDstToPinCm * vertDstToPinCm);		
		
		int trFrom = sgyFile.getLeftDistTraceIndex(pinTr, goodCm);
		int trTo = sgyFile.getRightDistTraceIndex(pinTr, goodCm);
		
		int smpFrom = pinSmp;
		int smpTo = Math.min(sgyFile.getMaxSamples() - 1,
				RulerTool.diagonalToSmp(sgyFile, pinTr, pinSmp, goodDiagCm));
		
		
		BufferedImage img = null;
		int[] buffer = null;
		int width = trTo - trFrom + 1;
		int height = smpTo - smpFrom + 1;
		
		if (isPrintLog) {
			Sout.p("smp:  " + smpFrom 
					+ " - " + smpTo 
					+ "  horiz good  " + goodCm 
					+ "   tr " + trFrom + " - " + trTo);
			
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			buffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			
		}		
		
		Store[] stores = new Store[5];
		for (int i = 0; i < stores.length; i++) {
			stores[i] = new Store();			
		}		
		
		int showIndex = model.getSettings().printHoughAindex.intValue();
		
		
		//
		for (int smp = smpFrom; smp <= smpTo; smp++) {
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("%3d | ", smp));
			
			double value = (smp == smpFrom ? 0.33 : 1.0); 
			
			for (int tr = trFrom; tr <= trTo; tr++) {
				
				
				int edge = getEdge(sgyFile, tr, smp);
				if (tr == pinTr) {
					continue;
				}
				
				double xf1 = xfact(sgyFile, pinTr, pinSmp, tr, smp + 0.01);
				//overlapping
				double xf2 = xfact(sgyFile, pinTr, pinSmp, tr, smp + 1.99);
				
				int xfd1 = discret(xf1);
				int xfd2 = discret(xf2);
				
				stores[edge].add(xfd1, xfd2, value);
				
				if (isPrintLog) {
					String s;
					
					if (xfd1 <= showIndex && xfd2 >= showIndex 
							|| xfd2 <= showIndex && xfd1 >= showIndex) {
						s = "" + edge;
					} else {
						s = " ";
						int index = tr - trFrom + width * (smp - smpFrom);
						buffer[index] = 0xDD505070;
					}
					
					sb.append(s);
				}				
				
			}
			
			if (isPrintLog) {
				//Sout.p(sb.toString());
			}
		}
		
		//print(stores);
		int[][] aux = new int[5][2];
		double[] best = getMax(stores, aux);
		
		
		//double res = Arrays.stream(best).max().getAsDouble();
		int mxIndex = 1;
		for (int i = 1; i < 5; i++) {
		
			if (best[i] > best[mxIndex]) {
				mxIndex = i;
			}
		}
		
		if (isPrintLog) {
			Sout.p("res " + best[mxIndex]);
			
			hd = new HoughDraw(img, sgyFile, trFrom, trTo + 1, smpFrom, smpTo + 1);
			
			hd.resedge = aux[mxIndex][0];
			hd.resindex = aux[mxIndex][1];
			hd.res = best[mxIndex];
			
		}
		return best[mxIndex];
	}

	public void printForPoint(int tr, int smp) {

		int pinSmp = model.getSettings().layer;
		SgyFile file = model.getSgyFileByTrace(model.getVField().getSelectedTrace());
		int pinTr = model.getVField().getSelectedTrace() - file.getOffset().getStartTrace();

		double xf1 = xfact(file, pinTr, pinSmp, tr, smp + 0.02);
		double xf2 = xfact(file, pinTr, pinSmp, tr, smp + 1.98);

		int xfd1 = discret(xf1);
		int xfd2 = discret(xf2);

		Sout.p(" " + (tr - pinTr) + " " + (smp - pinSmp) 
				+ " ->  " + Math.min(xfd1, xfd2) + " - "
				+ Math.max(xfd1, xfd2));

	}

	private void print(Store[] stores) {

		if (!isPrintLog) {
			return;
		}

		for (int i = 1; i < stores.length; i++) {
			Store s = stores[i];
			Sout.p("edge " + i + " = " + Arrays.toString(s.a));
		}
	}

	public double[] getMax(Store[] stores, int[][] aux) {
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
				for (int z = 0; z < stores[i].a.length; z++) {
					sb.append(String.format(" %.2f", stores[i].a[z]));
				}
				
				Sout.p(String.format("edge %2d ind %3d - maxval %.2f = ", 
						i, index, stores[i].getMax()) 
						+ sb.toString());
			}
		}
		return best;
	}

	private double xfact(SgyFile sgyFile, int pinTr, double pinSmp, int tr, double smp) {

		double diagCm = RulerTool.distanceCm(sgyFile, pinTr, pinTr, 0, smp);
		double y_cm = RulerTool.distanceCm(sgyFile, pinTr, pinTr, 0, pinSmp);
		double x_cm_ideal = Math.sqrt(diagCm * diagCm - y_cm * y_cm);

		double x_cm_real = RulerTool.distanceCm(sgyFile, pinTr, tr, smp, smp);

		double factorX = x_cm_ideal / x_cm_real;

		return factorX;
	}

	/*
	 * 0 - except
	 */
	private int discret(double factorX) {
		// to 0-1
		double norm = (factorX - DISCRET_FROM) / (DISCRET_TO - DISCRET_FROM);

		if (norm < 0) {
			return -1;
		}

		if (norm > 1) {
			return DISCRET_SIZE;
		}

		return (int) Math.round(norm * (DISCRET_SIZE - 1));
	}

	private int getEdge(SgyFile sgyFile, int tr, int smp) {

		return sgyFile.getTraces().get(tr).edge[smp];
	}


	static double[] FACTOR = new double[DISCRET_SIZE];

	static {
		for (int i = 0; i < DISCRET_SIZE; i++) {
			FACTOR[i] = (DISCRET_FROM + 
				(double) i 
				/ (double) DISCRET_SIZE 
				* (DISCRET_TO - DISCRET_FROM));
		}
	}

	class Store {
		double[] a = new double[DISCRET_SIZE];

		public void clear() {
			Arrays.fill(a, 0);
		}

		public void add(int _from, int _to, double value) {

			int from = Math.min(_from, _to);
			int to = Math.max(_from, _to);
			if (to < 0 || from >= DISCRET_SIZE) {
				return;
			}

			from = MathUtils.norm(from, 0, DISCRET_SIZE);
			to = MathUtils.norm(to, 0, DISCRET_SIZE);

			for (int i = from; i <= to; i++) {
				a[i] += value;
			}
		}

		int getMaxIndex() {
			double max = 0;
			int index = 0;
			for (int i = 0; i < a.length; i++) {

				double v = a[i] * FACTOR[i];

				if (v > max) {
					max = v;
					index = i;
				}
			}

			return index;

		}

		double getMax() {

			int index = getMaxIndex();
			double v = a[index] * FACTOR[index];

			return v;
		}

		void print() {
			StringBuilder sb = new StringBuilder();
			sb.append(" debug a ");
			for (int i = 0; i < a.length; i++) {
				sb.append(" ");
				sb.append(a[i]);
			}
			Sout.p(sb.toString());
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

	public static void main2(String[] args) {

		Sout.p("start");
		SgyFile sgyFile = new SgyFile();

		// sgyFile.getBinaryHeader().getSampleInterval() / 1000.0;
		sgyFile.setBinaryHeader(new BinaryHeader());
		sgyFile.getBinaryHeader().setSampleInterval((short) 104);
		sgyFile.setTraces(new ArrayList<>());
		for (int i = 0; i < 110; i++) {
			Trace t = new Trace(new byte[200], null, new float[200], 
					new LatLon(10, 10));
			t.setPrevDist(1.25);
			t.setOriginalValues(new float[200]);
			sgyFile.getTraces().add(t);
		}
		sgyFile.groundProfile = new HorizontalProfile(110);
		Arrays.fill(sgyFile.groundProfile.deep, 65);
		sgyFile.groundProfile.finish(sgyFile.getTraces());

		int pinTr = 50;
		int pinSmp = 100;

		HoughScan hs = new HoughScan();

		for (int smp = 100; smp <= 145; smp++) {

			StringBuilder sb = new StringBuilder();
			for (int tr = 0; tr <= 100; tr++) {

				double xf1 = hs.xfact(sgyFile, pinTr, pinSmp, tr, smp + 0.01);
				double xf2 = hs.xfact(sgyFile, pinTr, pinSmp, tr, smp + 0.99);

				int xfd1 = hs.discret(xf1);
				int xfd2 = hs.discret(xf2);

				String s = String.format("[%2d %2d]", xfd1, xfd2);
				sb.append(s);

			}
			Sout.p(sb.toString());
		}

		Sout.p("finisih");
	}

	
	
	
}
