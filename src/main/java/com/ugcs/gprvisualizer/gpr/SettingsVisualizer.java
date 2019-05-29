package com.ugcs.gprvisualizer.gpr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SettingsVisualizer {
	
	private static int[] palette = new int[555000];
	
	static{
		for(int i=0; i< palette.length; i++){
			
			double t= ((double)i) / 3750;
			
			int r = ((int)((Math.cos(t*1.50)+1)/2 * 255.0 ) ) & 0xff;
			int g = ((int)((Math.cos(t*1.23)+1)/2 * 255.0 ) ) & 0xff;
			int b = ((int)((Math.cos(t*1.00)+1)/2 * 255.0 ) ) & 0xff;
			
			palette[i] = r + (g << 8) + (b << 16);
		}		
	}
	
	double[] scaleArray;
	
	private void prepareScaleArray(Settings settings) {
		scaleArray = new double[settings.maxsamples];
		
		for(int i=0; i< scaleArray.length; i++) {
			
			scaleArray[i] = (settings.topscale + (settings.bottomscale - settings.topscale) * i / scaleArray.length) / 100.0 * settings.cutscale / 100.0;
			
		}
	}
	
	
//	public BufferedImage draw(Settings settings, CoordinateManager data) {
//		int width = 340;
//		int height = 300;
//		int topmargin = 40;
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		
//		if(data != null && settings.selectedScanIndex > 0) {
//			int middle = data.getScans().size()/2;
//			
//			prepareScaleArray(settings);
//			///
//		    int[] buffer = ((DataBufferInt)img.getRaster().getDataBuffer()).getData() ;	    
//		    
//		    for(int x=0; x<width; x++){
//		    	for(int y=0; y<height; y++){
//		    		
//		    		int ry = settings.layer - topmargin + y/2;
//		    		int rx = (x - width/2) / 2;
//		    		//int ry = y;
//		    		if(ry < 0 || ry >= data.getScans().get(middle).values.length) {
//		    			buffer[x + y * width] = 0;
//		    			continue;
//		    		}
//		    		
//		    		double d = data.getScans().get(settings.selectedScanIndex + rx).values[ry];
//					d = Math.max(0, Math.abs(d) - settings.threshold);
//					double val = d * scaleArray[ry];
//		    		
//					if(val < palette.length) {
//						buffer[x + y * width] = palette[(int)(val)];
//					}
//		    	
//		    	}
//		    }
//		}
//		
//		
//		///
//		
//		Graphics2D g2 = (Graphics2D)img.getGraphics();
//		
//		//g2.setColor(Color.LIGHT_GRAY);
//		//g2.fillRect(0, 0, img.getWidth(), img.getHeight());
//		
//		g2.setColor(Color.GRAY);
//		
//		int top = settings.layer * img.getHeight() / 512;
//		int bottom = (settings.layer + settings.hpage) * img.getHeight() / 512;
//
//		
//		g2.setColor(Color.BLACK);
//		g2.drawLine(0, top,  img.getWidth(), top);
//		g2.drawLine(0, bottom,  img.getWidth(), bottom);
//		
//		
//		g2.setColor(Color.RED);
//		int nullHpos = 100;
//		g2.drawLine(nullHpos, 0,  nullHpos, img.getHeight());
//		
////		int pnt = settings.startscale * img.getHeight() / 512;
////		int ts = nullHpos + (settings.topscale-100);
////		int bs = nullHpos + (settings.bottomscale-100);
////		g2.drawLine(ts, 0,  ts, pnt);
////		g2.drawLine(ts, pnt,  bs, img.getHeight());
//		
//		
//		
//		
//		
//		g2.dispose();
//		
//		return img;
//	}
	

}
