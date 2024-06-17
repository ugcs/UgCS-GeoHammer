package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.github.thecoldwine.sigrun.common.ext.MapField;

public interface Layer extends ToolProducer {

	void draw(Graphics2D g2, MapField field);
	
	boolean isReady();
	
	void somethingChanged(WhatChanged changed);
	
	
	boolean mousePressed(Point2D point);
	
	boolean mouseRelease(Point2D point);
	
	boolean mouseMove(Point2D point);

}