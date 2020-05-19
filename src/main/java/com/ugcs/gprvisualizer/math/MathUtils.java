package com.ugcs.gprvisualizer.math;

public class MathUtils {

	
	public static int norm(int i, int min, int max) {

		return Math.min(Math.max(i, min), max - 1);
	}
	
	public static double norm(double i, double min, double max) {

		return Math.min(Math.max(i, min), max);
	}

	public static float norm(float i, float min, float max) {

		return Math.min(Math.max(i, min), max);
	}
}
