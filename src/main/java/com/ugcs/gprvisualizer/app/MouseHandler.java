package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import javafx.scene.input.MouseDragEvent;

public interface MouseHandler {
	boolean mousePressHandle(Point localPoint);
	boolean mouseReleaseHandle(Point localPoint);
	boolean mouseMoveHandle(Point point);


}
