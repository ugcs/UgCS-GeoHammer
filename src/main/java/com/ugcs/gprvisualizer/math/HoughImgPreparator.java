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
		if (smp < workingRect.getSmpFrom()) {
			return;
		}
		
		if (xfd1 <= showIndex && xfd2 >= showIndex 
			//|| xfd2 <= showIndex && xfd1 >= showIndex
			) {
			
			//s = "" + edge;
		} else {
			//not fit selected Hyperbola, paint it
			//s = " ";
			int index = tr - workingRect.getTraceFrom() 
					+ width * (smp - workingRect.getSmpFrom());
			
			if (xfd2 < showIndex && xfd2 > showIndex - HoughDiscretizer.OUTSIDE_STEPS
				|| xfd1 > showIndex && xfd1 < showIndex + HoughDiscretizer.INSIDE_STEPS) {
				
				buffer[index] = 0x99705050;
			} else {
				buffer[index] = 0xAA505070;
			}
		}
		
	}
	
	public BufferedImage getImage() {
		return img;
	}

}
