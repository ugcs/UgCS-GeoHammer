package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.FoundTracesLayer;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.SatelliteMap;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.draw.Work;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.KmlSaver;
import com.ugcs.gprvisualizer.utils.TiffImagingCreation;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

@Component
public class MapView extends Work implements SmthChangeListener {
	
	@Autowired
	private TraceCutter traceCutter;
	
	@Autowired
	private Model model;
	
	@Autowired
	private Status status;

	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private SatelliteMap satelliteMap;

	@Autowired
	private RadarMap radarMap;	

	GpsTrack gpsTrackMap;
	
	private ToolBar toolBar = new ToolBar();
	private Dimension windowSize = new Dimension();
	
	
	public MapView() {
		super();		
	}
	
	protected RepaintListener listener = new RepaintListener() {
		@Override
		public void repaint() {
			repaintEvent();
		}
	};
	
	@PostConstruct
	public void init() {
		
		//radarMap = new RadarMap(windowSize, model, listener);
		radarMap.setDimension(windowSize);
		radarMap.setRepaintListener(listener);
		
		satelliteMap.setDimension(windowSize);
		satelliteMap.setRepaintListener(listener);

		
		gpsTrackMap = new GpsTrack(windowSize, model, listener);
		
		getLayers().add(satelliteMap);
		getLayers().add(radarMap);
		getLayers().add(gpsTrackMap);
		getLayers().add(new FoundTracesLayer(model));
		
		//TODO: bad
		traceCutter.setListener(listener);		
		getLayers().add(traceCutter);

		initImageView();
		
		//AppContext.smthListener.add(this);
	}
	
	public void somethingChanged(WhatChanged changed) {
		super.somethingChanged(changed);
		
		if (changed.isFileopened()) {
			toolBar.setDisable(!model.isActive() || !isGpsPresent());
			repaintEvent();
		}
		
	}
	
	protected boolean isGpsPresent() {
		
		return model.getField().isActive();
	}
		
	private void initImageView() {
		//ZOOM
		imageView.setOnScroll(event -> {
			
			if (!isGpsPresent()) {
				return;
			}
			
			Point2D p = getLocalCoords(event.getSceneX(), event.getSceneY());
			LatLon ll = model.getField().screenTolatLon(p);
			
	    	int zoom = model.getField().getZoom() + (event.getDeltaY() > 0 ? 1 : -1);
			model.getField().setZoom(zoom);
	    	
	    	Point2D p2 = model.getField().latLonToScreen(ll);
	    	Point2D pdist = new Point2D.Double(p2.getX() - p.getX(), p2.getY() - p.getY());
	    	
	    	LatLon sceneCenter = model.getField().screenTolatLon(pdist);			
			model.getField().setSceneCenter(sceneCenter);
	    	
			broadcast.notifyAll(new WhatChanged(Change.mapzoom));
	    });		
	
		imageView.setOnMouseClicked(mouseClickHandler);
		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);
		
		imageView.addEventFilter(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		    	
		    	imageView.startFullDrag();
		    }
		});		
		imageView.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		imageView.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, 
				new EventHandler<MouseDragEvent>() {
			@Override
			public void handle(MouseDragEvent event) {

				event.consume();
			}
		});
	}
	
	protected EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
			if (!isGpsPresent()) {
				return;
			}
        	
        	
        	if (event.getClickCount() == 2) {
        		
        		Point2D p = getLocalCoords(event);
        		
        		LatLon ll = model.getField().screenTolatLon(p);
        		
        		int traceIndex = findNearestTraceIndex(
        				model.getFileManager().getTraces(), ll);
        		
        		model.getVField().setSelectedTrace(traceIndex);
        		
        		ProfileView.createTempPlace(model, traceIndex);
        	}
        }

		private int findNearestTraceIndex(List<Trace> traces, LatLon ll) {
			
			int resultIndex = 0;
			double mindst = ll.getDistance(traces.get(0).getLatLon());
			
			for (int i = 0; i < traces.size(); i++) {
				
				double dst = ll.getDistance(traces.get(i).getLatLon());
				if (dst < mindst) {
					resultIndex = i;
					
					mindst = dst;
				}
				
			}
			
			return resultIndex;
		}

	};
	
	
	public Node getCenter() {
		
		toolBar.setDisable(true);
		toolBar.getItems().addAll(traceCutter.getToolNodes2());
		
		toolBar.getItems().add(
				CommandRegistry.createButton("",
						ResourceImageHolder.getImageView("geotiff.png"),
						"export map to GeoTIFF image",
						new EventHandler<ActionEvent>() {
							
							@Override
							public void handle(ActionEvent event) {
								
								new TiffImageExport(model, radarMap).execute();
								
							}
						}));
		
		
		toolBar.getItems().add(CommandRegistry.createButton("", 
				ResourceImageHolder.getImageView("kml.png"), 
				"export marks to KML", 
				new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				FileChooser chooser = new FileChooser();
				
				chooser.setTitle("Save kml file");
				chooser.setInitialFileName("geohammer.kml");
				
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("KML files (*.kml)", "*.kml");
				chooser.getExtensionFilters().add(extFilter);
				
				File tiffFile = chooser.showSaveDialog(AppContext.stage); 

				if (tiffFile == null) {
					return;
				}

				try {
					new KmlSaver(model).save(tiffFile);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBoxHelper.showError(
							"Error", "Can`t save file");

				}
			}
		}));
				
		toolBar.getItems().addAll(getToolNodes());
		
		Pane sp1 = new Pane();
		
		ChangeListener<Number> sp1SizeListener = (observable, oldValue, newValue) -> {
			this.setSize((int) (sp1.getWidth()), (int) (sp1.getHeight()));
		};
		
		
		Node n1 = imageView;
		sp1.getChildren().add(n1);
		sp1.getChildren().add(toolBar);
		
		sp1.widthProperty().addListener(sp1SizeListener);
		sp1.heightProperty().addListener(sp1SizeListener);
		
		return sp1;
	}

	public List<Node> getRight() {
		return radarMap.getControlNodes();
	}
		
	public List<Node> getToolNodes() {
		
		List<Node> lst = new ArrayList<>();
		
		for (Layer layer : getLayers()) {
			List<Node> l = layer.getToolNodes();
			if (!l.isEmpty()) {				
				lst.addAll(l);
			}
		}
		
		return lst;
	}

	
	private static final String NO_GPS_TEXT = "There are no coordinates in files";
	
	protected void repaintEvent() {
		if (isGpsPresent()) {
		
			img = draw(windowSize.width, windowSize.height);
			
		} else {
			
			BufferedImage noGpsImg = new BufferedImage(windowSize.width, windowSize.height, BufferedImage.TYPE_INT_RGB);
			
			Graphics2D g2 = (Graphics2D)noGpsImg.getGraphics();
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(0, 0, windowSize.width, windowSize.height);
			
			g2.setColor(Color.DARK_GRAY);
			
	        FontMetrics fm = g2.getFontMetrics();
	        Rectangle2D rect = fm.getStringBounds(NO_GPS_TEXT, g2);
			
			int x = (int) ((windowSize.width - rect.getWidth()) / 2);
			int y = windowSize.height / 2;
			
			g2.drawString(NO_GPS_TEXT, x, y);
			
			img = noGpsImg;
			
		}
		
		updateWindow();
	}

	//@Override
	public void show() {

		repaintEvent();
	}


	public void setSize(int width, int height) {
		
		windowSize.setSize(width, height);
		
		//repaintEvent();
		
		broadcast.notifyAll(new WhatChanged(Change.windowresized));
		
	}

	
	
}
