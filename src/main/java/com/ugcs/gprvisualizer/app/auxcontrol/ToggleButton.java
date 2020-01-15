package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Image;
import java.awt.Point;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;

public class ToggleButton extends DragAnchor {

	Image selectedImg;
	Image unselectedImg;
	private boolean selected = false;
	
	public ToggleButton( 
			Image selectedImg, 
			Image unselectedImg,
			AlignRect alignRect,
			VerticalCutPart offset,
			boolean selected) {
		super(selected ? selectedImg : unselectedImg, alignRect, offset);
		
		this.selectedImg = selectedImg;
		this.unselectedImg = unselectedImg;
		this.setSelected(selected);		
		
	}
	
	protected Image getImg() {
		if(isSelected()) {
			return selectedImg;
		}else {
			return unselectedImg;
		}
	}
	

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
			setSelected(!isSelected());
			
			signal(isSelected());
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseMoveHandle(Point point, VerticalCutField vField) {
		
		return false;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
