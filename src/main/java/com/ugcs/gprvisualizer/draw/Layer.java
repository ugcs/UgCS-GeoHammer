package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import javafx.scene.input.MouseEvent;

public interface Layer {

	void draw(Graphics2D g2);
	
	boolean isReady();
	
	void somethingChanged(WhatChanged changed);
	
	
	boolean mousePressed(MouseEvent event);
	
	boolean mouseRelease(MouseEvent event);
	
	boolean mouseMove(MouseEvent event);
}
