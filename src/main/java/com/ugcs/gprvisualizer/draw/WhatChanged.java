package com.ugcs.gprvisualizer.draw;

public class WhatChanged {

	private Change change;
	
	public WhatChanged(Change change) {
		this.change = change;
	}
	
	public boolean isZoom() {
		return change == Change.mapzoom;
	}
	public boolean isAdjusting() {
		return change == Change.adjusting;
	}
	public boolean isTraces() {
		return change == Change.traces;
	}
	public boolean isFileopened() {
		return change == Change.fileopened;
	}
	public boolean isMapscroll() {
		return change == Change.mapscroll;
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
}
