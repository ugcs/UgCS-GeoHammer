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
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class HyperFinder {
	
	private static final int R = 160;
	
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
	
	Color plusGood = new Color(100, 255, 100); 
	Color plusBad = new Color(0, 128, 0);
	
	Color minusGood = new Color(100, 100, 255); 
	Color minusBad = new Color(0, 0, 128);
	
	public HyperFinder() {
		
	}
	
	public void process(Model model) {
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
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
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
			traces.get(i).maxindex2 = Math.max(traces.get(i).maxindex2, cleversum(good[i])*4);
			
			if(traces.get(i).good == null) {
				traces.get(i).good = new int[good[i].length];
			}
			for(int z=0;z<good[i].length; z++) {
				traces.get(i).good[z] += good[i][z];
			}
			
		}
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
					if(grpsize>2) {
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

	private double processTrace(List<Trace> traces, int i, int[][] good, double hyperkf) {
		
		int goodSmpCnt = 0;
		for(int smp = AppContext.model.getSettings().layer; 
			smp< (AppContext.model.getSettings().layer + AppContext.model.getSettings().hpage) ; smp++) {
			
			double hyperRate = processHyper(traces, i, smp, hyperkf);
			
			if(hyperRate > AppContext.model.getSettings().hypergoodsize) {
				good[i][smp] = 1;
			}			
		}		
		
		return goodSmpCnt;
	}
	
	private double processHyper(List<Trace> traces, int tr, int smp, double hyperkf) {
		double y = smp;
		
		float example = traces.get(tr).getNormValues()[smp];
		
		int lft = getHyperSideLength(traces, tr, y, example, -1, hyperkf);
		int rht = getHyperSideLength(traces, tr, y, example, +1, hyperkf);
		
		return lft+rht;
		
	}

	private int getHyperSideLength(List<Trace> traces, int tr, double y, float example, int side, double hyperkf) {
		
		double kf = hyperkf;
		
		int i=1;
		int bad = 0;
		while(i<R && bad < 3 ) {
			int index = tr + side * i;
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
				bad++;
			}
			
			i++;
		}
		return i;
	}

	private boolean similar(float example, float val) {
		
		return (example > 0) == (val > 0);
	}

	
	
	public void setPoint(TraceSample ts) {
		this.ts = ts;
	}
	
	int lft;
	int rht;
	public void drawHyperbolaLine(Graphics2D g2, VerticalCutField vField) {
		
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

		for(int sm = ts.getSample(); sm < Math.min(ts.getSample() + 25, values.length); sm++) {
			float example = values[sm];
			
			double kf = AppContext.model.getSettings().hyperkfc / 100.0;
			
			double y = sm;
			lft = getHyperSideLength(traces, tr, y, example, -1, kf);
			rht = getHyperSideLength(traces, tr, y, example, +1, kf);
			
			drawHyperbolaLine(g2, vField, sm, example>0);
		}
		
	}
	
	public void drawHyperbolaLine(Graphics2D g2, VerticalCutField vField, int smp, boolean positive) {
		if(ts == null) {
			return;
		}
		
		if(lft+rht > AppContext.model.getSettings().hypergoodsize) {
			g2.setStroke(line);
			
		}else {
			g2.setStroke(dashed);			
		}
		g2.setColor(positive ? Color.GREEN : Color.CYAN);
		
		
		Point prev = null;
		
		double kf = AppContext.model.getSettings().hyperkfc/100.0;
		
		int tr = ts.getTrace();
		int s = ts.getTrace()-lft;
		int f = ts.getTrace()+rht;
		
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
