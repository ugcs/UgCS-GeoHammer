package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;

public interface Layer {

	void draw(Graphics2D g2);
	
	boolean isReady();
	
	void somethingChanged(WhatChanged changed);
	
	

}
