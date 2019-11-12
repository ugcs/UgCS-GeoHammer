package com.ugcs.gprvisualizer.app;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.TraceCutter;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SatelliteMap;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.draw.Work;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
		getLayers().add(new TraceCutter(model, listener));

		initImageView();
	}
	
	public Stage getStage() {
		return stage;
	}
		
	private void initImageView() {
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
			System.out.println(" ll ");
			List<Node> l = layer.getToolNodes();
			if(!l.isEmpty()) {
				
				lst.addAll(l);
			}
		}
		
		lst.addAll(AppContext.levelFilter.getToolNodes());
		
		return lst;//Arrays.asList();
	}

	@Override
	public void show(int width, int height) {
		this.width = width; 
		this.height = height;

		repaintEvent();
	}

	
}
