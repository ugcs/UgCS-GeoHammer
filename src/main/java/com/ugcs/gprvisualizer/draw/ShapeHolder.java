package com.ugcs.gprvisualizer.draw;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

public class ShapeHolder {

	public static Shape flag = getFlag();
	
	static Shape getFlag() {
		GeneralPath result = new GeneralPath(Path2D.WIND_NON_ZERO, 5);
	    double step = 5;
	    
	    result.moveTo(0, 0);
	    
	    result.lineTo(0, -5*step);	    
	    result.lineTo(3*step, -5*step);	    
	    result.lineTo(2*step, -4*step);	    
	    result.lineTo(3*step, -3*step);	    
	    result.lineTo(2, -3*step);
	    result.lineTo(2, 0);
	    
	    result.closePath();
	    return result;
	}
	
}
