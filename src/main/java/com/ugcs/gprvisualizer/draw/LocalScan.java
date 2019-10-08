package com.ugcs.gprvisualizer.draw;

import com.ugcs.gprvisualizer.gpr.Scan;

public class LocalScan {

	
	private Scan scan;
	private boolean beginOfTrackTrack;
	private int localX;
	private int localY;
	
	public LocalScan(
		Scan scan,
		int localX,
		int localY,
		boolean beginOfTrack) {
		
		this.scan = scan;
		this.localX = localX;
		this.localY = localY;
		this.beginOfTrackTrack = beginOfTrack;
	}
	
	public Scan getScan() {
		return scan;
	}
	
	public boolean isBeginOfTrack() {
		return beginOfTrackTrack;
	}
	
	public int getLocalX() {
		return localX;
	}
	
	public int getLocalY() {
		return localY;
	}
	
	
}
