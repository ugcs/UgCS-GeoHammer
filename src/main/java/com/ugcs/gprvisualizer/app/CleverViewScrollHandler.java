package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

public class CleverViewScrollHandler implements MouseHandler {
	private VerticalCutField field;
	private VerticalCutField dragField;
	private Point dragPoint;
	private CleverImageView cleverView;
	
	public CleverViewScrollHandler(CleverImageView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.getField();
	}
	
	TraceSample oldCenter;
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {        	
        	
    	dragField = new VerticalCutField(field);
		dragPoint = localPoint;    		
		oldCenter = dragField.screenToTraceSample(dragPoint);
		cleverView.repaintEvent();

		
		
		
    	return true;
	};

	public boolean mouseReleaseHandle(Point localPoint, VerticalCutField vField) {
		dragPoint = null;
		
		return false;
	}
	
	public boolean mouseMoveHandle(Point point, VerticalCutField vField){
			
		if(dragPoint == null) {
			
			return false;
		}
		
		try {
    		TraceSample newCenter = dragField.screenToTraceSample(point);
    		
    		int t = dragField.getSelectedTrace() + oldCenter.getTrace() - newCenter.getTrace();
    		field.setSelectedTrace(t);
    		cleverView.s1.setValue(t);
    		
    		field.setStartSample(dragField.getStartSample() + oldCenter.getSample() - newCenter.getSample());

    		
    		cleverView.repaintEvent();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}	
}
