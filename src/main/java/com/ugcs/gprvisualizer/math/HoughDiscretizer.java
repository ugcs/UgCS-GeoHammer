package com.ugcs.gprvisualizer.math;

public class HoughDiscretizer {

	public static final int DISCRET_SIZE = 21;
	public static final double DISCRET_GOOD_FROM = 4;
	
	public static final double FACTORX_FROM = 0.5 + 0.045;	
	public static final double FACTORX_TO = 1.5;
	public static final double FACTORX_WIDTH = FACTORX_TO - FACTORX_FROM;
	
	public int transform(double factorX) {
		
		if (factorX < FACTORX_FROM) {
			return 0;
		}
		
		double norm = (factorX - FACTORX_FROM) 
				/ (FACTORX_TO - FACTORX_FROM);

		if (norm > 1) {
			return DISCRET_SIZE;
		}
		
		int res = (int) Math.round(norm * (DISCRET_SIZE - 1));
		
		return res;// > 1 ? res : 0;
		
	}
	
	
}
