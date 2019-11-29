package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.MouseHandler;

public interface BaseObject extends MouseHandler {

	
	void drawOnMap(Graphics2D g2);
	void drawOnCut(Graphics2D g2);
	
	boolean isPointInside(Point localPoint);
	
	void signal(Object obj);
	
	List<BaseObject> getControls();
	
	void saveTo(JSONObject json);
}
