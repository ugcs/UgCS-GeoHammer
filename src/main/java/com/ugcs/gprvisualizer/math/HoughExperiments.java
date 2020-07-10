package com.ugcs.gprvisualizer.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.GprFile;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.auxcontrol.RulerTool;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;

public class HoughExperiments {

	private static final int BAD_MARGIN = 4;
	private static final int BAD_INCR = 2;

	private static final double HYP_MAX = 0.25;
	
	public boolean print = false; 
	private SgyFile file;
	public int traceFrom;
	public int traceTo;
	public int realTraceFrom;
	public int realTraceTo;
	
	
	private int tracePin;
	private int smpPin;	
	
	int lookingEdge;
		
	//real dist to pin 
	private double y;
	private double shift;
	
	int[] leftStart = new int[50];
	int[] leftFinish = new int[50];
	int[] rightStart = new int[50];
	int[] rightFinish = new int[50];
	public int lastSmp;
	
	RealSizeCalculator rsc;

	
	static public HoughExperiments f(SgyFile file, int tr, int smp, double heightShift, int lookingEdge, boolean print) {
		HoughExperiments he = new HoughExperiments();
		he.print = print;
		
		he.file = file;
		//he.edge = edge;
		
		
		he.smpPin = smp;
		he.tracePin = tr;
		he.y = RulerTool.distanceCm(file, he.tracePin, he.tracePin, 0, he.smpPin);		
		he.shift = heightShift;
		he.lookingEdge = lookingEdge;
		
		he.init();
		
		return he;
	}
	

	public boolean criteriaGoodCount() { 
		int compare = getMaxWidth() * 25 / 100;
		
		boolean res = good > compare;
		if (print) {
			Sout.p("criteriaGoodCount         " + good + "  <>  " + compare + " = " + res);
		}
		
		return res;
	}
	
	public boolean criteriaRealWidth() {
		
		int min = getMaxWidth() * 65 / 100;
		
		//add header
		for (int i = leftStart[0]; i <= rightFinish[0]; i++) {
			if (file.getEdge(i, smpPin) == lookingEdge 
				|| file.getEdge(i, smpPin - 1) == lookingEdge ) {
				rsc.add(i);
			}
		}
		
		realTraceFrom = tracePin - rsc.getLeft();
		realTraceTo = tracePin + rsc.getRight();
		
		int realWidth = realTraceTo - realTraceFrom + 1;
		
		boolean res = realWidth > min;
		if (print) {
			Sout.p("criteriaRealW " + maxgap + "(" + realTraceFrom + " - " + realTraceTo + ")"+ "  rw " + realWidth  + "  <>  " + min + " = " + res);
		}
		
		return res;
	}
	
	public boolean criteriaRealMinLeft() {
		
	
		int lft = tracePin - realTraceFrom;
		int rgh = realTraceTo - tracePin;
		
		int min = (tracePin - traceFrom) / 2;
		
		boolean res = lft > min;
		if (print) {
			Sout.p("criteriaRealMinLeft " + lft + "  <>  " + min + " = " + res);
		}
		
		return res;
	}	

	public boolean criteriaRealMinRight() {
		
		
		int rgh = realTraceTo - tracePin;
		
		int min = (traceTo - tracePin) / 2;
		
		boolean res = rgh > min;
		if (print) {
			Sout.p("criteriaRealMinRight " + rgh + "  <>  " + min + " = " + res);
		}
		
		return res;
	}	
	
	public boolean criteriaGoodBadRatio() {
		good = 0;
		bad = 0;
		for (int smp = smpPin + 1; smp < lastSmp; smp++) {
			
			int fromMin = Math.max(realTraceFrom - BAD_INCR, 0);
			int from = leftStart[smp - smpPin] - BAD_MARGIN;
			
			checkRow(smp, Math.max(fromMin, from), 
					leftFinish[smp - smpPin] + BAD_MARGIN);
			
			
			int toMax = Math.min(realTraceTo + BAD_INCR, file.size()-1);
			int to = rightStart[smp - smpPin] + BAD_MARGIN;
			checkRow(smp, 
					rightStart[smp - smpPin] - BAD_MARGIN, 
					Math.min(toMax, to));
			
		}
		
		int compare = bad * 2;
		boolean res = good > compare; 
			
		if (print) {
			Sout.p("criteria gd/bad    " + good + "  <>  " + compare + " = " + res);
		}
		
		return res;
	}

	

	private void checkRow(int smp, int from, int to) {
		for (int t = from; t <= to; t++) {
			if (file.getEdge(t, smp) == lookingEdge) {
				addPoint(t, smp);
			}
		}		
	}


	//getRealSizeTraces()
	//getRealSizeRelative()
	
	
	public void init() {
		
		traceFrom = tracePin;
		traceTo = tracePin;

		for (int i = 0; i < leftStart.length; i++) {
			lastSmp = i + smpPin;
			
			int leftstart = getLeft(lastSmp + 1, shift * 1.25);
			int leftfinish = getLeft(lastSmp, shift);
			
			int rightstart = getRight(lastSmp, shift);
			int rightfinish = getRight(lastSmp + 1, shift * 1.25);
			
			if (leftstart < 0 || leftfinish < 0 || rightstart < 0 || rightfinish < 0) {
				break;
			}
			
			traceFrom = Math.min(traceFrom, leftstart);
			traceTo = Math.max(traceTo, rightfinish);
			
			leftStart[i] = leftstart;
			leftFinish[i] = leftfinish;
			rightStart[i] = rightstart;
			rightFinish[i] = rightfinish;
		}
		
		maxgap = getMaxWidth() / 15;
		
		
		
		rsc = new RealSizeCalculator(tracePin, traceFrom, traceTo, maxgap, tracePin, tracePin);//leftFinish[1], rightStart[1]);
		rsc.print = print;
	}
	
	int maxgap;
	int good = 0;
	int bad = 0;
	
	public int analize() {
		
		for (int i = 0; i < lastSmp - smpPin; i++) {
			int smp = i + smpPin;
			
			analizeRow(smp, leftStart[i], leftFinish[i]);
			analizeRow(smp, rightStart[i], rightFinish[i]);
		}
		
		
		return 0;
	}
	
	private void analizeRow(int smp, int from, int to) {
		
		for (int tr = from; tr <= to; tr++) {
			if (file.getTraces().get(tr).edge[smp] == 4) {
				addPoint(tr, smp);
			}
		}
	}



	public void addPoint(int tr, int smp) {
		
		if (inGood(tr, smp, 0)) {
			good++;
			
			rsc.add(tr);
		} else if (inGood(tr, smp, BAD_MARGIN)) {
			bad++;
		}
		

	}	
	
	private boolean inGood(int tr, int smp, int range) {
		int index = smp - smpPin;
		
		if (index < 0 || index >= lastSmp) {
			return false;
		}
		
		return tr >= leftStart[smp - smpPin] - range 
				&& tr <= leftFinish[smp - smpPin] + range 
				|| tr >= rightStart[smp - smpPin] - range
				&& tr <= rightFinish[smp - smpPin] + range;
	}



//	public static void main(String[] args) throws Exception {
//		HoughExperiments h = new HoughExperiments();
//		
//		GprFile file = new GprFile();
//		file.open(new File("c:\\work\\geodata\\balozi3\\8\\2020-03-05-14-10-01-ch2-gpr_004.sgy"));
//		new LevelScanner().execute(file);
//		
//		h.file = file;
//		h.smpPin = 100;
//		h.tracePin = 550;
//		h.y = RulerTool.distanceCm(file, h.tracePin, h.tracePin, 0, h.smpPin);
//		
//		System.out.println("h.y "+h.y);
//		//h.scan();
//		
//	}

	public void draw(Graphics2D g2, ProfileField field) {
		VerticalCutPart of = file.getOffset();
		
		for (int smp = smpPin; smp < lastSmp; smp++) {
			
			int index = smp - smpPin;
			int leftstart = leftStart[index];
			int leftfinish = leftFinish[index];
			int rightstart = rightStart[index];
			int rightfinish = rightFinish[index];
			
			//System.out.println(leftstart + " - " + leftfinish);
			
			Point p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(leftstart), smp));
			Point p2 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(leftfinish), smp));
			g2.drawLine(
					p1.x, p1.y, 
					p2.x, p2.y);
			
			p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(rightstart), smp));
			p2 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(rightfinish), smp));
			g2.drawLine(
					p1.x, p1.y, 
					p2.x, p2.y);
		}
		
		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(1));
		//draw real tr
		Point p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(realTraceFrom), smpPin));
		g2.drawLine(
				p1.x, p1.y, 
				p1.x, p1.y-12);
		
		p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(realTraceTo), smpPin));
		g2.drawLine(
				p1.x, p1.y, 
				p1.x, p1.y-12);
	}
	
	public int getLeft(int smp, double shift) {
		
		double x = getXDist(smp, shift);
		
		if (x > (y + shift ) * HYP_MAX) {
			return -1;
		}
		
		return file.getLeftDistTraceIndex(tracePin, x);
		
	}
	
	public int getRight(int smp, double shift) {
		
		double x = getXDist(smp, shift);
		
		if (x > (y + shift ) * HYP_MAX) {
			return -1;
		}
		
		return file.getRightDistTraceIndex(tracePin, x);
		
	}

	private double getXDist(int smp, double shift) {
		double c = RulerTool.distanceCm(file, tracePin, tracePin, 0, smp);
		
		double cs = c + shift;
		double ys = y + shift;
		
		double x = Math.sqrt(cs * cs - ys * ys);
		
		
		
		return x;
	}

	public int getMaxWidth() {
		return traceTo - traceFrom + 1;
	}


}
