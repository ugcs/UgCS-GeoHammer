package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Shape;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.ScrollableData;
import javafx.geometry.Point2D;

public class DepthHeight extends DepthStart {
	
	public DepthHeight(Shape shape, ProfileField profField) {
		super(shape, profField);
	}

	@Override
	public void controlToSettings(TraceSample ts) {
		int max = profField.getMaxHeightInSamples();
		
		profField.getProfileSettings().hpage =
			Math.min(max - profField.getProfileSettings().getLayer(), Math.max(
				0, ts.getSample() - profField.getProfileSettings().getLayer()));
	}

	@Override
	public Point2D getCenter(ScrollableData profField) {
		Point2D scr = profField.traceSampleToScreen(new TraceSample(
				0, this.profField.getProfileSettings().getLayer() + this.profField.getProfileSettings().hpage));
		return profField instanceof ProfileField ?
				new Point2D(((ProfileField) profField).getVisibleStart(), scr.getY()) : scr;
	}
}
