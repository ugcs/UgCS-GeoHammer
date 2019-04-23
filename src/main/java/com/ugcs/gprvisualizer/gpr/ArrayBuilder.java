package com.ugcs.gprvisualizer.gpr;

public interface ArrayBuilder {

	/**
	 * 
	 * @return [0] - threshold,  [1] - scale  
	 */
	double[][] build();
	
	
	void clear();

}