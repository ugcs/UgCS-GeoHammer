package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.text.StyleContext.SmallAttributeSet;

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
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LayersWindowBuilder extends Work{
	
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private int[] palette = new PaletteBuilder().build();
	private Stage stage;
	private Loader loader;
	
	public LayersWindowBuilder(Model model) {
		super(model);
		
		loader = new Loader(model, listener);
		
		stage = new Stage();
		stage.setTitle("layers");
		stage.setScene(build());		
		
		
		getLayers().add(new SatelliteMap(model, listener));
		//getLayers().add(new SatelliteMap2(model, listener));
		getLayers().add(new RadarMap(model, listener));
		getLayers().add(new GpsTrack(model, listener));
		
		getLayers().add(new TraceCutter(model, listener));
		// layers.add(new AuxControl());
		
		
		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);
		//imageView.setOnMouseMoved(mouseMoveHandler);
		
		//img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
		//imageView.setImage(SwingFXUtils.toFXImage(img, null));
		
	    imageView.setVisible(true);
	    
	}
	
	EventHandler mousePressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	System.out.println(event.getX() + " " + event.getY());
        	
        	Point p = new Point((int)event.getX(), (int)event.getY());
        	
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mousePressed(event)) {
        			return;
        		}        		
        	}
        }
	};

	EventHandler mouseReleaseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mouseRelease(event)) {
        			return;
        		}        		
        	}
        	
        }
	};
	
	EventHandler mouseMoveHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	System.out.println("lwb mouse move " );
        	for(int i = getLayers().size()-1; i>=0; i--) {
        		Layer layer = getLayers().get(i);
        		
        		if(layer.mouseMove(event)) {
        			return;
        		}        		
        	}
        	
        	
        }
	};

	public Stage getStage() {
		return stage;
	}
		
	public Scene build() {
		
		BorderPane bPane = new BorderPane();   
		bPane.setCenter(imageView);
		
		bPane.setOnDragOver(loader.getDragHandler());		
		bPane.setOnDragDropped(loader.getDropHandler());
		
		bPane.setOnScroll(event -> { 
	    	model.getField().setZoom( model.getField().getZoom() + (event.getDeltaY() > 0 ? 1 : -1 ) );
	    	
	    	WhatChanged sc = new WhatChanged();
        	sc.setZoom(true);
			somethingChanged(sc);
	    } );		
		
		
		Scene scene = new Scene(bPane, 1024, 768);

		scene.addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		    	System.out.println("DRAG_DETECTED");	
		        scene.startFullDrag();
		    }
		});		

		imageView.setOnMouseDragEntered(mouseMoveHandler);		
		
		
		return scene;
	}
	
	protected void repaintEvent() {
		System.out.println("repaintEvent()");
		img = draw();
		
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


}
