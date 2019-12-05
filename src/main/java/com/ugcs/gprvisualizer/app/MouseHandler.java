package com.ugcs.gprvisualizer.app;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;

public interface MouseHandler {
	boolean mousePressHandle(Point localPoint, VerticalCutField vField);
	boolean mouseReleaseHandle(Point localPoint, VerticalCutField vField);
	boolean mouseMoveHandle(Point point, VerticalCutField vField);

	

}
