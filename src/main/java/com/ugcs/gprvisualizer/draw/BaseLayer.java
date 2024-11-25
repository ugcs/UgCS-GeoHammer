package com.ugcs.gprvisualizer.draw;

public abstract class BaseLayer implements Layer {
	
	private boolean active = true;
		
	private RepaintListener listener;
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void setRepaintListener(RepaintListener listener) {
		this.listener = listener;
	}
	
	public RepaintListener getRepaintListener() {
		return listener;
	}	
	
}
