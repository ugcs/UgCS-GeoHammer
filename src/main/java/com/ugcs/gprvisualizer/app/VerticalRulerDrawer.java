package com.ugcs.gprvisualizer.app;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.gpr.Model;

public class VerticalRulerDrawer {

	
	public void draw(Graphics2D g2, ProfileField field, Model model) {
		
		Rectangle rect = field.getLeftRuleRect(); 
		
		int firstSample = field.getStartSample();		
		int lastSample = 
				Math.min(
					field.getLastVisibleSample(rect.height),
					model.getMaxHeightInSamples());	
		
		int tick[] = {100, 50, 10, 5};
		
		int sz = 21;
		for(int b : tick) {
			
			int s = ((firstSample+1)/b)*b;
			int f = ((lastSample))/b*b;
			boolean drawText = (f-s)/b  < 8;
			
			for(int i=s; i<=f; i+=b) {
				
				int y = field.sampleToScreen(i);
				
				g2.drawLine(rect.x + rect.width - sz, y, rect.x + rect.width, y);
				
				if(drawText) {
					g2.drawString(String.format("%1$3s", i), rect.x + rect.width - 30, y-2);
				}
				
			}
			sz=sz*2/3;
		}
		
	}
	
	
}
