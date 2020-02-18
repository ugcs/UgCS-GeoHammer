package com.ugcs.gprvisualizer.app;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MapView extends Work implements SmthChangeListener, ModeFactory {
	
	TraceCutter traceCutter;
	ToolBar toolBar = new ToolBar();
	
	RadarMap radarMap;
	Dimension windowSize = new Dimension();
	
	public MapView(Model model) {
		super(model);
		
		radarMap = new RadarMap(windowSize, model, listener);
		
		getLayers().add(new SatelliteMap(windowSize, model, listener));
		getLayers().add(radarMap);
		getLayers().add(new GpsTrack(windowSize, model, listener));
		getLayers().add(new FoundTracesLayer(model));
		
		traceCutter = new TraceCutter(model, listener);
		getLayers().add(traceCutter);

		initImageView();
	}
	
	public void somethingChanged(WhatChanged changed) {
		super.somethingChanged(changed);
		
		if(changed.isFileopened()) {
			toolBar.setDisable(!model.isActive());
		}
		
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
            	
            	event.consume();
            }
		});		
	}
	
	@Override
	public Node getCenter() {
		
		//VBox vBox = new VBox();
		
		
		toolBar.setDisable(true);
		toolBar.getItems().addAll(traceCutter.getToolNodes2());
		
		toolBar.getItems().addAll(getToolNodes());
		
		//vBox.getChildren().add(toolBar);
		
		Pane sp1 = new Pane();
		
		ChangeListener<Number> sp1SizeListener = (observable, oldValue, newValue) -> {
			this.setSize((int) (sp1.getWidth()), (int) (sp1.getHeight()));
		};
		
		
		Node n1 = imageView;
		sp1.getChildren().add(n1);
		sp1.getChildren().add(toolBar);
		
		sp1.widthProperty().addListener(sp1SizeListener);
		sp1.heightProperty().addListener(sp1SizeListener);
		
		//vBox.getChildren().add(sp1);
		
		
		return sp1;
	}

	@Override
	public List<Node> getRight() {
		return radarMap.getControlNodes();//Arrays.asList();
	}
		
	public List<Node> getToolNodes() {
		
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
		
		img = draw(windowSize.width, windowSize.height);
		
		updateWindow();
	}

	@Override
	public void show() {

		repaintEvent();
	}


	public void setSize(int width, int height) {
		
		windowSize.setSize(width, height);
		
		//repaintEvent();
		
		AppContext.notifyAll(new WhatChanged(Change.windowresized));
		
	}

	
	
}
