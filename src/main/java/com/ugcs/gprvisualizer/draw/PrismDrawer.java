package com.ugcs.gprvisualizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PaletteBuilder;
import com.ugcs.gprvisualizer.gpr.Settings.RadarMapMode;

public class PrismDrawer implements VCutDrawer {

	private Model model;
	private int vOffset;
	Tanh tanh = new Tanh();
	
	
	int goodcolor1 = (255<<16) + (70 << 8 ) + 177;
	int goodcolor2 = (70<<16) + (255 << 8 ) + 177;
	int geencolor = (32<<16) + (255 << 8 ) + 32;
	
	private static int goodcolors[] = new int[100];
	static {
		for(int i=0; i< goodcolors.length; i++) {
			goodcolors[i] = (((i%5)*30+50)<<16) + (((7-i%7)*20+100) <<8 ) + 177;
		}
	}
	
	public PrismDrawer(Model model, int vOffset) {
		this.model = model;
		this.vOffset = vOffset;
	}
	
	public void draw(int width, int height, 
			ProfileField field,
			Graphics2D g2,
			int[] buffer,			
			double threshold) {
		
		if(model.isLoading() || !model.getFileManager().isActive()) {
			return;
		}
		
		boolean showInlineHyperbolas = model.getSettings().radarMapMode == RadarMapMode.SEARCH;
		
		List<Trace> traces = model.getFileManager().getTraces();
		
		tanh.setThreshold((float)threshold);
		
		int startTrace = field.getFirstVisibleTrace(width);
		int finishTrace = field.getLastVisibleTrace(width);		
		
		int lastSample = field.getLastVisibleSample(height-vOffset);
		
		for(int i=startTrace; i<finishTrace; i++ ) {

				int traceStartX = field.traceToScreen(i);
				int traceFinishX = field.traceToScreen(i+1);
				int hscale = traceFinishX - traceStartX;
				if(hscale < 1) {
					continue;
				}
				
				Trace trace = traces.get(i);
				float middleAmp = model.getSettings().hypermiddleamp;
				float[] values = trace.getNormValues();
				for(int j = field.getStartSample(); j<Math.min(lastSample, values.length ); j++) {
					
					int sampStart = field.sampleToScreen(j);
					int sampFinish = field.sampleToScreen(j+1);
					
					int vscale = sampFinish - sampStart;
					if(vscale == 0) {
						continue;
					}
					
					int color = tanh.trans(values[j] - middleAmp);
					
		    		for(int xt=0; xt < hscale; xt ++) {
		    			for(int yt =0; yt < vscale; yt++) {
		    				buffer[width / 2 + xt + traceStartX + (vOffset + sampStart + yt) * width ] = color;
		    			}
		    			
		    			//hyperbola
		    			if(showInlineHyperbolas && trace.good != null && trace.good[j] != 0) {
		    				buffer[width / 2 + xt + traceStartX + (vOffset + sampStart + 0) * width ] = 
		    					trace.good[j] > 0 ? goodcolor1 : goodcolor2;
		    			}
		    			//
		    		}
				}
				
				//Point p1 = field.traceSampleToScreenCenter(new TraceSample(i-1,  trace.maxindex2));
//				int sampStart = field.sampleToScreen(trace.maxindex2);
//				if(sampStart>0 && sampStart <height) {
//					buffer[width / 2 + 0 + traceStartX + (vOffset + sampStart + 0) * width ] = geencolor;
//				}    			
				
			
		}
	}
	
	
}
