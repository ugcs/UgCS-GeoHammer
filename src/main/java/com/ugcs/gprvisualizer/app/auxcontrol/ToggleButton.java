package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Image;
import java.awt.Point;

import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;

public class ToggleButton extends DragAnchor {

	Image selectedImg;
	Image unselectedImg;
	boolean selected = false;
	
	public ToggleButton(VerticalCutField vField, int trace, int sample, 
			Image selectedImg, 
			Image unselectedImg,
			AlignRect alignRect) {
		super(vField, trace, sample, unselectedImg, alignRect);
		
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
	public boolean mousePressHandle(Point localPoint) {
		
		if(isPointInside(localPoint)) {
			
			selected = !selected;
			
			signal(selected);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseMoveHandle(Point point) {
		
		return false;
	}
}
