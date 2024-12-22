package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import com.ugcs.gprvisualizer.app.ScrollableData;
import javafx.geometry.Point2D;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.event.WhatChanged;

public class DepthStart extends BaseObjectImpl {

	int horM;
	int verM;
	int offsetX;
	int offsetY;
		
	Shape shape;
	ProfileField profField;
	
	public DepthStart(Shape shape, ProfileField profField) {
		this.shape = shape;
		this.profField = profField;

		horM = shape.getBounds().width;
		verM = shape.getBounds().height;
		offsetX = shape.getBounds().x;
		offsetY = shape.getBounds().y;
	}
	
	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
		if (isPointInside(localPoint, profField)) {
			return true;
		}
		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
		return null;
	}


	@Override
	public boolean mouseMoveHandle(Point2D point, ScrollableData profField) {
		TraceSample ts = profField.screenToTraceSample(point);
		controlToSettings(ts);

		AppContext.model.publishEvent(new WhatChanged(this, WhatChanged.Change.adjusting));

		return true;
	}

	public void controlToSettings(TraceSample ts) {
		int max = profField.getMaxHeightInSamples();
		profField.getProfileSettings().setLayer(Math.min(max - profField.getProfileSettings().hpage,
			Math.max(0, ts.getSample())));
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField profField) {
		
		setClip(g2, profField.getClipLeftMainRect());
		
		Point2D p = getCenter(profField);

		g2.setColor(Color.BLUE);
		
		g2.translate(p.getX(), p.getY());
		g2.fill(shape);
		
		if (isSelected()) {
			g2.setColor(Color.green);
			g2.setStroke(FoundPlace.SELECTED_STROKE);
			g2.draw(shape);
		}
		
		g2.translate(-p.getX(), -p.getY());
	}

	@Override
	public boolean isPointInside(Point2D localPoint, ScrollableData profField) {
		Rectangle rect = getRect(profField);
		return rect.contains(localPoint.getX(), localPoint.getY());
	}
	
	//@Override
	private Rectangle getRect(ScrollableData profField) {
		Point2D scr = getCenter(profField);
		Rectangle rect = new Rectangle((int) scr.getX() + offsetX,(int) scr.getY() + offsetY, horM, verM);
		return rect;
	}

	public Point2D getCenter(ScrollableData profField) {
		Point2D scr = profField.traceSampleToScreen(new TraceSample(
				0, this.profField.getProfileSettings().getLayer()));
		return profField instanceof ProfileField ?
				new Point2D(((ProfileField) profField).getVisibleStart(), scr.getY()) : scr;
	}
}