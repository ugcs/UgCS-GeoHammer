package com.ugcs.gprvisualizer.draw;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.Broadcast;
import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

@Component
public class SatelliteMap extends BaseLayer implements InitializingBean {

	@Autowired
	protected Model model; 
	
	@Autowired
	private Status status;
	
	@Autowired
	private Broadcast broadcast;

	@Autowired
	private Dimension wndSize;

	private LatLon click;
	private ThrQueue recalcQueue;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		recalcQueue = new ThrQueue(model) {
			protected void draw(BufferedImage backImg, MapField field) {
				if (field.getMapProvider() != null) {
					this.backImg = field.getMapProvider().loadimg(field);
				}
			}
			
			public void ready() {
				getRepaintListener().repaint();
			}
			
			protected void actualizeBackImg() {
				
			}
		};
	}	
	
//	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
//		@Override
//		public void handle(ActionEvent event) {
//			setActive(showLayerCheckbox.isSelected());
//			if (isActive()) {
//				q.add();
//			} else {
//				getRepaintListener().repaint();
//			}
//				
//		}
//	};
	
	
	
	RadioMenuItem menuItem1 = new RadioMenuItem("google maps");
	RadioMenuItem menuItem2 = new RadioMenuItem("here.com");
	RadioMenuItem menuItem3 = new RadioMenuItem("turn off");
	
	private MenuButton optionsMenuBtn = new MenuButton("", ResourceImageHolder.getImageView("gmap-20.png"), 
			menuItem1,
			menuItem2,
			menuItem3);
	{
		
		ToggleGroup toggleGroup = new ToggleGroup();
		menuItem1.setToggleGroup(toggleGroup);
		menuItem2.setToggleGroup(toggleGroup);
		menuItem3.setToggleGroup(toggleGroup);
		
		menuItem1.setSelected(true);		
		
		menuItem1.setOnAction(e -> {
			model.getField().setMapProvider(new GoogleMapProvider());
			setActive(model.getField().getMapProvider() != null);
			recalcQueue.clear();
			broadcast.notifyAll(new WhatChanged(Change.mapzoom));		        
		});

		menuItem2.setOnAction(e -> {
			model.getField().setMapProvider(new HereMapProvider());
			setActive(model.getField().getMapProvider() != null);
			recalcQueue.clear();
			broadcast.notifyAll(new WhatChanged(Change.mapzoom));
		});

		menuItem3.setOnAction(e -> {
			model.getField().setMapProvider(null);
			setActive(model.getField().getMapProvider() != null);
			recalcQueue.clear();
			broadcast.notifyAll(new WhatChanged(Change.mapzoom));
		});
		
		///optionsMenuBtn.setStyle("padding-left: 2px; padding-right: 2px");
		
	}
	
//	private ToggleButton showLayerCheckbox = 
//			new ToggleButton("", ResourceImageHolder.getImageView("gmap-20.png"));
//	
//	{
//		//boolean apiExists = StringUtils.isNotBlank(GOOGLE_API_KEY);
//		
//		showLayerCheckbox.setTooltip(new Tooltip("Toggle satellite map layer"));
//		//showLayerCheckbox.setDisable(!apiExists);
//		//showLayerCheckbox.setSelected(apiExists);
//		showLayerCheckbox.setOnAction(showMapListener);
//	}
	
	@Override
	public void draw(Graphics2D g2, MapField currentField) {
		ThrFront front = recalcQueue.getFront();
		
		if (front != null && isActive()) {
			
			recalcQueue.drawImgOnChangedField(g2, currentField, front);
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
				recalcQueue.add();
			} else {
				recalcQueue.clear();
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
		HBox cnt = new HBox(/*showLayerCheckbox,*/ optionsMenuBtn);
		return Arrays.asList(cnt);
	}
	
}
