package com.ugcs.gprvisualizer.draw;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.Broadcast;
import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.gpr.Model;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

@Component
public class SatelliteMap extends BaseLayer {

	
	
	@Autowired
	protected Model model; 
	
	@Autowired
	private Status status;
	
	@Autowired
	private Broadcast broadcast;

	@Autowired
	private Dimension wndSize;


	private LatLon click;
	
	ThrQueue q;
	
	
	
	@PostConstruct
	public void init() {
		q = new ThrQueue(model) {
			protected void draw(BufferedImage backImg, MapField field) {
				this.backImg = field.getMapProvider().loadimg(field);
			}
			
			public void ready() {
				getRepaintListener().repaint();
			}
			
			protected void actualizeBackImg() {
				
			}
		};
		
	//	q.width = 640*2;
	//	q.height = 640*2;
	}	
	
	
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			setActive(showLayerCheckbox.isSelected());
			if (isActive()) {
				//loadMap();
				q.add();
			} else {
				getRepaintListener().repaint();
			}
				
		}
	};
	
	
	
	RadioMenuItem menuItem1 = new RadioMenuItem("Google Maps");
	RadioMenuItem menuItem2 = new RadioMenuItem("Here.com");
	//MenuItem menuItem3 = new MenuItem("Option 3");
	
	private MenuButton optionsMenuBtn = new MenuButton("", null, 
			//ResourceImageHolder.getImageView("arrow-down-20.png"),
			menuItem1,
			menuItem2);
	{
		
		ToggleGroup toggleGroup = new ToggleGroup();
		menuItem1.setToggleGroup(toggleGroup);
		menuItem2.setToggleGroup(toggleGroup);
		menuItem2.setSelected(true);
		
		menuItem1.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        model.getField().setMapProvider(new GoogleMapProvider());
		        
		        broadcast.notifyAll(new WhatChanged(Change.mapzoom));
		    }
		});
		menuItem2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        model.getField().setMapProvider(new HereMapProvider());
		        broadcast.notifyAll(new WhatChanged(Change.mapzoom));
		    }
		});
		
		///optionsMenuBtn.setStyle("padding-left: 2px; padding-right: 2px");
		
	}
	
	private ToggleButton showLayerCheckbox = 
			new ToggleButton("", ResourceImageHolder.getImageView("gmap-20.png"));
	
	{
		//boolean apiExists = StringUtils.isNotBlank(GOOGLE_API_KEY);
		
		showLayerCheckbox.setTooltip(new Tooltip("Toggle satellite map layer"));
		//showLayerCheckbox.setDisable(!apiExists);
		//showLayerCheckbox.setSelected(apiExists);
		showLayerCheckbox.setOnAction(showMapListener);
	}
	
	public SatelliteMap() {
		super();		
	}
	
	@Override
	public void draw(Graphics2D g2, MapField currentField) {
		ThrFront front = q.getFront();
		
		if (front != null && isActive()) {
			
			q.drawImgOnChangedField(g2, currentField, front);
		}		
		
		if (click != null) {
			Point2D p = currentField.latLonToScreen(click);
			
			g2.drawOval((int) p.getX() - 3, (int) p.getY() - 3, 7, 7);
		}
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		
		if (changed.isFileopened() || changed.isZoom()) {
			
			if (model.isActive()) {
				//loadMap();
				q.add();
			} else {
				q.clear();
			}			
		}		
	}
	
	MapField dragField = null;
	
	@Override
	public boolean mousePressed(Point2D point) {
		
		if (!model.getFileManager().isActive()) {
			return false;
		}
		
		dragField = new MapField(model.getField());
		
		click = model.getField().screenTolatLon(point);
		
		
		status.showProgressText(click.toString());
		
		getRepaintListener().repaint();
		
		return true;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		
		dragField = null;
		click = null;
		
		broadcast.notifyAll(new WhatChanged(Change.mapscroll));
		
		return true;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		
		if (dragField == null) {
			return false;
		}

		LatLon newCenter = dragField.screenTolatLon(point);		
		double lat = dragField.getSceneCenter().getLatDgr() 
				+ click.getLatDgr() - newCenter.getLatDgr();
		double lon = dragField.getSceneCenter().getLonDgr() 
				+ click.getLonDgr() - newCenter.getLonDgr();
		
		model.getField().setSceneCenter(new LatLon(lat, lon));
		
		getRepaintListener().repaint();
		
		return true;
	}

	@Override
	public List<Node> getToolNodes() {
		HBox cnt = new HBox(showLayerCheckbox, optionsMenuBtn);
		return Arrays.asList(cnt);
	}
	
}
