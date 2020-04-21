package com.ugcs.gprvisualizer.math;

import java.util.Arrays;

public class ScanProfile {

	public double [] intensity;
	public double maxVal;
	
	public ScanProfile(int size) {
		
		intensity = new double[size];
		
		
	}


	public void finish() {
		maxVal = Arrays.stream(intensity).max().getAsDouble();		
	}
	
}
