package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Work {

	private List<Layer> layers = new ArrayList<>();
	protected Model model;
	protected ImageView imageView = new ImageView();
	protected BufferedImage img;
	protected int width;
	protected int height;
	
	
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

	protected void updateWindow() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if(img == null) {
            		return;
            	}
			    Image i = SwingFXUtils.toFXImage(img, null);
			    imageView.setImage(i);
            }
          });
	}

	protected EventHandler mousePressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	Point2D p = getLocalCoords(event);
        	
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mousePressed(p)) {
        			return;
        		}        		
        	}
        }

	};

	protected EventHandler mouseReleaseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	Point2D p = getLocalCoords(event);
        	
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mouseRelease(p)) {
        			return;
        		}        		
        	}
        	
        }
	};
	
	protected EventHandler mouseMoveHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	Point2D p = getLocalCoords(event);
        	
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mouseMove(p)) {
        			return;
        		}        		
        	}        	
        	
        }
	};

	private Point2D getLocalCoords(MouseEvent event) {
		
		return getLocalCoords(event.getSceneX(), event.getSceneY());
	
	}
	protected Point2D getLocalCoords(double x, double y) {
		javafx.geometry.Point2D sceneCoords  = new javafx.geometry.Point2D(x, y);
    	javafx.geometry.Point2D imgCoord = imageView.sceneToLocal(sceneCoords );        	
    	Point2D p = new Point2D.Double(
    			imgCoord.getX() - imageView.getBoundsInLocal().getWidth()/2, 
    			imgCoord.getY() - imageView.getBoundsInLocal().getHeight()/2);
		return p;
	}

	public void somethingChanged(WhatChanged changed) {
		if(changed.isWindowresized()) {
			width = model.getSettings().center_box_width;
			height = model.getSettings().center_box_height;
		}

		
		System.out.println("somethingChanged lwb");
		for (Layer l : getLayers()) {
			l.somethingChanged(changed);
		}

		if(changed.isJustdraw()) {
			repaintEvent();
		}
		
	}


	
}
