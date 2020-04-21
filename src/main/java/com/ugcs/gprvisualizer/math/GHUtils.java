package com.ugcs.gprvisualizer.math;

public class GHUtils {

	
	public static int norm(int i, int min, int max) {

		return Math.min(Math.max(i, min), max - 1);
	}
	
}
