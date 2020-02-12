package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

public class CleverViewScrollHandler implements MouseHandler {
	private ProfileField field;
	private ProfileField dragField;
	private Point dragPoint;
	private ProfileView cleverView;
	
	public CleverViewScrollHandler(ProfileView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.getField();
	}
	
	TraceSample oldCenter;
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {        	
        	
    	dragField = new ProfileField(field);
		dragPoint = localPoint;    		
		oldCenter = dragField.screenToTraceSample(dragPoint);
		cleverView.repaintEvent();

		
		
		
    	return true;
	};

	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		dragPoint = null;
		
		return false;
	}
	
	public boolean mouseMoveHandle(Point point, ProfileField vField){
			
		if(dragPoint == null) {
			
			return false;
		}
		
		try {
    		TraceSample newCenter = dragField.screenToTraceSample(point);
    		
    		int t = dragField.getSelectedTrace() + oldCenter.getTrace() - newCenter.getTrace();
    		field.setSelectedTrace(t);
    		//cleverView.scrollBar.setValue(t);
    		cleverView.profileScroll.recalc();
    		
    		field.setStartSample(dragField.getStartSample() + oldCenter.getSample() - newCenter.getSample());

    		
    		cleverView.repaintEvent();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}	
}
