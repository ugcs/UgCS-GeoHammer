package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Image;
import java.awt.Point;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;

public class ToggleButton extends DragAnchor {

	Image selectedImg;
	Image unselectedImg;
	boolean selected = false;
	
	public ToggleButton(MutableInt trace, MutableInt sample, 
			Image selectedImg, 
			Image unselectedImg,
			AlignRect alignRect,
			VerticalCutPart offset) {
		super(trace, sample, unselectedImg, alignRect, offset);
		
		this.selectedImg = selectedImg;
		this.unselectedImg = unselectedImg;		
	}
	
	protected Image getImg() {
		if(selected) {
			return selectedImg;
		}else {
			return unselectedImg;
		}
	}
	

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
			selected = !selected;
			
			signal(selected);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseMoveHandle(Point point, VerticalCutField vField) {
		
		return false;
	}
}
