package com.github.thecoldwine.sigrun.common.ext;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

public class PrismImageProducer {

	
	
	public BufferedImage getImg(List<Trace> traces) {
		int width = traces.size();
		int height = traces.get(0).getNormValues().length;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    for(int x=0; x<width; x++){
	    	
	    	Trace trace = traces.get(x);
	    	float values[] = trace.getNormValues();
	    	
	    	for(int y=0; y< values.length; y++) {
	    		
	    		int c = (int) (127.0 + Math.tanh(values[y]/900.0) * 127.0);
	    		int color = (c << 16) + (c << 8) + c;
	    		buffer[x + y * width] = color;
	    	}
	    }
		
	    
	    
	    return image;
	}
}
