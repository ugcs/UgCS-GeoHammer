package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;

public interface MouseHandler {
	
	boolean mousePressHandle(Point localPoint, ProfileField profField);
	
	boolean mouseReleaseHandle(Point localPoint, ProfileField profField);
	
	boolean mouseMoveHandle(Point point, ProfileField profField);

}
