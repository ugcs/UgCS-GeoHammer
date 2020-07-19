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

	//private static final int BAD_MARGIN = 4;
	private int badMargin;

	private static final int BAD_INCR = 3;

	private static final double HYP_MAX = 0.25;
	private double effectHypMax;

	public boolean print = false;
	private SgyFile file;
	public int traceFrom;
	public int traceTo;
	public int realTraceFrom;
	public int realTraceTo;

	private int tracePin;
	private int smpPin;

	int lookingEdge;

	// real dist to pin
	private double y;
	public double shift;

	int[] leftStart = new int[50];
	int[] leftFinish = new int[50];
	int[] rightStart = new int[50];
	int[] rightFinish = new int[50];
	public int lastSmp;
	int lftInd;
	int rghInd;

	RealSizeCalculator rsc;

	static public HoughExperiments f(SgyFile file, int tr, int smp, double heightShift, int lookingEdge,
			boolean print) {
		HoughExperiments he = new HoughExperiments();
		he.print = print;

		he.file = file;
		// he.edge = edge;

		he.smpPin = smp;
		he.tracePin = tr;
		he.y = RulerTool.distanceCm(file, he.tracePin, he.tracePin, 0, he.smpPin);
		he.shift = heightShift;
		he.lookingEdge = lookingEdge;

		he.effectHypMax = HYP_MAX - (heightShift / 150) * 0.075;

		he.init();

		return he;
	}
	
	
	

	public boolean criteriaHeader() {

		// TODO:
		return true;
	}

	public boolean criteriaGoodCount() {
		int compare = getMaxWidth() * 21 / 100;

		boolean res = good > compare;
		if (print) {
			Sout.p("criteriaGoodCount         " + good + "  <>  " + compare + " = " + res);
		}

		return res;
	}

	public boolean criteriaRealWidth() {

		int min = getMaxWidth() * 65 / 100;

		// add header
		for (int i = leftStart[0]; i <= rightFinish[0]; i++) {
			if (file.getEdge(i, smpPin) == lookingEdge || file.getEdge(i, smpPin - 1) == lookingEdge) {
				rsc.add(i, smpPin);
			}
		}
		lftInd = rsc.getLeft();
		rghInd = rsc.getRight();
		
		realTraceFrom = tracePin - lftInd;
		realTraceTo = tracePin + rghInd;

		int realWidth = realTraceTo - realTraceFrom + 1;

		boolean res = realWidth > min;
		if (print) {
			Sout.p("criteriaRealW " + maxgap + "(" + realTraceFrom + "-" + realTraceTo + ")" + "  rw " + realWidth
					+ "  <>  " + min + " = " + res);
		}

		return res;
	}

	public boolean criteriaRealMinLeft() {

		int min = (tracePin - traceFrom) / 2;

		boolean res = lftInd > min;
		if (print) {
			Sout.p("criteriaRealMinLeft  " + lftInd + "  <>  " + min + " = " + res);
		}

		return res;
	}


	public boolean criteriaRealMinRight() {
       
        int min = (traceTo - tracePin) / 2;
		boolean res = rghInd > min;
		if (print) {
			Sout.p("criteriaRealMinRight " + rghInd + "  <>  " + min + " = " + res);
		}

		return res;
	}	
	
	public boolean criteriaRealMinHight() {
		int lh = rsc.getLeftSmp(lftInd);
		int rh = rsc.getRightSmp(rghInd);
		
		int minHt = Math.min(lh, rh) - smpPin;
		
		
		int min = 1 + (lastSmp - smpPin) / 9;
		
		boolean res = minHt > min;
		if (print) {
			Sout.p("criteriaRealMinHeight " + minHt + "  <>  " + min + " = " + res);
		}

		return res;
	}

	public boolean criteriaGoodBadRatio() {
		good = 0;
		bad = 0;
		
		
		//top
		int tfrom = leftStart[0] - badMargin;
		int tto = rightFinish[0] + badMargin;

		checkRow(smpPin - 2, 
				Math.max(0, tfrom), 
				Math.min(tto , file.size() - 1));
		checkRow(smpPin - 1, 
				Math.max(0, tfrom), 
				Math.min(tto , file.size() - 1));
		checkRow(smpPin, 
				Math.max(0, tfrom), 
				Math.min(tto , file.size() - 1));
		
		//left
		for (int smp = smpPin + 1 ; smp <= rsc.getLeftSmp(lftInd); smp++) {

			int fromMin = Math.max(realTraceFrom - BAD_INCR, 0);
			int lfrom = leftStart[smp - smpPin] - badMargin;
			int lto = leftFinish[smp - smpPin] + badMargin;
			if (smp <= smpPin + 2) {
				lto = tracePin;
			}
			
			checkRow(smp, 
					Math.max(fromMin, lfrom), 
					Math.min(lto , file.size()-1));
		}
		
		
		//right
		for (int smp = smpPin ; smp <= rsc.getRightSmp(rghInd); smp++) {
			//
			int toMax = Math.min(realTraceTo + BAD_INCR, file.size() - 1);
			int to = rightStart[smp - smpPin] + badMargin;
			int rfrom = rightStart[smp - smpPin] - badMargin;
			if (smp <= smpPin + 2) {
				rfrom = tracePin;
			}
						
			checkRow(smp, 
					rfrom, 
					Math.min(toMax, to));

		}

		int compare = bad * 215 / 100;
		boolean res = good > compare;

		if (print) {
			Sout.p("criteria  gd/bad      " + good + "  <>  " + compare + " = " + res);
		}

		return res;
	}

	public boolean callAll() {
		return criteriaGoodCount() 
			&& criteriaRealWidth()
			&& criteriaRealMinLeft()
			&& criteriaRealMinRight()
			&& criteriaRealMinHight()
			&& criteriaGoodBadRatio();		
	}
	
	private void checkRow(int smp, int from, int to) {
		//Sout.p("checkRow " + smp + " (" + from + " - " + to + ")");
		for (int t = from; t <= to; t++) {
			if (file.getEdge(t, smp) == lookingEdge) {
				addPoint(t, smp);
			}
		}
	}

	// getRealSizeTraces()
	// getRealSizeRelative()

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

		badMargin = (leftFinish[1] - leftStart[1] + 1) * 130 / 100;
		if (print) {
			Sout.p("badMargin " + badMargin);
		}
		maxgap = getMaxWidth() / 15;

		rsc = new RealSizeCalculator(tracePin, traceFrom, traceTo, maxgap, tracePin, tracePin);// leftFinish[1],
																								// rightStart[1]);
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
		
		if (inGood(tr, smp)) {
			good++;

			rsc.add(tr, smp);
			
		} else if (inBad(tr, smp, badMargin)) {
			bad++;
		}

	}
	
	private boolean inBad(int tr, int smp, int range) {
		int index = smp - smpPin;

		if (index < -2) {
			return false;
		}
		
		if (index >= lastSmp) {
			return false;
		}

		int smpInd = Math.max(0, smp - smpPin); 
		if (smpInd <= 2) {
			return tr >= leftStart[smpInd] - range 
				&& tr <= rightFinish[smpInd] + range;
		}
		
		return tr >= leftStart[smpInd] - range 
				&& tr <= leftFinish[smpInd] + range
				|| tr >= rightStart[smpInd] - range 
				&& tr <= rightFinish[smpInd] + range;
	}
	

	private boolean inGood(int tr, int smp) {
		int index = smp - smpPin;

		if (index < -1 || index >= lastSmp) {
			return false;
		}

		int smpInd = Math.max(0, smp - smpPin); 
		
		return tr >= leftStart[smpInd] 
				&& tr <= leftFinish[smpInd]
				|| tr >= rightStart[smpInd]
				&& tr <= rightFinish[smpInd];
	}


	private static final int borderSize = 24;
	public void draw(Graphics2D g2, ProfileField field) {
		
		boolean good = callAll();
		
		g2.setColor(good ? new Color(60, 170, 100, 170) : new Color(60, 140, 150, 170));
		
		VerticalCutPart of = file.getOffset();

		for (int smp = smpPin; smp < lastSmp; smp++) {

			int index = smp - smpPin;
			int leftstart = leftStart[index];
			int leftfinish = leftFinish[index];
			int rightstart = rightStart[index];
			int rightfinish = rightFinish[index];

			// System.out.println(leftstart + " - " + leftfinish);

			Point p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(leftstart), smp));
			Point p2 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(leftfinish), smp));
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);

			p1 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(rightstart), smp));
			p2 = field.traceSampleToScreenCenter(new TraceSample(of.localToGlobal(rightfinish), smp));
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(2));
		// draw real tr
		Point p1 = field.traceSampleToScreen(new TraceSample(
				of.localToGlobal(realTraceFrom), rsc.getLeftSmp(lftInd) + 1));
		g2.drawLine(p1.x, p1.y, p1.x, p1.y - borderSize);
		g2.drawLine(p1.x, p1.y, p1.x + borderSize, p1.y);

		p1 = field.traceSampleToScreen(new TraceSample(
				of.localToGlobal(realTraceTo + 1), 
				rsc.getRightSmp(rghInd) + 1));
		g2.drawLine(p1.x, p1.y, p1.x, p1.y - borderSize);
		g2.drawLine(p1.x, p1.y, p1.x - borderSize, p1.y);
	}

	public int getLeft(int smp, double shift) {

		double x = getXDist(smp, shift);

		if (x > (y + shift) * effectHypMax) {
			return -1;
		}

		return file.getLeftDistTraceIndex(tracePin, x);

	}

	public int getRight(int smp, double shift) {

		double x = getXDist(smp, shift);

		if (x > (y + shift) * effectHypMax) {
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
