package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import javafx.scene.input.MouseDragEvent;

public interface MouseHandler {
	void mousePressHandle(Point localPoint);
	void mouseReleaseHandle(Point localPoint);
	void mouseMoveHandle(Point point);


}
