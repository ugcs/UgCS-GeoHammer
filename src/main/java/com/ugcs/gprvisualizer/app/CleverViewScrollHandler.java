package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.AuxElement;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;

import javafx.event.EventHandler;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

public class CleverViewScrollHandler implements MouseHandler {
	private VerticalCutField field;
	private VerticalCutField dragField;
	private Point dragPoint;
	private CleverImageView cleverView;
	
	public CleverViewScrollHandler(CleverImageView cleverView) {
		this.cleverView = cleverView;
		field = cleverView.field;
	}
	
	public void mousePressHandle(Point localPoint) {        	
        	
        	dragField = new VerticalCutField(field);
    		dragPoint = localPoint;    		
    		    		
    		cleverView.repaintEvent();

	};

	public void mouseReleaseHandle(Point localPoint) {
		dragPoint = null;	
	}
	
	public void mouseMoveHandle(Point point){
			
		if(dragPoint == null) {
			
			return;
		}
		
		try {
    		Point p = new Point(
    			dragPoint.x - point.x, 
    			dragPoint.y - point.y);
    		
    		TraceSample sceneCenter = dragField.screenToTraceSample(p);
    		
    		field.setSelectedTrace(sceneCenter.getTrace());
    		cleverView.s1.setValue(sceneCenter.getTrace());
    		field.setStartSample(sceneCenter.getSample());
    		
    		cleverView.repaintEvent();
		} catch(Exception e) {
			e.printStackTrace();
		}        	
	}	
}
