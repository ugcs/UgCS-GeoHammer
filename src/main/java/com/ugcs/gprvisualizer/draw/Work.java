package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

public class Work {

	private List<Layer> layers = new ArrayList<>();
	protected Model model;
	
	protected RepaintListener listener = new RepaintListener() {

		@Override
		public void repaint() {
			repaintEvent();
		}
	};
	
	protected void repaintEvent() {
		
	}
	
	protected BufferedImage draw(int width,	int height) {
		if(width <= 0 || height <= 0) {
			return null;
		}
		
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2 = (Graphics2D)bi.getGraphics();
		g2.setPaint ( Color.DARK_GRAY );
		g2.fillRect ( 0, 0, bi.getWidth(), bi.getHeight() );		
		
		
		
		g2.translate(width/2, height/2);
		
		for (Layer l : getLayers()) {
			l.draw(g2);
		}
		
		return bi;
	}
	
	public Work(Model model) {
		this.model = model;
		

	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

}
