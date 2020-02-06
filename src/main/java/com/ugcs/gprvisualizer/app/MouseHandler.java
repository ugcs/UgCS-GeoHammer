package com.ugcs.gprvisualizer.app;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;

public interface MouseHandler {
	boolean mousePressHandle(Point localPoint, ProfileField vField);
	boolean mouseReleaseHandle(Point localPoint, ProfileField vField);
	boolean mouseMoveHandle(Point point, ProfileField vField);

	

}
