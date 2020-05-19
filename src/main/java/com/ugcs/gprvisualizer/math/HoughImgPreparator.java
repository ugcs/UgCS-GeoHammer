package com.ugcs.gprvisualizer.math;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.ugcs.gprvisualizer.math.HoughScanPinncaleAnalizer.StubPrepator;

public class HoughImgPreparator extends StubPrepator {

	private WorkingRect workingRect;
	private int showIndex;
	
	private int width;
	private int height;
	private BufferedImage img;
	private int[] buffer;
	
	public HoughImgPreparator(WorkingRect workingRect, int showIndex) {
		
		this.workingRect = workingRect;
		this.showIndex = showIndex;

		this.width = workingRect.getTraceTo() - workingRect.getTraceFrom() + 1;
		this.height = workingRect.getSmpTo() - workingRect.getSmpFrom() + 1;

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		buffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();		
	}
	
	public void mark(int tr, int smp, int xfd1, int xfd2) {
		
		if (xfd1 <= showIndex && xfd2 >= showIndex 
			|| xfd2 <= showIndex && xfd1 >= showIndex) {
			
			//s = "" + edge;
		} else {
			//not fit selected Hyperbola, paint it
			//s = " ";
			int index = tr - workingRect.getTraceFrom() 
					+ width * (smp - workingRect.getSmpFrom());
			buffer[index] = 0xDD505070;
		}
		
	}
	
	public BufferedImage getImage() {
		return img;
	}

}
