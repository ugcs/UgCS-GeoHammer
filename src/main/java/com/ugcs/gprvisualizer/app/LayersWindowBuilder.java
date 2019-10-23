package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.text.StyleContext.SmallAttributeSet;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.TraceCutter;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.SatelliteMap;
import com.ugcs.gprvisualizer.draw.SatelliteMap2;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.draw.Work;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PaletteBuilder;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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

public class LayersWindowBuilder extends Work{
	
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private Stage stage;
	private BorderPane bPane;
	private Loader loader;
	private Scene scene;
	
	public LayersWindowBuilder(Model model) {
		super(model);
		
		loader = new Loader(model, listener);
		
		getLayers().add(new SatelliteMap(model, listener));
		getLayers().add(new RadarMap(model, listener));
		getLayers().add(new GpsTrack(model, listener));		
		getLayers().add(new TraceCutter(model, listener));
		// layers.add(new AuxControl());
		
	    //imageView.setVisible(true);

		stage = new Stage();
		stage.setTitle("layers");
		stage.setScene(build());
		
//		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
//			System.out.println("Height: " + stage.getHeight() + " Width: " + stage.getWidth());
//			listener.repaint();
//		};
//
//	    stage.widthProperty().addListener(stageSizeListener);
//	    stage.heightProperty().addListener(stageSizeListener); 
		
	}
	
	public Stage getStage() {
		return stage;
	}
		
	public Scene build() {
		
		bPane = new BorderPane();		
		
		bPane.setTop(getToolBar());
		
		bPane.setCenter(imageView);
		
		bPane.setOnDragOver(loader.getDragHandler());		
		bPane.setOnDragDropped(loader.getDropHandler());
		
		bPane.setOnScroll(event -> {
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
		
		
		scene = new Scene(bPane, 1024, 768);
		
	
	
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

		//imageView.setOnMouseDragEntered(mouseMoveHandler);
		
		
		return scene;
	}
	
	protected void repaintEvent() {
		img = draw(
				(int)bPane.getWidth(), 
				(int)bPane.getHeight());
		
		updateWindow();
	}

	
	private void updateWindow() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
			    Image i = SwingFXUtils.toFXImage(img, null);
			    imageView.setImage(i);
            }
          });
	}


	private Node getToolBar() {
		ToolBar toolBar = new ToolBar();
		//toolBar.setPrefWidth(400);
		//toolBar.setPrefHeight(60);
		

		for(Layer layer : getLayers()) {
			System.out.println(" ll ");
			List<Node> l = layer.getToolNodes();
			if(!l.isEmpty()) {
				System.out.println(" ll2 ");
				toolBar.getItems().addAll(l);
			}
		}
		
		return toolBar;
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
	
}
