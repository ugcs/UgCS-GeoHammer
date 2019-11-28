package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;

import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;

public interface VCutDrawer {
	public void draw(int width, int height, 
			VerticalCutField field,
			Graphics2D g2,
			int[] buffer,			
			double threshold);
}
