package com.ugcs.gprvisualizer.app;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.AuxElement;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class AuxElementEditHandler implements MouseHandler {

	private Model model;
	private CleverImageView cleverView;
	
	private AuxElement selectedAuxElement;
	private VerticalCutField field;
	private boolean moved = false;
	
	//private List<BaseObject> controls = null;
	private BaseObject selected;
	private MouseHandler mouseInput;
	

	public AuxElementEditHandler(CleverImageView cleverView) {
		this.cleverView = cleverView;
		this.model = this.cleverView.model;
		field = cleverView.getField();
	}

	@Override
	public boolean mousePressHandle(Point localPoint) {
		
		boolean processed = false;
		if(model.getControls() != null) {
			processed = processPress(model.getControls(), localPoint);
		}
		
		if(!processed && selected != null) {
			processed = selected.mousePressHandle(localPoint);
			if(processed) {
				mouseInput = selected;
			}
		}
		
		if(!processed) {
			processed = processPress1(cleverView.model.getAuxElements(), localPoint);
		}
		
		if(!processed) {
			//deselect
			mouseInput = null;
			selected = null;
			model.setControls(null);
		}
		
		moved = false;

		if(processed) {
			cleverView.repaintEvent();
		}

		return processed;
	}

	private boolean processPress(List<BaseObject> controls2, Point localPoint) {
		for(BaseObject o : controls2) {
			if(o.isPointInside(localPoint)) {
				
				o.mousePressHandle(localPoint);
				mouseInput = o;
				
				return true;
			}
		}
		
		return false;
	}

	private boolean processPress1(List<BaseObject> controls2, Point localPoint) {
		for(BaseObject o : controls2) {
			if(o.isPointInside(localPoint)) {
				
				selected  = o;
				model.setControls(null);
				List<BaseObject> c = selected.getControls();
				if(c != null) {
					model.setControls(c);
				}
				
				return true;
			}
		}
		
		return false;
	}
	/**
				

	 */

	@Override
	public boolean mouseReleaseHandle(Point localPoint) {

		if(mouseInput != null) {			
			mouseInput.mouseReleaseHandle(localPoint);
			mouseInput = null;
			
			cleverView.repaintEvent();
			return true;
		}
		
		
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point localPoint) {

		if(mouseInput != null) {			
			mouseInput.mouseMoveHandle(localPoint);
			
			cleverView.repaintEvent();
			
			return true;
		}
		
		
		
		return false;
	}


}
