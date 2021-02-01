package com.ugcs.gprvisualizer.draw;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.gpr.Model;

public class ThrQueue {
	
	
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	Model model;
	BufferedImage frontImg;
		
	BufferedImage backImg;

	private Dimension windowSize;
	
	ThrFront actual;
	
	public ThrFront getFront() {
		return actual;
	}
	
	public ThrQueue(Model model) {
		this.model = model;
	}
	
	void add() {
		executor.submit(getWorker());
	}
	
	private Runnable getWorker() {
		
		return new Thread() {
			public void run() {
				try {
					if (executor.getQueue().size() > 0) {
						return;
					}
				
					actualizeBackImg();
							
					MapField field = new MapField(model.getField());
					
					///
					
					draw(backImg, field);
					
					
					///
					
					BufferedImage img = backImg;
					backImg = frontImg;
					frontImg = img;
					
					if (img == null) {
						backImg = null;
						frontImg = null;
						
						clear();
						return;
					}
					actual = new ThrFront(img, field);
					ready();
								
						
				} catch (Exception e) {
					e.printStackTrace();
				}			
			}

			
		};
	}

	protected void draw(BufferedImage backImg, MapField field) {
		Sout.p("start d");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		Sout.p("finish d");
				
	}		
	
	public void ready() {
		Sout.p("ready");
	}

	protected void actualizeBackImg() {
		if (backImg != null 
				&& backImg.getWidth() == windowSize.width 
				&& backImg.getHeight() == windowSize.height) {
		
		} else {
			backImg = new BufferedImage(windowSize.width, windowSize.height, BufferedImage.TYPE_INT_ARGB);
		}
		
		
		if (backImg != null) {
			Graphics2D g2 = (Graphics2D) backImg.getGraphics();
		
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
		    g2.setComposite(composite);
		    g2.setColor(new Color(0, 0, 0, 0));
		    
			g2.fillRect(0, 0, backImg.getWidth(), backImg.getHeight());
			
			
			//g2.setColor(new Color(0,  255, 0, 255));			
			//g2.fillRect(300, 300, 100, 100);
			
			g2.dispose();
		}
	}
	
	public void clear() {
		
		actual = null;
		
	}
	
	public void drawImgOnChangedField(Graphics2D g2, MapField currentField, ThrFront front) {
		if (front == null) {			
			return;
		}
		
		Point2D offst = currentField.latLonToScreen(front.getField().getSceneCenter());
		
		double scale = Math.pow(2, currentField.getZoom() - front.getField().getZoom());
		BufferedImage tmpImg = front.getImg();
		g2.drawImage(tmpImg, 
			(int) (offst.getX() - tmpImg.getWidth() / 2 * scale), 
			(int) (offst.getY() - tmpImg.getHeight() / 2 * scale),
			(int) (tmpImg.getWidth() * scale),
			(int) (tmpImg.getHeight() * scale),
			null);
	}
	
	public void setWindowSize(Dimension windowSize) {
		this.windowSize = windowSize;
	}
}
