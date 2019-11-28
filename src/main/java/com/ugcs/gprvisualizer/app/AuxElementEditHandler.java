package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.AuxElement;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;

public class AuxElementEditHandler implements MouseHandler {

	private CleverImageView cleverView;
	private AuxElement selectedAuxElement;
	private VerticalCutField field;
	private boolean moved = false;

	public AuxElementEditHandler(CleverImageView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.field;
	}

	@Override
	public void mousePressHandle(Point localPoint) {
		moved = false;

		selectedAuxElement = findElement(localPoint);

		if (selectedAuxElement != null) {

		} else {
			TraceSample ts = field.screenToTraceSample(localPoint);
			if (ts.getTrace() >= 0 && ts.getTrace() < cleverView.model.getFileManager().getTraces().size()) {
				selectedAuxElement = new AuxElement(cleverView.model.getFileManager().getTraces().get(ts.getTrace()),
						ts.getSample());
				cleverView.model.getAuxElements().add(selectedAuxElement);

				moved = true;
			}
		}
		cleverView.repaintEvent();

	}

	@Override
	public void mouseReleaseHandle(Point localPoint) {

		if (selectedAuxElement != null && !moved) {
			cleverView.model.getAuxElements().remove(selectedAuxElement);
		}
		selectedAuxElement = null;
	}

	@Override
	public void mouseMoveHandle(Point localPoint) {

		if (selectedAuxElement != null) {
			moved = true;
			TraceSample ts = field.screenToTraceSample(localPoint);
			if (ts.getTrace() >= 0 && ts.getTrace() < cleverView.model.getFileManager().getTraces().size()) {
				selectedAuxElement.setTraceStart(cleverView.model.getFileManager().getTraces().get(ts.getTrace()));
				selectedAuxElement.setSampleStart(ts.getSample());

				cleverView.repaintEvent();
				
				AppContext.notifyAll(new WhatChanged(Change.justdraw));
			}

		}
	}

	protected AuxElement findElement(Point localPoint) {
		Point p1 = ((Point) localPoint.clone());
		p1.translate(-5, -5);

		Point p2 = ((Point) localPoint.clone());
		p2.translate(5, 5);

		TraceSample ts1 = field.screenToTraceSample(p1);
		TraceSample ts2 = field.screenToTraceSample(p2);

		for (AuxElement au : cleverView.model.getAuxElements()) {
			if (au.getTraceStart().indexInSet >= ts1.getTrace() && au.getTraceStart().indexInSet <= ts2.getTrace()) {

				return au;
			}
		}

		return null;
	}

}
