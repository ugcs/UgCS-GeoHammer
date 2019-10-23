package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public interface Layer {

	void draw(Graphics2D g2);
	
	boolean isReady();
	
	void somethingChanged(WhatChanged changed);
	
	
	boolean mousePressed(Point2D point);
	
	boolean mouseRelease(Point2D point);
	
	boolean mouseMove(Point2D point);

	List<Node> getToolNodes();
	
	
}
