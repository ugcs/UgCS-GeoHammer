package com.ugcs.gprvisualizer.math;

public class NumberUtils {

	
	public static int norm(int i, int low, int hi) {
		
		return 
			Math.max(low,
				Math.min(hi, i));
		
	}
	
	
}
