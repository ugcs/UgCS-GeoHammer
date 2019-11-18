package com.ugcs.gprvisualizer.draw;

public abstract class BaseLayer implements Layer {
	private boolean active = true;

	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
