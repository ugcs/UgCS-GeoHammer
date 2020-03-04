package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.apache.commons.lang3.tuple.Pair;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.gpr.LeftRulerController.Converter;
import com.ugcs.gprvisualizer.gpr.Model;

public class VerticalRulerDrawer {
	
	private static Color txtColor = new Color(230, 221, 150);
	
	Model model;
	VerticalRulerDrawer(Model model){
		this.model = model;
	}
	
	public void draw(Graphics2D g2, ProfileField field) {
		
		Converter converter = getConverter(); 
		
		Rectangle ir = field.getInfoRect();
		g2.setColor(new Color(10, 10, 20));
		g2.fillRect(ir.x, ir.y, ir.width, ir.height);
		
		
		
		Rectangle rect = field.getLeftRuleRect(); 
		int firstSample;		
		int lastSample; 
		
		firstSample = field.getStartSample();		
		lastSample = 
				Math.min(
					field.getLastVisibleSample(rect.height),
					model.getMaxHeightInSamples());	
		Pair<Integer, Integer> p = converter.convert(firstSample, lastSample);
		int first = p.getLeft();
		int last = p.getRight();
		
		int tick[] = {100, 50, 10, 5};
		
		int sz = 21;
		
		g2.setColor(Color.WHITE);
		for(int b : tick) {
			
			int s = ((first)/b +1 )*b;
			int f = ((last))/b*b;
			boolean drawText = (f-s)/b  < 8;
			
			for(int i=s; i<=f; i+=b) {
				
				int y = field.sampleToScreen(converter.back(i));
				
				g2.drawLine(rect.x + rect.width - sz, y, rect.x + rect.width, y);
				
				if(drawText) {
					g2.drawString(String.format("%1$3s", i), rect.x + rect.width - 30, y-2);
				}
				
			}
			sz=sz*2/3;
		}
		
	}


	private Converter getConverter() {
		
		return model.getLeftRulerController().getConverter();
		//return new SamplConverter();
	}
	
	
}
