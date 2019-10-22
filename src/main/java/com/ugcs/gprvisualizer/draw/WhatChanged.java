package com.ugcs.gprvisualizer.draw;

public class WhatChanged {

	private boolean mapscroll;
	private boolean zoom;
	private boolean adjusting;
	private boolean traces;
	private boolean fileopened;
	
	public boolean isZoom() {
		return zoom;
	}
	public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}
	public boolean isAdjusting() {
		return adjusting;
	}
	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}
	public boolean isTraces() {
		return traces;
	}
	public void setTraces(boolean traces) {
		this.traces = traces;
	}
	public boolean isFileopened() {
		return fileopened;
	}
	public void setFileopened(boolean fileopened) {
		this.fileopened = fileopened;
	}
	public boolean isMapscroll() {
		return mapscroll;
	}
	public void setMapscroll(boolean mapscroll) {
		this.mapscroll = mapscroll;
	}
	
	
	
	
}
