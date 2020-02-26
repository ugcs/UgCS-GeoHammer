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

	int HOR_M;
	int VER_M;
	int offsetX;
	int offsetY;
	
	Model model = AppContext.model;
	
	Shape shape;
	
	public DepthStart(Shape shape){
		this.shape = shape;
		HOR_M = shape.getBounds().width;
		VER_M = shape.getBounds().height;
		offsetX = shape.getBounds().x;
		offsetY = shape.getBounds().y;
		//System.out.println(" offsetX " + offsetX + "  offsetY " + offsetY  + "  " + HOR_M + " " + VER_M );
	}
	
	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		if(isPointInside(localPoint, vField)) {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
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
		
		//AppContext.notifyAll(new WhatChanged(Change.));
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		//Rectangle r = getRect(vField);
		//g2.drawRect(r.x, r.y, r.width, r.height);
		
		
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
		Rectangle rect = new Rectangle(scr.x+offsetX, scr.y+offsetY, HOR_M, VER_M);
		return rect;
	}

	public Point getCenter(ProfileField vField) {
		Point scr = vField.traceSampleToScreen(new TraceSample(0, model.getSettings().layer));
		scr.x = vField.visibleStart;
		return scr;
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BaseObject> getControls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveTo(JSONObject json) {
		return false;
		
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFit(int begin, int end) {
		// TODO Auto-generated method stub
		return false;
	}

}
