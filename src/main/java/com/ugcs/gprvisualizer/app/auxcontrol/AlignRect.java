package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class AlignRect {
	
	int hor;
	int ver;
	
	public static final AlignRect CENTER = new AlignRect(0, 0);
	
	public AlignRect(int h, int v) {
		this.hor = h;
		this.ver = v;
	}
	
	public Rectangle getRect(Point point, Dimension dim) {
		
		int rh = dim.width / 2;
		int rv = dim.height / 2;
		
		Rectangle rect = new Rectangle(point.x - rh + rh * hor, 
				point.y - rv + rv * ver, 
				dim.width, dim.height);
		return rect;		
	}
}
