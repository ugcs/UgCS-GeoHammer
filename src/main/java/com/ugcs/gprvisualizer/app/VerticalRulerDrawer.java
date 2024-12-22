package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.apache.commons.lang3.tuple.Pair;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.gpr.LeftRulerController.Converter;
import com.ugcs.gprvisualizer.gpr.Model;

public class VerticalRulerDrawer {
	
	private final Model model;
	
	VerticalRulerDrawer(Model model) {
		this.model = model;
	}
	
	public void draw(Graphics2D g2, ProfileField field) {

		Rectangle rect = field.getLeftRuleRect(); 
		int firstSample;		
		int lastSample; 
		
		firstSample = field.getStartSample();		
		lastSample = 
				Math.min(
					field.getLastVisibleSample(rect.height),
					field.getMaxHeightInSamples());
		
		Converter converter = getConverter();
		Pair<Integer, Integer> p = converter.convert(firstSample, lastSample);
		int first = p.getLeft();
		int last = p.getRight();
		
		int[] tick = {100, 50, 10, 5};
		
		int sz = 21;
		
		for (int b : tick) {
			
			int s = (first / b + 1) * b;
			int f = last / b * b;
			boolean drawText = (f - s) / b < 8;
			
			for (int i = s; i <= f; i += b) {
				
				int y = field.sampleToScreen(converter.back(i));
				
				g2.setColor(Color.lightGray);
                g2.drawLine(rect.x , y, rect.x + sz, y);

                if (drawText) {
					g2.setColor(Color.darkGray);
					g2.setFont(new Font("Arial", Font.PLAIN, 12));
					g2.drawString(String.format("%1$3s", i), 
							rect.x + rect.width / 3, y + 4);
				}
				
			}
			sz = sz * 2 / 3;
		}		
	}

	private Converter getConverter() {
		return model.getLeftRulerController().getConverter();
	}
}
