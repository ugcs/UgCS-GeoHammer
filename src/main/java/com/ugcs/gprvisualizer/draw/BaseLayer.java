package com.ugcs.gprvisualizer.draw;

import java.awt.Dimension;

import com.ugcs.gprvisualizer.gpr.Model;

public abstract class BaseLayer implements Layer {
	private boolean active = true;

	protected Dimension parentDimension;
	
	protected Model model; 
	
	protected BaseLayer(Dimension parentDimension, Model model){
		this.parentDimension = parentDimension;
		this.model = model;
	}

	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
