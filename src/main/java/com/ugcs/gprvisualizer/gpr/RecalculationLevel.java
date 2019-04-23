package com.ugcs.gprvisualizer.gpr;

public enum RecalculationLevel {

	JUST_AUX_GRAPHICS(1),
	BUFFERED_IMAGE(2),
	COORDINATES(3);
	
	private int level;
	
	RecalculationLevel(int level){
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
