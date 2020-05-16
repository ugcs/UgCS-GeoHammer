package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public interface Layer extends ToolProducer{

	void draw(Graphics2D g2);
	
	boolean isReady();
	
	void somethingChanged(WhatChanged changed);
	
	
	boolean mousePressed(Point2D point);
	
	boolean mouseRelease(Point2D point);
	
	boolean mouseMove(Point2D point);

	
	
	
}
