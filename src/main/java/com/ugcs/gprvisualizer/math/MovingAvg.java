package com.ugcs.gprvisualizer.math;

public class MovingAvg {

	double sum;
	double div;
	double reduce = 0.93;
	public MovingAvg() {
		
	}
	
	public void add(int val) {
		sum+=val;
		div+=1;
		
		sum *= reduce;
		div *= reduce;
	}
	
	public int get() {
		return (int) Math.round(sum/div); 
	}
	
}
