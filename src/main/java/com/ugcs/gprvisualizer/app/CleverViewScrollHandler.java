package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.geometry.Point2D;

public class CleverViewScrollHandler extends BaseObjectImpl {//implements MouseHandler {
	private final GPRChart field;
	private GPRChart dragField;
	private Point2D dragPoint;
	private final GPRChart cleverView;
	private TraceSample oldCenter;
	
	public CleverViewScrollHandler(GPRChart cleverView) {
		this.cleverView = cleverView;
		field = cleverView;
	}	

	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
        	
    	dragField = field;//new ProfileField(field);
		dragPoint = localPoint;    		
		oldCenter = dragField.screenToTraceSample(dragPoint);

		//TODO:
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

			AppContext.model.publishEvent(new WhatChanged(cleverView, WhatChanged.Change.justdraw));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}	
}
