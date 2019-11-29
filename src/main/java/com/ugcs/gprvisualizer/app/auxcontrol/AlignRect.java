package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class AlignRect {
	
	int h;
	int v;
	
	public static final AlignRect CENTER = new AlignRect(0, 0);
	
	public AlignRect(int h, int v) {
		this.h = h ;
		this.v = v;
	}
	
	public Rectangle getRect(Point point, Dimension dim) {
		
		int rh = dim.width / 2;
		int rv = dim.height / 2;
		
		Rectangle rect = new Rectangle(point.x -rh +rh*h, point.y -rv +rv*v, dim.width, dim.height);
		return rect;		
	}
}
