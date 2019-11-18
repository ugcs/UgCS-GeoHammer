package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.gpr.Model;

public class PrismImageProducer {

	int vscale = 4;
	public BufferedImage getImg(Model model, List<Trace> traces, double threshold) {
		int width = traces.size();
		int height = vscale * traces.get(0).getNormValues().length;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    int x=0;
	    int fileStartX = 0;
	    for(SgyFile sgyFile : model.getFileManager().getFiles()) {
	    	
	    	fileStartX = x;
	    	
		    for(Trace trace : sgyFile.getTraces()){
		    	
		    	float values[] = trace.getNormValues();
		    	
		    	for(int y=0; y< values.length; y++) {
		    		
		    		int c = (int) (127.0 + Math.tanh(values[y]/threshold) * 127.0);
		    		int color = (c << 16) + (c << 8) + c;
		    		for(int yt =0; yt < vscale; yt ++) {
		    			buffer[x + vscale * y  * width + yt * width ] = color;
		    		}
		    	}
		    	x++;
		    }
		    
		    List<Integer> lst = model.getFoundIndexes().get(sgyFile);
		    if(lst != null) {
			    Graphics2D g2 = (Graphics2D)image.getGraphics();
			    g2.setColor(Color.GREEN);
			    
			    Image img = ResourceImageHolder.IMG_SHOVEL;
		    	for(Integer i : lst) {		    		
					
					g2.drawImage(img, fileStartX + i - img.getWidth(null)/2 , height/2 - img.getHeight(null), null);
		    		
		    		//g2.fillOval(fileStartX + i, height -30, 17, 17);	    		
		    	}
		    }
	    }	    
	    
	    
	    return image;
	}
	
	
}
