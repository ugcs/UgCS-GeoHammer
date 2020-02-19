package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;

public class FoundTracesLayer implements Layer {

	private Model model;
	//private Image img;// = ImageIO.read(getClass().getClassLoader().getResourceAsStream("shovel.png"));
	private int R = 5;
	private Color pointColor = Color.GREEN;
	
	public FoundTracesLayer(Model model) {
		this.model = model;
		
//		try {
//			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream("shovel-48.png"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public List<Node> getToolNodes() {

		return Arrays.asList();
	}

	@Override
	public void draw(Graphics2D g2) {

		g2.setColor(pointColor);
		
		for (BaseObject bo : model.getAuxElements()) {
			
			bo.drawOnMap(g2, model.getField());
		}

		if(model.getControls() != null) {
			for (BaseObject bo : model.getControls()) {
				
				bo.drawOnMap(g2, model.getField());
			}
		}
		
//		for (Trace trace : model.getFoundTrace()) {
//
//			Point2D p = model.getField().latLonToScreen(trace.getLatLon());
//			
//			Image img = ResourceImageHolder.IMG_SHOVEL;
//			g2.drawImage(img, (int)p.getX() - img.getWidth(null)/2 , (int)p.getY() - img.getHeight(null), null);
//			g2.fillOval((int)p.getX()-R, (int)p.getY()-R/2, R*2, R);
//		}
//		
//		for (AuxElement au : model.getAuxElements()) {
//			
//			au.drawOnMap(g2, model.getField());
//		}

	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {

	}

	@Override
	public boolean mousePressed(Point2D point) {

		for(BaseObject bo : model.getAuxElements()) {
			
			
			if(bo.mousePressHandle(point, model.getField())) {
				
				return true;
			}			
		}
		
		return false;
	}

	@Override
	public boolean mouseRelease(Point2D point) {

		return false;
	}

	@Override
	public boolean mouseMove(Point2D point) {

		return false;
	}

}
