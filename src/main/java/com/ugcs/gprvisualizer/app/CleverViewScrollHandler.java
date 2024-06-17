package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;

public class CleverViewScrollHandler implements MouseHandler {
	private ProfileField field;
	private ProfileField dragField;
	private Point dragPoint;
	private ProfileView cleverView;
	private TraceSample oldCenter;
	
	public CleverViewScrollHandler(ProfileView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.getField();
	}	
	
	public boolean mousePressHandle(Point localPoint, ProfileField profField) {        	
        	
    	dragField = new ProfileField(field);
		dragPoint = localPoint;    		
		oldCenter = dragField.screenToTraceSample(dragPoint);
		cleverView.repaintEvent();
		
    	return true;
	}

	public boolean mouseReleaseHandle(Point localPoint, ProfileField profField) {
		dragPoint = null;
		
		return false;
	}
	
	public boolean mouseMoveHandle(Point point, ProfileField profField) {
			
		if (dragPoint == null) {			
			return false;
		}
		
		try {
    		TraceSample newCenter = dragField.screenToTraceSample(point);
    		
    		int t = dragField.getSelectedTrace() 
    				+ oldCenter.getTrace() - newCenter.getTrace();
    		field.setSelectedTrace(t);
    		cleverView.getProfileScroll().recalc();
    		
    		field.setStartSample(dragField.getStartSample() 
    				+ oldCenter.getSample() - newCenter.getSample());

    		cleverView.repaintEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}	
}
