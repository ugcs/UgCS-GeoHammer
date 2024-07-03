package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Point;
import java.awt.Shape;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;

public class DepthHeight extends DepthStart {
	
	public DepthHeight(Shape shape) {
		super(shape);
	}

	@Override
	public void controlToSettings(TraceSample ts) {
		int max = model.getMaxHeightInSamples();
		
		model.getSettings().hpage = 
			Math.min(max - model.getSettings().getLayer(), Math.max(
				0, ts.getSample() - model.getSettings().getLayer()));
	}
	
	@Override
	public Point getCenter(ProfileField profField) {
		Point scr = profField.traceSampleToScreen(new TraceSample(
				0, model.getSettings().getLayer() + model.getSettings().hpage));
		scr.x = profField.visibleStart;
		return scr;
	}	
}
