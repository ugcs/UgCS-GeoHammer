package com.ugcs.gprvisualizer.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import java.util.stream.IntStream;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class HyperFinder {
	
	//private static final int R = 160;
	
	TraceSample ts;
	
	//private static Map<Integer, Integer> map = ;
	
	final static float dash1[] = {1.0f, 7.0f};
	final static BasicStroke dashed =
	        new BasicStroke(1.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        10.0f, dash1, 0.0f);

	final static BasicStroke line1 =
	        new BasicStroke(1.0f);
	
	final static BasicStroke line2 =
	        new BasicStroke(2.0f);
	final static BasicStroke line4 =
	        new BasicStroke(4.0f);
	
	Color plusBest = new Color(100, 255, 100);
	Color plusGood = new Color(70, 180, 70); 
	Color plusBad = new Color(0, 111, 0);
	
	Color minusBest = new Color(100, 100, 255);
	Color minusGood = new Color(70, 70, 180); 
	Color minusBad = new Color(0, 0, 111);
	Model model;
	
	public HyperFinder(Model model) {
		this.model = model;
	}
	
	public void deleprocess() {
		
		System.out.println("finish");
		
		AppContext.notifyAll(new WhatChanged(Change.adjusting));
	}

	public void setPoint(TraceSample ts) {
		this.ts = ts;
	}
	
	public void drawHyperbolaLine(Graphics2D g2, ProfileField vField) {
		
		if(ts == null) {
			return;
		}
		
		int tr = ts.getTrace();
		List<Trace> traces = AppContext.model.getFileManager().getTraces();
		
		if(tr <0 || tr >= traces.size() || ts.getSample() < 0) {
			return;
		}
		
		Trace extr = traces.get(tr);
		
		float [] values = extr.getNormValues();
		if(ts.getSample() < 0 || ts.getSample() >= values.length) {
			return;
		}
		
		////
		SgyFile file = AppContext.model.getSgyFileByTrace(tr);
		HoughScan hs = new HoughScan();
		hs.print_log = true;
		hs.scan(file, tr-file.getOffset().getStartTrace(), ts.getSample());
		
		
		hs.hd.drawOnCut(g2, vField);
		//hs.printForPoint(tr-file.getOffset().getStartTrace(), ts.getSample());
		
		////
		
		double hyperkf = AppContext.model.getSettings().hyperkfc / 100.0;		
		float example2 = values[ts.getSample()];
		HalfHyper left2 = HalfHyper.getHalfHyper(traces, tr, ts.getSample(), example2, -1, hyperkf);		
		HalfHyper right2 = HalfHyper.getHalfHyper(traces, tr, ts.getSample(), example2, +1, hyperkf);		
		
		Point lt = vField.traceSampleToScreen(ts);

		///draw
		
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(lt.x-100, lt.y - 60, 200, 40);
		g2.setColor(Color.RED);

		g2.drawString("" + ts.getTrace() + " (" + extr.indexInFile + ") " + ts.getSample() + " (" + fl(example2) + ")   ofst: " + extr.verticalOffset,
				lt.x-100, lt.y - 40);
		
		g2.drawString(" l: " + fl(left2.oppositeAbovePerc) + " " + fl(left2.oppositeBelowPerc) + " <-|-> " +  
				" r: " +  fl(right2.oppositeAbovePerc) + " " + fl(right2.oppositeBelowPerc),				
				lt.x-100, lt.y - 30);
		
		g2.setColor(Color.CYAN);
		g2.setStroke(line4);
		
		//drawHyperbolaLine2(g2, vField);		
	}
	
	public void drawHalfHyperLine(Graphics2D g2, ProfileField vField, HalfHyper hh, int voffst) {
		
		
		boolean positive = hh.example>0;
		double hyperkf = AppContext.model.getSettings().hyperkfc / 100.0;
		int goodside = (int)(HalfHyper.getGoodSideSize(hh.pinnacle_smp) / hyperkf);
		if(hh.length >= goodside ) {
			
			if(hh.isGood()) {
				g2.setStroke(line4);
				g2.setColor(positive ? plusBest : minusBest);
			}else {
				g2.setStroke(line2);
				g2.setColor(positive ? plusGood : minusGood);
			}
			
		}else {
			g2.setStroke(dashed);			
			g2.setColor(positive ? plusBad : minusBad);
		}
		
		
		
		Point prev = null;
		for(int i=0; i<hh.length; i++) {
			Point lt = vField.traceSampleToScreen(new TraceSample(hh.pinnacle_tr + i * hh.side, hh.smp[i]));
			if(prev != null) {
				g2.drawLine(prev.x, prev.y+voffst, lt.x, lt.y+voffst);				
			}
			
			prev = lt;
		}
		
	}
	
	
	public double getThreshold() {
		double thr = (double)AppContext.model.getSettings().hyperSensitivity.intValue() / 100.0;
		return thr;
	}
	
	
	public static double THRESHOLD = 0.7;
	public void drawHyperbolaLine2(Graphics2D g2, ProfileField vField) {
		
		double thr = getThreshold();
		
		int tr = ts.getTrace();
		int smp = ts.getSample();
		
		SgyFile sgyFile = AppContext.model.getSgyFileByTrace(tr);
		int traceInFile = tr - sgyFile.getOffset().getStartTrace();
		
		//List<Trace> traces = AppContext.model.getFileManager().getTraces();
		//double x_factor = AppContext.model.getSettings().hyperkfc/100.0;
		
		for(double x_factor = AlgorithmicScan.X_FACTOR_FROM; x_factor <= AlgorithmicScan.X_FACTOR_TO; x_factor += AlgorithmicScan.X_FACTOR_STEP) {
			drawHyperSingleLine(g2, vField, thr, smp, sgyFile, traceInFile, x_factor);
		}
		
		
	}

	public void drawHyperSingleLine(Graphics2D g2, ProfileField vField, double thr, int smp, SgyFile sgyFile,
			int traceInFile, double x_factor) {
		HalfHyperDst lft = HalfHyperDst.getHalfHyper(sgyFile, traceInFile, smp, -1, x_factor);
		double lftRate = lft.analize(100);
		
		HalfHyperDst rht = HalfHyperDst.getHalfHyper(sgyFile, traceInFile, smp, +1, x_factor);
		double rhtRate = rht.analize(100);
		
				
		boolean lftGood = lftRate > thr;		
		g2.setColor(lftGood ? Color.RED : Color.CYAN);
		g2.setStroke(lftGood ? line2 : line1);
		
		drawHHDst(g2, vField, sgyFile.getOffset(), lft);
		
		boolean rhtGood = rhtRate > thr;		
		g2.setColor(rhtGood ? Color.RED : Color.CYAN);
		g2.setStroke(rhtGood ? line2 : line1);
		drawHHDst(g2, vField, sgyFile.getOffset(), rht);
	}

	public void drawHHDst(Graphics2D g2, ProfileField vField, VerticalCutPart  offset, HalfHyperDst lft) {
		Point prev = null;
		for(int i=0; i<lft.length; i++) {
			
			int traceIndex = offset.localToGlobal(lft.pinnacle_tr + i * lft.side);
			
			Point lt = vField.traceSampleToScreenCenter(new TraceSample(traceIndex, lft.smp[i]));
			if(prev != null) {
				g2.drawLine(prev.x, prev.y, lt.x, lt.y);
			}
			
			prev = lt;
		}
	}	
	
	
	public void drawHyperbolaLine(Graphics2D g2, ProfileField vField, int smp, int lft, int rht, int voffst) {
		if(ts == null) {
			return;
		}
		
		
		Point prev = null;
		
		double kf = AppContext.model.getSettings().hyperkfc/100.0;
		
		int tr = ts.getTrace();
		int s = lft;
		int f = rht;
		
		double y = smp;
		
		for(int i=s; i<= f; i++) {
			
			double x=(i-tr) * kf;
			double c = Math.sqrt(x*x+y*y);
			//g2.setColor(Math.abs(x) < y/2 ? Color.RED:Color.GRAY ); 
			
			Point lt = vField.traceSampleToScreen(new TraceSample(i, (int)c));
			if(prev != null) {
				g2.drawLine(prev.x, prev.y+voffst, lt.x, lt.y+voffst);				
			}
			
			prev = lt;
		}
	}
	
	
	String fl(double d) {
		return String.format(" %.2f ", d);
	}
	
	
}
