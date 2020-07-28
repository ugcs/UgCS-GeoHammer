package com.ugcs.gprvisualizer.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.gpr.Model;

public class HyperFinder {
	
	private TraceSample ts;	
	private static final float[] dash1 = {1.0f, 7.0f};
	private static final BasicStroke dashed =
	        new BasicStroke(1.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        10.0f, dash1, 0.0f);

	private static final BasicStroke line1 =
	        new BasicStroke(1.0f);
	private static final BasicStroke line2 =
	        new BasicStroke(2.0f);
	private static final BasicStroke line4 =
	        new BasicStroke(4.0f);
	private static final BasicStroke line8 =
	        new BasicStroke(8.0f);
	
	Color plusBest = new Color(100, 255, 100);
	Color plusGood = new Color(70, 180, 70); 
	Color plusBad = new Color(0, 111, 0);
	
	Color minusBest = new Color(100, 100, 255);
	Color minusGood = new Color(70, 70, 180); 
	Color minusBad = new Color(0, 0, 111);
	Model model;
	HoughScan hs;
	
	public HyperFinder(Model model) {
		this.model = model;
		
		hs = new HoughScan(model);
	}
	
	public void setPoint(TraceSample ts) {
		this.ts = ts;
	}
	
	public void drawHyperbolaLine(Graphics2D g2, ProfileField profField, 
			int smp, int lft, int rht, int voffst) {
		if (ts == null) {
			return;
		}
		
		
		Point prev = null;
		
		double kf = model.getSettings().hyperkfc / 100.0;
		
		int tr = ts.getTrace();
		int s = lft;
		int f = rht;
		
		double y = smp;
		
		for (int i = s; i <= f; i++) {
			
			double x = (i - tr) * kf;
			double c = Math.sqrt(x * x + y * y);
			
			Point lt = profField.traceSampleToScreen(new TraceSample(i, (int) c));
			if (prev != null) {
				g2.drawLine(prev.x, prev.y + voffst, lt.x, lt.y + voffst);
			}
			
			prev = lt;
		}
	}
	
	public void drawHyperbolaLine(Graphics2D g2, ProfileField profField) {
		
		if (ts == null) {
			return;
		}
		
		int tr = ts.getTrace();
		List<Trace> traces = model.getFileManager().getTraces();
		
		if (tr < 0 || tr >= traces.size() || ts.getSample() < 0) {
			return;
		}
		
		Trace extr = traces.get(tr);
		
		float [] values = extr.getNormValues();
		if (ts.getSample() < 0 || ts.getSample() >= values.length) {
			return;
		}
		
		////
		SgyFile file = model.getSgyFileByTrace(tr);
		
		hs.isPrintLog = true;
		
//		double threshold = model.getSettings().hyperSensitivity.doubleValue();
//		hs.scan(file, tr - file.getOffset().getStartTrace(), ts.getSample(), threshold);
//		
//		if (hs.getHoughDrawer() != null) {
//			hs.getHoughDrawer().drawOnCut(g2, profField);
//		}
		
		double hyperkf = model.getSettings().hyperkfc / 100.0;		
		float example2 = values[ts.getSample()];
		HalfHyper left2 = HalfHyper.getHalfHyper(
				traces, tr, ts.getSample(), example2, -1, hyperkf);		
		HalfHyper right2 = HalfHyper.getHalfHyper(
				traces, tr, ts.getSample(), example2, +1, hyperkf);		
		
		Point lt = profField.traceSampleToScreen(ts);

		///draw
		
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(lt.x - 100, lt.y - 60, 200, 40);
		g2.setColor(Color.RED);

		g2.drawString("" + ts.getTrace() + " (" + extr.indexInFile + ") " 
				+ ts.getSample() + " (" + fl(example2) 
				+ ")   ofst: " + extr.verticalOffset,
				lt.x - 100, lt.y - 40);
		
		g2.drawString(" l: " + fl(left2.oppositeAbovePerc) + " " 
				+ fl(left2.oppositeBelowPerc) + " <-|-> " 
				+ " r: " +  fl(right2.oppositeAbovePerc)
				+ " " + fl(right2.oppositeBelowPerc),
				lt.x - 100, lt.y - 30);
		
		g2.setColor(new Color(60, 140, 150, 170));
		g2.setStroke(line8);
		
		
		HoughExperiments.HYP_MAX = (double) model.getSettings().hyperkfc / 100.0;
		HoughExperimentsAnalizer hea = new HoughExperimentsAnalizer(file);
		
		HoughExperiments he = hea.debug(
				tr - file.getOffset().getStartTrace(), 
				ts.getSample(), 
				model.getSettings().printHoughVertShift.doubleValue());
				
//		HoughExperiments he = HoughExperiments.f(file, tr - file.getOffset().getStartTrace(), ts.getSample(), 
//				model.getSettings().printHoughVertShift.doubleValue(), 4);
		
		he.draw(g2, profField);
//		Sout.p("good c " + he.criteriaGoodCount());
//		Sout.p("real w " + he.criteriaRealWidth());
//		Sout.p("gb rat " + he.criteriaGoodBadRatio());
		
		//drawHalfHyperLine(g2, profField, left2, 1);
		//drawHalfHyperLine(g2, profField, right2, 1);
	}
	
	protected void drawHalfHyperLine(Graphics2D g2, ProfileField profField, 
			HalfHyper hh, int voffst) {
		
		
		boolean positive = hh.example > 0;
		double hyperkf = model.getSettings().hyperkfc / 100.0;
		int goodside = (int) (HalfHyper.getGoodSideSize(hh.pinSmp) / hyperkf);
		if (hh.length >= goodside) {
			
			if (hh.isGood()) {
				g2.setStroke(line4);
				g2.setColor(positive ? plusBest : minusBest);
			} else {
				g2.setStroke(line2);
				g2.setColor(positive ? plusGood : minusGood);
			}
			
		} else {
			g2.setStroke(dashed);			
			g2.setColor(positive ? plusBad : minusBad);
		}		

		
		g2.setStroke(line4);
		g2.setColor(plusBest);
		
		Point prev = null;
		for (int i = 0; i < hh.smp.length; i++) {
			Point lt = profField.traceSampleToScreen(new TraceSample(
					hh.pinTr + i * hh.side, hh.smp[i]));
			
			if (prev != null) {
				g2.drawLine(prev.x, prev.y + voffst, lt.x, lt.y + voffst);
			}
			
			prev = lt;
		}
		
	}
	
	
	public double getThreshold() {
		double thr = (double) model.getSettings().hyperSensitivity.intValue() / 100.0;
		return thr;
	}
		
	public static double THRESHOLD = 0.7;
	
	protected void drawHyperbolaLine2(Graphics2D g2, ProfileField profField) {
		
		double thr = getThreshold();
		
		int tr = ts.getTrace();
		int smp = ts.getSample();
		
		SgyFile sgyFile = model.getSgyFileByTrace(tr);
		int traceInFile = tr - sgyFile.getOffset().getStartTrace();
		
		//List<Trace> traces = AppContext.model.getFileManager().getTraces();
		//double x_factor = AppContext.model.getSettings().hyperkfc/100.0;
		
		for (double factorX = AlgorithmicScan.X_FACTOR_FROM; 
				factorX <= AlgorithmicScan.X_FACTOR_TO; 
				factorX += AlgorithmicScan.X_FACTOR_STEP) {
			drawHyperSingleLine(g2, profField, thr, smp, sgyFile, traceInFile, factorX);
		}
		
		
	}

	protected void drawHyperSingleLine(Graphics2D g2, 
			ProfileField profField, double thr, 
			int smp, SgyFile sgyFile,
			int traceInFile, double factorX) {
		
		//left
		HalfHyperDst lft = HalfHyperDst.getHalfHyper(sgyFile, 
				traceInFile, smp, -1, factorX);
		double lftRate = lft.analize(100);
		
		boolean lftGood = lftRate > thr;		
		g2.setColor(lftGood ? Color.RED : Color.CYAN);
		g2.setStroke(lftGood ? line2 : line1);
		
		drawHypDst(g2, profField, sgyFile.getOffset(), lft);

		
		//right
		HalfHyperDst rht = HalfHyperDst.getHalfHyper(sgyFile, 
				traceInFile, smp, +1, factorX);
		double rhtRate = rht.analize(100);
		
		boolean rhtGood = rhtRate > thr;		
		g2.setColor(rhtGood ? Color.RED : Color.CYAN);
		g2.setStroke(rhtGood ? line2 : line1);
		drawHypDst(g2, profField, sgyFile.getOffset(), rht);
	}

	protected void drawHypDst(Graphics2D g2, ProfileField profField, 
			VerticalCutPart  offset, HalfHyperDst lft) {
		Point prev = null;
		for (int i = 0; i < lft.length; i++) {
			
			int traceIndex = offset.localToGlobal(lft.pinnacleTrace + i * lft.side);
			
			Point lt = profField.traceSampleToScreenCenter(new TraceSample(
					traceIndex, lft.smp[i]));
			if (prev != null) {
				g2.drawLine(prev.x, prev.y, lt.x, lt.y);
			}
			
			prev = lt;
		}
	}	
	
	String fl(double d) {
		return String.format(" %.2f ", d);
	}
	
}
