package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.MouseHandler;

public interface BaseObject extends MouseHandler {

	
	void drawOnMap(Graphics2D g2, Field hField);
	void drawOnCut(Graphics2D g2, VerticalCutField vField);
	
	boolean isPointInside(Point localPoint, VerticalCutField vField);
	Rectangle getRect(VerticalCutField vField);
	
	void signal(Object obj);
	
	List<BaseObject> getControls();
	
	void saveTo(JSONObject json);
	
	boolean mousePressHandle(Point2D point, Field field);
	 
}
