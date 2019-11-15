package com.ugcs.gprvisualizer.app;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.FoundTracesLayer;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.TraceCutter;
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
	
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private Stage stage;
	private int width;
	private int height;
	
	public LayersWindowBuilder(Model model) {
		super(model);
		
		
		getLayers().add(new SatelliteMap(model, listener));
		getLayers().add(new RadarMap(model, listener));
		getLayers().add(new GpsTrack(model, listener));		
		getLayers().add(new FoundTracesLayer(model));
		getLayers().add(new TraceCutter(model, listener));

		initImageView();
	}
	
	public Stage getStage() {
		return stage;
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
	    	
	    	WhatChanged sc = new WhatChanged();
        	sc.setZoom(true);
			somethingChanged(sc);
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
            	System.out.println("drag release");
            	
            	WhatChanged wc = new WhatChanged();
            	wc.setMapscroll(true);
            	somethingChanged(wc);
            	
            	event.consume();
            }
		});		
	}
	
	protected void repaintEvent() {
		img = draw(width, height);
		
		updateWindow();
	}

	
	private void updateWindow() {
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

	EventHandler mousePressHandler = new EventHandler<MouseEvent>() {
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

	EventHandler mouseReleaseHandler = new EventHandler<MouseEvent>() {
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
	
	EventHandler mouseMoveHandler = new EventHandler<MouseEvent>() {
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
	private Point2D getLocalCoords(double x, double y) {
		javafx.geometry.Point2D sceneCoords  = new javafx.geometry.Point2D(x, y);
    	javafx.geometry.Point2D imgCoord = imageView.sceneToLocal(sceneCoords );        	
    	Point2D p = new Point2D.Double(
    			imgCoord.getX() - imageView.getBoundsInLocal().getWidth()/2, 
    			imgCoord.getY() - imageView.getBoundsInLocal().getHeight()/2);
		return p;
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

	@Override
	public void show(int width, int height) {
		if(this.width != width || this.height != height) {
			
		}
		
		this.width = width;		
		this.height = height;

		repaintEvent();
	}

	public void somethingChanged(WhatChanged changed) {
		System.out.println("somethingChanged lwb");
		for (Layer l : getLayers()) {
			l.somethingChanged(changed);
		}

		if(changed.isJustdraw()) {
			repaintEvent();
		}
		
	}


	
}
