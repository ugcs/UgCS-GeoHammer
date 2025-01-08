package com.ugcs.gprvisualizer.gpr;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;

public interface ArrayBuilder {

	/**
	 * builds. 
	 * @return [0] - threshold,  [1] - scale  
	 */
	double[][] build(SgyFile file);
	
	void clear();

}