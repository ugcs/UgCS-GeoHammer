package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;

public class PrismDrawer {

	private Model model;
	private Tanh tanh = new Tanh();	
	
	int goodcolor1 = (250 << 16) + (250 << 8 ) + 32;
	int goodcolor2 = (70 << 16) + (235 << 8 ) + 197;
	
	int colorRed3 = (120 << 16) + (0 << 8 ) + 0;
	int colorBlue4 = (255 << 16) + (130 << 8 ) + 130;
	
	int geencolorp = (0 << 16) + (100 << 8 ) + 94;
	int geencolorm = (94 << 16) + (100 << 8 ) + 0;	
	int geencolorb = (130<<16) + (255 << 8 ) + 130;
	
//	private static int goodColors[] = new int[100];
//	static {
//		for(int i=0; i< goodColors.length; i++) {
//			goodColors[i] = (((i%5)*30+50)<<16) + (((7-i%7)*20+100) <<8 ) + 177;
//		}
//	}
	
	public PrismDrawer(Model model) {
		this.model = model;
	}

	int goodColors[] = {			
			0,
			geencolorp,
			geencolorm,
			geencolorb
			//,
			//color_blu4
	};
	
	int edgeColors[] = {
			
			0,
			goodcolor1,
			goodcolor2,			
			colorBlue4,
			colorRed3
	};
	
	public void draw(//int width, int height, 
			int bytesInRow, 
			ProfileField field,
			Graphics2D g2,
			int[] buffer,			
			double threshold) {
		
		if(model.isLoading() || !model.getFileManager().isActive()) {
			return;
		}
		
		Rectangle rect = field.getMainRect();
		
		//boolean showInlineHyperbolas = model.getSettings().radarMapMode == RadarMapMode.SEARCH;
		
		boolean showInlineHyperbolas = model.getSettings().showGood.booleanValue();
		boolean showEdge = model.getSettings().showEdge.booleanValue();
		
		List<Trace> traces = model.getFileManager().getTraces();
		
		tanh.setThreshold((float)threshold);
		
		int startTrace = field.getFirstVisibleTrace();
		int finishTrace = field.getLastVisibleTrace();
		int lastSample = field.getLastVisibleSample(rect.height);
		
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
					
					if(showEdge && trace.edge != null && trace.edge[j] > 0) {
						color = edgeColors[trace.edge[j]] ;
					}

					if(showInlineHyperbolas && trace.good != null && trace.good[j] > 0) {
						color = goodColors[trace.good[j]] ;
					}
					
		    		for(int xt=0; xt < hscale; xt ++) {
		    			for(int yt =0; yt < vscale; yt++) {
		    				buffer[ rect.x +rect.width / 2 + xt + traceStartX + (sampStart + yt) * bytesInRow ] = color;
		    			}
		    			
		    			//hyperbola
//		    			if(showInlineHyperbolas && trace.good != null && trace.good[j] != 0) {
//		    				
//		    				int colorval = good_colors[trace.good[j]+1] ;// > 0 ? goodcolor1 : goodcolor2;
//		    				
//		    				buffer[ rect.x +rect.width / 2 + xt + traceStartX + (sampStart + 0) * bytesInRow ] = colorval;
//		    			}
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
