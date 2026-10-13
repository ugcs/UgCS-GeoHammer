package com.ugcs.geohammer.model.element;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

public class ShapeHolder {

	public static final Shape FLAG = createFlag();

	public static final Shape TOP_SELECTION = createTopSelection();

	public static final Shape BOTTOM_SELECTION = createBottomSelection();
	
	static Shape createFlag() {
		GeneralPath result = new GeneralPath(Path2D.WIND_NON_ZERO, 5);
		int topY = -FoundPlace.FLAG_POLE_HEIGHT;
		result.moveTo(0, 0);
		result.lineTo(0, topY);
		result.lineTo(15, topY);
		result.lineTo(10, topY + 5);
		result.lineTo(15, topY + 10);
		result.lineTo(1, topY + 10);
		result.lineTo(1, 0);
	    result.closePath();
	    return result;
	}

	static Shape createTopSelection() {
		GeneralPath result = new GeneralPath(Path2D.WIND_NON_ZERO, 5);
	    double step = 5;
	    
	    result.moveTo(0, 0);
	    result.lineTo(0, -3 * step);
	    result.lineTo(3 * step, 0);
	    result.closePath();
	    return result;
	}

	static Shape createBottomSelection() {
		GeneralPath result = new GeneralPath(Path2D.WIND_NON_ZERO, 5);
	    double step = 5;
	    
	    result.moveTo(0, 0);
	    result.lineTo(0, 3 * step);
	    result.lineTo(3 * step, 0);	    
	    result.closePath();
	    return result;
	}
}
