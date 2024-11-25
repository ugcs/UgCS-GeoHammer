package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;
import javafx.geometry.Point2D;

public class CleverViewScrollHandler extends BaseObjectImpl {//implements MouseHandler {
	private ProfileField field;
	private ProfileField dragField;
	private Point2D dragPoint;
	private ProfileView cleverView;
	private TraceSample oldCenter;
	
	public CleverViewScrollHandler(ProfileView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.getField();
	}	

	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
        	
    	dragField = field;//new ProfileField(field);
		dragPoint = localPoint;    		
		oldCenter = dragField.screenToTraceSample(dragPoint);
		cleverView.repaintEvent();
		
    	return true;
	}

	@Override
	public boolean mouseReleaseHandle(Point2D localPoint, ScrollableData profField) {
		dragPoint = null;
		
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point2D point, ScrollableData profField) {
			
		if (dragPoint == null) {			
			return false;
		}
		
		try {
    		TraceSample newCenter = dragField.screenToTraceSample(point);
    		
    		int t = dragField.getMiddleTrace()
    				+ oldCenter.getTrace() - newCenter.getTrace();
    		field.setMiddleTrace(t);
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
