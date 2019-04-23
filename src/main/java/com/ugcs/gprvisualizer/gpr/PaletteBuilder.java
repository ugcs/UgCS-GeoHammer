package com.ugcs.gprvisualizer.gpr;

public class PaletteBuilder {

	

	
	public int[] build() {
		
		int[] palette = new int[15000];
		
		for(int i=0; i< palette.length; i++){
			
			double t= ((double)i) / 25;
			
			int r = ((int)((Math.cos(t*1.50)+1)/2 * 255.0 ) ) & 0xff;
			int g = ((int)((Math.cos(t*1.23)+1)/2 * 255.0 ) ) & 0xff;
			int b = ((int)((Math.cos(t*1.00)+1)/2 * 255.0 ) ) & 0xff;
			
			palette[i] = r + (g << 8) + (b << 16);
		}
		
		
		return palette;
	}

	
}
