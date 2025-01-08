package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Shape;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.GPRChart;
import com.ugcs.gprvisualizer.app.ScrollableData;
import javafx.geometry.Point2D;

public class DepthHeight extends DepthStart {
	
	public DepthHeight(Shape shape) { //, GPRChart profField) {
		super(shape); //, profField);
	}

	@Override
	public void controlToSettings(TraceSample ts, ProfileField profField) {
		int max = profField.getMaxHeightInSamples();
		
		profField.getProfileSettings().hpage =
			Math.min(max - profField.getProfileSettings().getLayer(), Math.max(
				0, ts.getSample() - profField.getProfileSettings().getLayer()));
	}

	@Override
	public Point2D getCenter(ScrollableData scrollable) {
		if (scrollable instanceof GPRChart gprChart) {
			var profField = gprChart.getField();
			Point2D scr = gprChart.traceSampleToScreen(new TraceSample(
					0, profField.getProfileSettings().getLayer() + profField.getProfileSettings().hpage));
			return new Point2D(gprChart.getField().getVisibleStart(), scr.getY());
		} else {
			return scrollable.traceSampleToScreen(new TraceSample(0, 0));
		}
	}
}
