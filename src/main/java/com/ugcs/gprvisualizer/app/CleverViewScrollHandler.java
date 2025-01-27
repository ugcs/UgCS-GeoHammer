package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.geometry.Point2D;

public class CleverViewScrollHandler extends BaseObjectImpl {//implements MouseHandler {
	//private final GPRChart field;
	//private GPRChart dragField;
	private Point2D dragPoint;
	//private final ScrollableData cleverView;
	private TraceSample oldCenter;
	
	public CleverViewScrollHandler(ScrollableData cleverView) {
		//this.cleverView = cleverView;
		//field = cleverView;
	}	

	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
        	
    	//dragField = field;//new ProfileField(field);
		dragPoint = localPoint;    		
		oldCenter = profField.screenToTraceSample(dragPoint);

		//TODO:
		//cleverView.repaintEvent();
		
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
    		TraceSample newCenter = profField.screenToTraceSample(point);
    		
    		int t = profField.getMiddleTrace()
    				+ oldCenter.getTrace() - newCenter.getTrace();

			profField.setMiddleTrace(t);
			if (profField instanceof GPRChart gprChart) {
				gprChart.getProfileScroll().recalc();
				gprChart.setStartSample(profField.getStartSample()
						+ oldCenter.getSample() - newCenter.getSample());
			}

			AppContext.model.publishEvent(new WhatChanged(profField, WhatChanged.Change.justdraw));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}	
}