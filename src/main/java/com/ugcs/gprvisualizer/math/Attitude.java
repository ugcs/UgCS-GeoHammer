package com.ugcs.gprvisualizer.math;

public class Attitude {

	//крен
	public double roll;
	//тангаж
	public double tangage;
	//рыскание
	public double yaw; 	
	
	public Attitude(double roll, double tangage, double yaw) {
		this.roll = roll;
		this.tangage = tangage;
		this.yaw = yaw;		
	}

}
