package com.ugcs.gprvisualizer.draw;

public class WhatChanged {

	private Change change;
	
	public String toString() {
		return change.name();
	}
	
	public WhatChanged(Change change) {		
		
		this.change = change;
	}
	
	public boolean isZoom() {
		return change == Change.mapzoom;
	}
	
	public boolean isAdjusting() {
		return change == Change.adjusting;
	}
	
	public boolean isTraceCut() {
		return change == Change.traceCut;
	}
	
	public boolean isFileopened() {
		return change == Change.fileopened;
	}
	
	public boolean isUpdateButtons() {
		return change == Change.updateButtons;
	}
	
	public boolean isMapscroll() {
		return change == Change.mapscroll;
	}
	
	public boolean isProfilescroll() {
		return change == Change.profilescroll;
	}
	
	public boolean isJustdraw() {
		return change == Change.justdraw;
	}
	
	public boolean isWindowresized() {
		return change == Change.windowresized;
	}
	
	public boolean isTraceValues() {
		return change == Change.traceValues;
	}
	
	public boolean isAuxOnMapSelected() {
		return change == Change.auxOnMapSelected;
	}

    public boolean isFileSelected() {
		return change == Change.fileSelected;
    }
}
