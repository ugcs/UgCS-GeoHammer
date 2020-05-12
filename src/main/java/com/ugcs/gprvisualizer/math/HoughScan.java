package com.ugcs.gprvisualizer.math;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.auxcontrol.RulerTool;
import com.ugcs.gprvisualizer.app.commands.AsinqCommand;
import com.ugcs.gprvisualizer.app.commands.Command;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.PrismDrawer;
import com.ugcs.gprvisualizer.gpr.Model;

public class HoughScan implements AsinqCommand {

	public static final int DISCRET_SIZE = 22;
	private static final double DISCRET_FROM = 0.5;
	private static final double DISCRET_TO = 1.5;

	static int statcount = 0;
	static int statmax = 0;
	public boolean print_log = false;

	Model model = AppContext.model;

	HoughDraw hd = null;

	@Override
	public void execute(SgyFile file) {

		new LevelScanner().execute(file);
		new EdgeFinder().execute(file);
		new EdgeSubtractGround().execute(file);

		//
		int maxSmp = Math.min(AppContext.model.getSettings().layer + AppContext.model.getSettings().hpage,
				file.getMaxSamples() - 2);

		double threshold = model.getSettings().hyperSensitivity.doubleValue();

		for (int pincl_tr = 0; pincl_tr < file.size(); pincl_tr++) {
			if (pincl_tr % 100 == 0) {
				Sout.p("tr " + pincl_tr + "/" + file.size());
			}
			Trace tr = file.getTraces().get(pincl_tr);
			tr.good = new int[file.getMaxSamples()];

			for (int pincl_smp = AppContext.model.getSettings().layer; pincl_smp < maxSmp; pincl_smp++) {
				double s = scan(file, pincl_tr, pincl_smp);

				// maxf = Math.max(s, maxf);

				if (s > threshold) {
					// Sout.p("^ "+ s + " " + pincl_tr);
					tr.good[pincl_smp] = 3;
				}
			}
		}
	}	

	public double scan(SgyFile sgyFile, int pincl_tr, int pincl_smp) {
		
		// TODO:
		// sides
		// good size
		
		double good_cm = HalfHyperDst.getGoodSideDstPin(sgyFile, pincl_tr, pincl_smp);
		double pin_v_cm = RulerTool.distanceCm(sgyFile, pincl_tr, pincl_tr, 0, pincl_smp);
		
		double vd_cm = Math.sqrt(good_cm*good_cm + pin_v_cm*pin_v_cm);		
		//int grn = sgyFile.groundProfile.deep[pincl_tr];
		//double grn_cm = grn * sgyFile.getSamplesToCmAir();
		//int vdiapason = (int)(vd_cm / sgyFile.getSamplesToCmGrn());
		
		int tr_from = sgyFile.getLeftDistTraceIndex(pincl_tr, good_cm);
		int tr_to = sgyFile.getRightDistTraceIndex(pincl_tr, good_cm);
		
		int smp_from = pincl_smp;
		int smp_to = Math.min(sgyFile.getMaxSamples()-1, RulerTool.diagonalToSmp(sgyFile, pincl_tr, pincl_smp, vd_cm) );
		
		
		BufferedImage img = null;
		int[] buffer = null;
		int width = tr_to-tr_from+1;
		int height = smp_to-smp_from+1;
		
		if(print_log) {
			Sout.p("smp:  "+ smp_from + " - "+ smp_to + "  horiz good  " + good_cm + "   tr " + tr_from + " - " + tr_to);
			
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			buffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			
		}		
		
		Store stores[] = new Store[5];
		for(int i=0;i<stores.length; i++) {
			stores[i] = new Store();			
		}		
		
		int showIndex = model.getSettings().printHoughAindex.intValue();
		
		
		//
		for(int smp = smp_from; smp <= smp_to; smp++) {
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("%3d | ", smp));
			
			double value = (smp == smp_from ? 0.33 : 1.0); 
			
			for(int tr = tr_from; tr <= tr_to; tr++) {
				
				
				int edge = getEdge(sgyFile, tr, smp);
				if(/*edge == 0 ||*/ tr == pincl_tr) {
					continue;
				}
				
				double xf1 = xfact(sgyFile, pincl_tr, pincl_smp, tr, smp+0.01);
				//overlapping
				double xf2 = xfact(sgyFile, pincl_tr, pincl_smp, tr, smp+1.99);
				
//				if((xf1 < DISCRET_FROM || xf1 > DISCRET_TO) && (xf2 < DISCRET_FROM || xf2 > DISCRET_TO)) {
//					continue;
//				}
				
				int xfd1 = discret(xf1);
				int xfd2 = discret(xf2);
				
				
				
				stores[edge].add(xfd1, xfd2, value);
				
				if(print_log) {
					String s;
					
					if(xfd1 <=showIndex && xfd2 >= showIndex || xfd2 <= showIndex && xfd1 >= showIndex) {
						s = "" + edge;
					}else {
						s = " ";
						buffer[tr-tr_from + width * (smp-smp_from)] = 0xDD505070;
					}
					
					sb.append(s);
				}				
				
			}
			
			if(print_log) {
				//Sout.p(sb.toString());
			}
		}
		
		//print(stores);
		int aux[][] = new int[5][2];
		double[] best = getMax(stores, aux);
		
		
		//double res = Arrays.stream(best).max().getAsDouble();
		int mxIndex = 0;
		for(int i=1; i<5;i++){
		
			if(best[i] > best[mxIndex]){
				mxIndex = i;
			}
		}
		
		if(print_log) {
			Sout.p("res "+ best[mxIndex]);
			
			hd = new HoughDraw(img, sgyFile, tr_from, tr_to+1, smp_from, smp_to+1);
			
			hd.resedge = aux[mxIndex][0];
			hd.resindex = aux[mxIndex][1];
			hd.res = best[mxIndex];
			
		}
		return best[mxIndex];
	}

	public void printForPoint(int tr, int smp) {

		int pincl_smp = model.getSettings().layer;
		SgyFile file = model.getSgyFileByTrace(model.getVField().getSelectedTrace());
		int pincl_tr = model.getVField().getSelectedTrace() - file.getOffset().getStartTrace();

		double xf1 = xfact(file, pincl_tr, pincl_smp, tr, smp + 0.02);
		double xf2 = xfact(file, pincl_tr, pincl_smp, tr, smp + 1.98);

		int xfd1 = discret(xf1);
		int xfd2 = discret(xf2);

		Sout.p(" " + (tr - pincl_tr) + " " + (smp - pincl_smp) + " ->  " + Math.min(xfd1, xfd2) + " - "
				+ Math.max(xfd1, xfd2));

	}

	private void print(Store[] stores) {

		if (!print_log) {
			return;
		}

		for (int i = 1; i < stores.length; i++) {

			Store s = stores[i];

			Sout.p("edge " + i + " = " + Arrays.toString(s.a));

		}

	}

	public double[] getMax(Store[] stores, int aux[][] ) {
		double[] best = new double[5];
		for (int i = 1; i < stores.length; i++) {

			int index = stores[i].getMaxIndex();
			if (index > 2) {
				best[i] = stores[i].getMax();
			}

			if (print_log) {
			
				aux[i][0] = i;
				aux[i][1] = index;
				
				StringBuilder sb = new StringBuilder();
				for(int z=0; z<stores[i].a.length; z++){
					sb.append(String.format(" %.2f", stores[i].a[z]));
				}
				
				Sout.p(String.format("edge %2d ind %3d - maxval %.2f = " , i, index, stores[i].getMax()) + sb.toString());
			}
		}
		return best;
	}

	private double xfact(SgyFile sgyFile, int pincl_tr, double pincl_smp, int tr, double smp) {
		// TODO: check again
		double c_cm = RulerTool.distanceCm(sgyFile, pincl_tr, pincl_tr, 0, smp);
		double y_cm = RulerTool.distanceCm(sgyFile, pincl_tr, pincl_tr, 0, pincl_smp);
		double x_cm_ideal = Math.sqrt(c_cm * c_cm - y_cm * y_cm);

		double x_cm_real = RulerTool.distanceCm(sgyFile, pincl_tr, tr, smp, smp);

		double x_factor = x_cm_ideal / x_cm_real;

		return x_factor;
	}

	/*
	 * 0 - except
	 */
	private int discret(double x_factor) {
		// to 0-1
		double norm = (x_factor - DISCRET_FROM) / (DISCRET_TO - DISCRET_FROM);

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


	static double FACTOR[] = new double[DISCRET_SIZE];
	static {
		for(int i=0; i<DISCRET_SIZE; i++){
			FACTOR[i] = (DISCRET_FROM + (double) i / (double) DISCRET_SIZE * (DISCRET_TO - DISCRET_FROM));
			Sout.p("f " + i + " = " + FACTOR[i]);
		}
	}

	class Store {
		double a[] = new double[DISCRET_SIZE];

		public void clear() {
			Arrays.fill(a, 0);
		}

		public void add(int _from, int _to, double value) {

			int from = Math.min(_from, _to);
			int to = Math.max(_from, _to);
			if (to < 0 || from >= DISCRET_SIZE) {
				return;
			}

			from = GHUtils.norm(from, 0, DISCRET_SIZE);
			to = GHUtils.norm(to, 0, DISCRET_SIZE);

			for (int i = from; i <= to; i++) {
				a[i]+=value;
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
			// int val = Arrays.stream(a).max().getAsInt();
//			double max = 0;
//			for(int i=0; i<a.length; i++) {
//
//				double v = (double)a[i] * (DISCRET_FROM + (double)i / (double)DISCRET_SIZE * (DISCRET_TO-DISCRET_FROM));
//				
//				max = Math.max(max, v);
//			}

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

	public static void main2(String args[]) {

		Sout.p("start");
		SgyFile sgyFile = new SgyFile();

		// sgyFile.getBinaryHeader().getSampleInterval() / 1000.0;
		sgyFile.setBinaryHeader(new BinaryHeader());
		sgyFile.getBinaryHeader().setSampleInterval((short) 104);
		sgyFile.setTraces(new ArrayList<>());
		for (int i = 0; i < 110; i++) {
			Trace t = new Trace(new byte[200], null, new float[200], new LatLon(10, 10));
			t.setPrevDist(1.25);
			t.setOriginalValues(new float[200]);
			sgyFile.getTraces().add(t);
		}
		sgyFile.groundProfile = new HorizontalProfile(110);
		Arrays.fill(sgyFile.groundProfile.deep, 65);
		sgyFile.groundProfile.finish(sgyFile.getTraces());

		int pincl_tr = 50;
		int pincl_smp = 100;

		HoughScan hs = new HoughScan();

		for (int smp = 100; smp <= 145; smp++) {

			StringBuilder sb = new StringBuilder();
			for (int tr = 0; tr <= 100; tr++) {

				double xf1 = hs.xfact(sgyFile, pincl_tr, pincl_smp, tr, smp + 0.01);
				double xf2 = hs.xfact(sgyFile, pincl_tr, pincl_smp, tr, smp + 0.99);

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
