package com.ugcs.gprvisualizer.app.auxcontrol;

public abstract class BaseObjectImpl implements BaseObject {

	private boolean selected = false;
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}

}
