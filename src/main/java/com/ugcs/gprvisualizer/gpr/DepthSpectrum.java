package com.ugcs.gprvisualizer.gpr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class DepthSpectrum {

	private Settings settings;
	private float[] maxvalues = new float[1];
	private float[] avgvalues = new float[1];
	private double avgcount=0;
	private int[] palette = new PaletteBuilder().build();
	
	
	private double[][] scaleArray;
	private ArrayBuilder scaleArrayBuilder;
	
	public DepthSpectrum(Settings settings){
		
		this.settings = settings;
		
		scaleArrayBuilder = new ScaleArrayBuilder(settings);
	}

		
	
	public void analyze(float[] values) {
		if(maxvalues.length < values.length) {
			float[] tmp = new float[values.length];
			System.arraycopy(maxvalues, 0, tmp, 0, maxvalues.length);
			maxvalues = tmp;
		}

		if(avgvalues.length < values.length) {
			float[] tmp = new float[values.length];
			System.arraycopy(avgvalues, 0, tmp, 0, avgvalues.length);
			avgvalues = tmp;
		}

		for(int i=0; i<values.length; i++) {
			maxvalues[i] = Math.max(maxvalues[i], Math.abs(values[i]));
			
			avgvalues[i] += Math.abs(values[i]);
		}
		avgcount++;
	}
	
	public BufferedImage toImg(int width, int height){
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    double scale = (double)maxvalues.length / (double)width;
	    
	    scaleArray = scaleArrayBuilder.build();
	    
	    int ivalprev = -1;
	    int valindexprev = -1;
	    for(int imgx=0; imgx<width; imgx++){
	    	int valindex = (int)(imgx * scale);

	    	//avg
	    	double val2 = Math.max(0, avgvalues[valindex]/avgcount - settings.threshold) * scaleArray[1][valindex];
	    	int ival2 = (int)val2;		
	    	int color2 = palette[ Math.min(ival2, 200)];
	    	
	    	for(int imgy=0; imgy<height/2; imgy++){
	    		buffer[imgx + imgy * width] = color2;
	    	}	    	
	    	
	    	
	    	//max
	    	double val = Math.max(0, maxvalues[valindex] - settings.threshold) * scaleArray[1][valindex];
	    	int ival = (int)val;
	    	if(valindexprev == valindex) {
	    		ival = Math.max(ival, ivalprev);
	    	}
	    	int color = palette[ Math.min(ival, 200)];	    	
	    	
	    	for(int imgy=height/2; imgy<height; imgy++){
	    		buffer[imgx + imgy * width] = color;
	    	}
	    	
	    	ivalprev = ival;
	    	valindexprev = valindex;
	    }
	    
	    
	    Graphics2D g2 = (Graphics2D)image.getGraphics();
	    int x = (int)(settings.layer / scale);
	    int w = (int)(settings.hpage / scale);
	    
	    g2.setStroke(new BasicStroke(10.0f));
	    g2.setColor(Color.WHITE);
	    g2.drawLine(x, 3, x+w, 3);
	    
	    g2.setStroke(new BasicStroke(8.0f));
	    g2.setColor(Color.BLACK);
	    g2.drawLine(x, 3, x+w, 3);

	    g2.dispose();
	    
	    return image;
	}
	
	
}
