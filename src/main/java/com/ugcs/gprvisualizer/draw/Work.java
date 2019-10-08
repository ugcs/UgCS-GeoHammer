package com.ugcs.gprvisualizer.draw;

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
	
	protected BufferedImage draw() {
		int width = model.getSettings().width;
		int height = model.getSettings().height;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = (Graphics2D)bi.getGraphics();
		for (Layer l : getLayers()) {
			l.draw(g2);
		}
		
		return bi;
	}
	
	public Work(Model model) {
		this.model = model;
		

	}

	public void somethingChanged(WhatChanged changed) {
		for (Layer l : getLayers()) {
			l.somethingChanged(changed);
		}
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

}
