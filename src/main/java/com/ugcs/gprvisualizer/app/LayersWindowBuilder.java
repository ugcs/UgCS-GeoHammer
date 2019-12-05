package com.ugcs.gprvisualizer.app;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.FoundTracesLayer;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.TraceCutter;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SatelliteMap;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.draw.Work;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LayersWindowBuilder extends Work implements SmthChangeListener, ModeFactory {
	
	
	public LayersWindowBuilder(Model model) {
		super(model);
		
		
		getLayers().add(new SatelliteMap(model, listener));
		getLayers().add(new RadarMap(model, listener));
		getLayers().add(new GpsTrack(model, listener));
		getLayers().add(new FoundTracesLayer(model));
		getLayers().add(new TraceCutter(model, listener));

		initImageView();
	}
	
		
	private void initImageView() {
		//ZOOM
		imageView.setOnScroll(event -> {
			Point2D p = getLocalCoords(event.getSceneX(), event.getSceneY());
			LatLon ll = model.getField().screenTolatLon(p);
			
	    	model.getField().setZoom( model.getField().getZoom() + (event.getDeltaY() > 0 ? 1 : -1 ) );
	    	
	    	Point2D p2 = model.getField().latLonToScreen(ll);
	    	Point2D pdist = new Point2D.Double(p2.getX()-p.getX(), p2.getY()-p.getY());
	    	
	    	LatLon sceneCenter = model.getField().screenTolatLon(pdist);			
			model.getField().setSceneCenter(sceneCenter);
	    	
			AppContext.notifyAll(new WhatChanged(Change.mapzoom));
	    } );		
	
		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);
		//imageView.setOnMouseMoved(mouseMoveHandler);

		
		imageView.addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		    	
		    	imageView.startFullDrag();
		    }
		});		
		imageView.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		imageView.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
            	
            	
            	AppContext.notifyAll(new WhatChanged(Change.mapscroll));
            	
            	event.consume();
            }
		});		
	}
	
	@Override
	public Node getCenter() {
		
		return imageView;
	}

	@Override
	public List<Node> getRight() {
		
		List<Node> lst = new ArrayList<>();
		
		for(Layer layer : getLayers()) {
			List<Node> l = layer.getToolNodes();
			if(!l.isEmpty()) {
				
				lst.addAll(l);
			}
		}
		
		return lst;
	}

	
	protected void repaintEvent() {
		img = draw(width, height);
		
		updateWindow();
	}

	@Override
	public void show() {

		repaintEvent();
	}


	public void setSize(int width, int height) {
		this.width = width; 
		this.height = height;
		
		repaintEvent();
	}

	
	
}
