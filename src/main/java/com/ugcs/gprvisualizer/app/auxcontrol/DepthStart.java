package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class DepthStart extends BaseObjectImpl implements BaseObject, MouseHandler {

	int horM;
	int verM;
	int offsetX;
	int offsetY;
	
	Model model = AppContext.model;
	
	Shape shape;
	
	public DepthStart(Shape shape){
		this.shape = shape;
		horM = shape.getBounds().width;
		verM = shape.getBounds().height;
		offsetX = shape.getBounds().x;
		offsetY = shape.getBounds().y;
	}
	
	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField profField) {
		if(isPointInside(localPoint, profField)) {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point, ProfileField vField) {
		TraceSample ts = vField.screenToTraceSample(point);
		
		controlToSettings(ts);
		
		AppContext.notifyAll(new WhatChanged(Change.adjusting));
		
		return true;
	}

	public void controlToSettings(TraceSample ts) {
		int max = model.getMaxHeightInSamples();
		model.getSettings().layer = Math.min( max-model.getSettings().hpage, Math.max(0, ts.getSample()));
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		
		setClip(g2, vField.getClipLeftMainRect());
		
		Point p = getCenter(vField);

		g2.setColor(Color.BLUE);
		
		g2.translate(p.x , p.y);
		g2.fill(shape);
		
		if(isSelected()) {
			g2.setColor(Color.green);
			g2.setStroke(FoundPlace.SELECTED_STROKE);
			g2.draw(shape);
		}
		
		g2.translate(-p.x , -p.y);
	}

	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}
	
	@Override
	public Rectangle getRect(ProfileField vField) {
		
		Point scr = getCenter(vField);
		Rectangle rect = new Rectangle(scr.x+offsetX, scr.y+offsetY, horM, verM);
		return rect;
	}

	public Point getCenter(ProfileField vField) {
		Point scr = vField.traceSampleToScreen(new TraceSample(0, model.getSettings().layer));
		scr.x = vField.visibleStart;
		return scr;
	}

	@Override
	public void signal(Object obj) {
		
	}

	@Override
	public List<BaseObject> getControls() {
		return null;
	}

	@Override
	public boolean saveTo(JSONObject json) {
		return false;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {

		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {

		return null;
	}

	@Override
	public boolean isFit(int begin, int end) {

		return false;
	}

}
