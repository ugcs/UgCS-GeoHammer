package com.ugcs.gprvisualizer.gpr;

public class ScaleArrayBuilder implements ArrayBuilder {

	private Settings settings;

	private double[][] scaleArray = new double[2][512];
	
	public ScaleArrayBuilder(Settings settings) {
		this.settings = settings;
	}
	
	/* (non-Javadoc)
	 * @see com.ugcs.gprvisualizer.gpr.ArrayBuilder#build()
	 */
	@Override
	public double[][] build() {
		
		for(int i=0; i < settings.maxsamples; i++) {
			scaleArray[0][i] = settings.threshold;
			scaleArray[1][i] = (settings.topscale + (settings.bottomscale - settings.topscale) *i / settings.maxsamples)/10000.0;
		}
		
		return scaleArray;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
