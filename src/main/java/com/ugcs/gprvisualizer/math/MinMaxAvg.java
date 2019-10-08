package com.ugcs.gprvisualizer.math;

public class MinMaxAvg {

	private double min;
	private double max;
	private boolean first  = true;
	
	public void put(double x) {
		if(first) {
			first = false;
			min = x;
			max = x;
		}
		
		min = Math.min(min, x);
		max = Math.max(max, x);
	}
	
	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getMid() {
		return (min + max) / 2;
	}
	
}
