package com.ugcs.gprvisualizer.math;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;

public class HoughDraw {

	BufferedImage img;
	SgyFile file;
	int startTrace;
	int finishTrace;
	int startSample;
	int finishSample;
	
	public int resedge;
	public int resindex;
	public double res;
	
	public HoughDraw(	
		BufferedImage img,
		SgyFile file,
		int startTrace,
		int finishTrace,
		int startSample,
		int finishSample) {
		
		this.img = img;
		this.file = file;
		this.startTrace = startTrace;
		this.finishTrace = finishTrace;
		this.startSample = startSample;
		this.finishSample = finishSample;		
	}
	
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		
		Rectangle rect = getRect(vField); 
	
//		Sout.p(img.getWidth() + " - " + img.getHeight());
//		Sout.p(" tr tr sm sm  str" + startTrace + " ft " + finishTrace + " ss " + startSample + " fs " + finishSample);
//		Sout.p(" draw in rect  x " + rect.x + " y " + rect.y + " w " + rect.width + " h " + rect.height);
		
		
		g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
		
		g2.setColor(Color.BLACK);
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);
			
		drawText(g2, rect.x+rect.width, rect.y +  0 , "edge: " + resedge);
		drawText(g2, rect.x+rect.width, rect.y + 20 , "indx: " + resindex);
		drawText(g2, rect.x+rect.width, rect.y + 40 , "res : " + String.format("%.2f", res));
	}
	
	public Rectangle getRect(ProfileField vField) {
		
		int gtr_left = file.getOffset().localToGlobal(startTrace);
		int gtr_right = file.getOffset().localToGlobal(finishTrace);
		
		Point lt = vField.traceSampleToScreen(new TraceSample(gtr_left, startSample));
		Point rb = vField.traceSampleToScreen(new TraceSample(gtr_right, finishSample));
		return new Rectangle(lt.x, lt.y, rb.x - lt.x, rb.y - lt.y);
	}
	
	
	
	private static final double MARGIN = 1.0;
	private void drawText(Graphics2D g2, int x, int y, String str) {
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(str, g2);

        rect = new Rectangle2D.Double(rect.getX()-MARGIN-2, rect.getY()-MARGIN, rect.getWidth()+2*MARGIN+3, rect.getHeight()+2*MARGIN);
        
        g2.setColor(Color.BLACK);
        
        g2.fillRoundRect(x+(int)rect.getX(),
        			y + (int)(rect.getY()), //- fm.getAscent() 
                   (int) rect.getWidth(),
                   (int) rect.getHeight(), 
                   5, 5);
        
        g2.setColor(Color.YELLOW.darker());
        g2.drawRoundRect(x+(int)rect.getX(),
    			y  + (int)(rect.getY()), //- fm.getAscent()
                (int) rect.getWidth(),
                (int) rect.getHeight(), 
                5, 5);

        g2.setColor(Color.MAGENTA);
        g2.drawString(str, x, y);		
	}
	
	
}
