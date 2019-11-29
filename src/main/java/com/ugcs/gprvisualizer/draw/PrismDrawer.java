package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.gpr.Model;

public class PrismDrawer implements VCutDrawer {

	private Model model;
	private int vOffset;
	
	public PrismDrawer(Model model, int vOffset) {
		this.model = model;
		this.vOffset = vOffset;
	}
	
	public void draw(int width, int height, 
			VerticalCutField field,
			Graphics2D g2,
			int[] buffer,			
			double threshold) {
		
		int startTrace = field.getFirstVisibleTrace(width);
		int finishTrace = field.getLastVisibleTrace(width);		
		
		int lastSample = field.getLastVisibleSample(height-vOffset);
		int vscale = Math.max(1, (int)field.getVScale());
		int hscale = Math.max(1, (int)field.getHScale());
		
		for(int i=startTrace; i<finishTrace; i++ ) {
			
				Trace trace = model.getFileManager().getTraces().get(i);
				float[] values = trace.getNormValues();
				for(int j = field.getStartSample(); j<Math.min(lastSample, values.length ); j++) {
					
					Point p = field.traceSampleToScreen(new TraceSample(i, j));
					
		    		int c = (int) (127.0 - Math.tanh(values[j]/threshold) * 127.0);
		    		int color = ((c) << 16) + ((c) << 8) + c;
		    		
		    		//buffer[width/2 + p.x + vscale * p.y  * width ] = color;
		    		for(int xt=0; xt < hscale; xt ++) {
		    			for(int yt =0; yt < vscale; yt++) {
		    				buffer[width / 2 + xt + p.x + (vOffset + p.y + yt) * width ] = color;
		    			}
		    		}
				}
			
		}
	}
	
	
}
