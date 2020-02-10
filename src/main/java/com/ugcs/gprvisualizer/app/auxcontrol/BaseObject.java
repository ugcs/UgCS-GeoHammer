package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.MouseHandler;

public interface BaseObject extends MouseHandler {

	
	void drawOnMap(Graphics2D g2, MapField hField);
	void drawOnCut(Graphics2D g2, ProfileField vField);
	
	boolean isPointInside(Point localPoint, ProfileField vField);
	Rectangle getRect(ProfileField vField);
	
	void signal(Object obj);
	
	List<BaseObject> getControls();
	
	boolean saveTo(JSONObject json);
	
	boolean mousePressHandle(Point2D point, MapField field);
	 
	
	BaseObject copy(int offset, VerticalCutPart verticalCutPart);
	
	boolean isFit(int begin, int end);
	
	void setSelected(boolean selected);
	boolean isSelected();
}
