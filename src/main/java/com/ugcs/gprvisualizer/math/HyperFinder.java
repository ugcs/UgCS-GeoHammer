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
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.AppContext;
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

	final static BasicStroke line =
	        new BasicStroke(2.0f);
	final static BasicStroke line2 =
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
	
	public void process() {
		//clear
		for(Trace t: model.getFileManager().getTraces()) {
			t.maxindex2 = 0;
			t.good = null;			
		}
		
		//for(int kf = 13; kf<36; kf+=2) {
			
		int kf = model.getSettings().hyperkfc;
			for(SgyFile sf : model.getFileManager().getFiles()) {
				System.out.println("file: " + sf.getFile().getName());
				processSgyFile(sf, kf/100.0);			
			}
		//}
		
		System.out.println("finish");
		
		AppContext.notifyAll(new WhatChanged(Change.adjusting));
	}

	private void processSgyFile(SgyFile sf, double hyperkf) {
		List<Trace> traces = sf.getTraces();
		
		int height = traces.get(0).getNormValues().length;
		int good[][] = new int[traces.size()][height];
		
		for(int i=0; i<traces.size(); i++) {
			processTrace(traces, i, good, hyperkf); 
		}
		
		//filter
		
		//filterGood(height, good);
		
		//
		saveResultToTraces(traces, good);
	}

	private void saveResultToTraces(List<Trace> traces, int[][] good) {
		for(int i=0; i<traces.size(); i++) {
			traces.get(i).maxindex2 = Math.max(traces.get(i).maxindex2, cleversum(good, i));
			
			
			//put to trace.good
			if(traces.get(i).good == null) {
				traces.get(i).good = new int[good[i].length];
			}
			for(int z=0;z<good[i].length; z++) {
				traces.get(i).good[z] = good[i][z];
			}
			
		}
	}

	
	
	private int cleversum(int[][] good, int tr) {
		int margin = 0;
		double sum = 0;
		int mult = 0;
		int prevsign = 0;
		int onesize=1;
		for(int i=0; i<good[tr].length; i++) {
			
			int val = getAtLeastOneGood(good, tr, margin, i);
			
			if(val != 0) {				
				
				if(val != prevsign) {
					//reverse amplitude
					mult++;
					prevsign = val;
					onesize=1;
				}else {
					onesize++;
				}
				
				sum += 1.0 / (double)onesize;
			}			
			
			if(sum>0) {
				margin+=3;
			}
		}
		
		return (int)(sum*mult);
	}
	
	private int getAtLeastOneGood(int[][] good, int tr, int margin, int smp) {
		
		for(int chtr = Math.max(0, tr-margin); chtr < Math.min(good.length, tr+margin+1); chtr++) {
			if(good[chtr][smp] != 0) {
				return good[chtr][smp];
			}
		}
		
		return 0;
	}

	private int cleversum(int[] is) {
		int sum = 0;
		int grpsize=0;
		boolean activegrp = false;
		for(int i=0; i<is.length; i++) {
			
			if(is[i] > 0) {
				if(!activegrp) {
					activegrp = true;
					grpsize=0;
				}
				grpsize+=is[i];
			}else{
				if(activegrp) {
					activegrp=false;
					if(grpsize>0) {
						sum += grpsize;
						grpsize=0;
					}
				}
			}
			
			//sum += is[i];
		}
		
		return sum;
	}

	private void filterGood(int height, int[][] good) {
		for(int smp=0; smp<height; smp++) { //row
			//fill gaps in 1 trace to ignore them
			for(int tr =1; tr<good.length-1; tr++) {
				if(good[tr+1][smp]>0) {
					good[tr-1][smp] = 1;
				}
			}
			
			int grpstart = -1;
			for(int tr =0; tr<good.length; tr++) {
				if(good[tr][smp]>0) {
					if(grpstart == -1) {
						//start group
						grpstart = tr;
					}					
				}else{
					//finish group
					if(grpstart != -1) {
						if(tr-grpstart > 99) {
							//clear row
							for(int tri=grpstart; tri<tr; tri++) {
								good[tri][smp] = 0;
							}							
						}
						
						grpstart = -1;
					}					
				}
			}			
		}
	}

	private double processTrace(List<Trace> traces, int tr, int[][] good, double hyperkf) {
		int goodSmpCnt = 0;
		int maxSmp =
				Math.min(
						AppContext.model.getSettings().layer + AppContext.model.getSettings().hpage,
						traces.get(tr).getNormValues().length-1
				);
		for(int smp = AppContext.model.getSettings().layer;				
			smp< maxSmp ; smp++) {
			
			processHyper(traces, tr, smp, hyperkf, good);
			//	good[tr][smp] = 1;
			//}
		}
		
		return goodSmpCnt;
	}
	
	private boolean processHyper(List<Trace> traces, int tr, int smp, double hyperkf, int[][] good) {
		double y = smp;
		
		float example = traces.get(tr).getNormValues()[smp];
		
		TraceSample lft = getHyperSideLength(traces, tr, y, example, -1, hyperkf);
		TraceSample rht = getHyperSideLength(traces, tr, y, example, +1, hyperkf);
		
		//find opposite value inside hyperbola
		
		int hyperRate = rht.getTrace() - lft.getTrace(); 
		if(hyperRate > AppContext.model.getSettings().hypergoodsize) {		
			
			boolean b1 = findOppositeBelowHyperSide(traces, tr, smp, example, -1, hyperkf, lft.getSample());
			boolean b2 = findOppositeBelowHyperSide(traces, tr, smp, example, +1, hyperkf, rht.getSample());
		
			if(b1 || b2) {
				
				good[tr][smp] = example > 0 ? 1 : -1;				

			}
		}
		return false;
	}

	private boolean findOppositeBelowHyperSide(List<Trace> traces, int tr, int smp, float example, int side, double hyperkf, int sizesmp) {
		boolean positive = example > 0;
		
		float threshold = example/6.7f;
		
		double y = smp;
		int sum = 0;		
		for(int smpcheck=smp; smpcheck < sizesmp; smpcheck++) {
			
			double c = smpcheck;
			double x = Math.sqrt(c*c - y*y);
			
			for(int i=0; i<x; i++) {
				int ind = tr+side*i;
				if(ind <0 || ind >= traces.size()) {
					continue;
				}
				float []values = traces.get(ind).getNormValues();
				
				if(smpcheck < values.length) {
					float f = values[smpcheck];
					if((f>0) != positive || Math.abs(f) < Math.abs(threshold)) {
						sum++;
					}
				}
			}			
		}
		
		return sum > 30;
	}
	
	private TraceSample getHyperSideLength(List<Trace> traces, int tr, double y, float example, int side, double hyperkf) {
		
		double kf = hyperkf;
		
		int i=1;
		double bad = 0;
		int index = 0;
		int hypergoodsize = model.getSettings().hypergoodsize;
		while(i<hypergoodsize && bad < 0.2 ) {
			index = tr + side * i;
			if(index<0 || index>= traces.size() ) {
				break;
			}

			double x = i * kf;
			double c = Math.sqrt(x*x+y*y);
			
			float values[] = traces.get(index).getNormValues();
			
			int smp = (int)c;
			if(smp >= values.length) {
				break;
			}
			float val = values[smp];
			
			if(!similar(example, val)) {
				bad += Math.abs(val/example);
			}
			
			i++;
		}
		
		double x = (index-tr) * kf;
		double c = Math.sqrt(x*x+y*y);		
		return new TraceSample(index, (int)c);
	}

	private boolean similar(float example, float val) {
		
		return (example > 0) == (val > 0);
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
		
		float [] values = traces.get(tr).getNormValues();
		if(ts.getSample() < 0 || ts.getSample() >= values.length) {
			return;
		}
		
		
		Point lt = vField.traceSampleToScreen(ts);
		g2.setColor(Color.RED);
		g2.drawString(ts.getTrace() + " (" + traces.get(tr).indexInFile + ") " + ts.getSample() + " (" + traces.get(tr).getNormValues()[ts.getSample()] + ")" , lt.x-100, lt.y - 20);

		for(int smp = ts.getSample(); smp < Math.min(ts.getSample() + 30, values.length); smp++) {
			float example = values[smp];
			
			double kf = AppContext.model.getSettings().hyperkfc / 100.0;
			
			double y = smp;
			TraceSample lts = getHyperSideLength(traces, tr, y, example, -1, kf);
			TraceSample rts = getHyperSideLength(traces, tr, y, example, +1, kf);
			
			
			boolean b1 = findOppositeBelowHyperSide(traces, tr, smp, example, -1, kf, lts.getSample());
			boolean b2 = findOppositeBelowHyperSide(traces, tr, smp, example, +1, kf, rts.getSample());
			
			int lft = lts.getTrace();
			int rht = rts.getTrace();
			
			drawHyperbolaLine(g2, vField, smp, example>0, lft, rht, b1||b2);
		}
		
	}
	
	public void drawHyperbolaLine(Graphics2D g2, ProfileField vField, int smp, boolean positive, int lft, int rht, boolean containsOpposite) {
		if(ts == null) {
			return;
		}
		
		
		if(rht-lft > AppContext.model.getSettings().hypergoodsize) {
			
			if(containsOpposite) {
				g2.setStroke(line2);
				g2.setColor(positive ? plusBest : minusBad);
			}else {
				g2.setStroke(line);
				g2.setColor(positive ? plusGood : minusGood);
			}
			
		}else {
			g2.setStroke(dashed);			
			g2.setColor(positive ? plusBad : minusBad);
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
			
			Point lt = vField.traceSampleToScreen(new TraceSample(i, (int)c));
			if(prev != null) {
				g2.drawLine(prev.x, prev.y, lt.x, lt.y);				
			}
			
			prev = lt;
		}
	}
	
	
}
