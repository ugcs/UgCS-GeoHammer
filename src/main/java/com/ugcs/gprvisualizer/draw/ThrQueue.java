package com.ugcs.gprvisualizer.draw;

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
	MapField frontImgField;
	
	BufferedImage backImg;
	int width;
	int height;
	
	boolean actual = false;
	
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
					frontImg = backImg;
					backImg = img;
					frontImgField = field;
					
					actual = true;
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

	private void actualizeBackImg() {
		if (backImg != null 
				&& backImg.getWidth() == width 
				&& backImg.getHeight() == height) {
		
		} else {
			backImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	public BufferedImage getFrontImg() {
		return frontImg;
	}
	
	public MapField getFrontImgField() {
		
		return actual ? frontImgField : null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Model model = new Model();
		
		ThrQueue q = new  ThrQueue(model);
		
		q.width = 1000;
		q.height = 800;
		
		q.add();
		q.add();
		
		Thread.sleep(50);
		
		q.add();
		q.add();
		
		Thread.sleep(500);
		
		q.add();
		q.add();
		
	}
	
	public void clear() {
		
		actual = false;
		
	}
	
}
