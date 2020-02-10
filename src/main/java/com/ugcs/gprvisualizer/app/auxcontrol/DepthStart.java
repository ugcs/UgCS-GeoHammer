package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class DepthStart extends BaseObjectImpl implements BaseObject, MouseHandler {

	static int HOR_M = ShapeHolder.topSelection.getBounds().width;
	static int VER_M = ShapeHolder.topSelection.getBounds().height;

	Model model = AppContext.model;
	
	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		if(isPointInside(localPoint, vField)) {
			
			//AppContext.model.getField().setSceneCenter(getTrace().getLatLon());
			
			//AppContext.notifyAll(new WhatChanged(Change.adjusting));
			
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
		
		int max = model.getMaxHeightInSamples();
		model.getSettings().layer = Math.min( max-1, Math.max(0, ts.getSample()));
		
		AppContext.notifyAll(new WhatChanged(Change.adjusting));
		
		return true;
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		Rectangle rect = getRect(vField);
		//ShapeHolder.topSelection.
		g2.setColor(Color.CYAN);
		
		g2.translate(rect.x , rect.y+rect.height);
		g2.fill(ShapeHolder.topSelection);
		
		if(isSelected()) {
			g2.setColor(Color.green);
			g2.setStroke(FoundPlace.SELECTED_STROKE);
			g2.draw(ShapeHolder.topSelection);
		}
		
		g2.translate(-rect.x , -(rect.y+rect.height));
		
	}

	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	@Override
	public Rectangle getRect(ProfileField vField) {
		
		Point scr = vField.traceSampleToScreen(new TraceSample(0, model.getSettings().layer));
		
		
		Rectangle rect = new Rectangle(vField.visibleStart, scr.y-VER_M, HOR_M, VER_M);
		return rect;
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
	public void saveTo(JSONObject json) {
		// TODO Auto-generated method stub
		
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
