package com.ugcs.gprvisualizer.draw;

import java.awt.Dimension;

public abstract class BaseLayer implements Layer {
	private boolean active = true;
	
	//protected Dimension parentDimension;
	
	private RepaintListener listener;

	
	protected BaseLayer(){
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

//	public void setDimension(Dimension parentDimension) {
//		this.parentDimension = parentDimension;
//	}
//
//	public Dimension getDimension() {
//		return parentDimension;
//	}
	
	public void setRepaintListener(RepaintListener listener) {
		this.listener = listener;
	}
	
	public RepaintListener getRepaintListener() {
		return listener;
	}	
	
}
