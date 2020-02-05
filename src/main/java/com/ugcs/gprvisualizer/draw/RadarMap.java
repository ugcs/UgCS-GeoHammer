package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.sun.javafx.collections.SetAdapterChange;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.gpr.ArrayBuilder;
import com.ugcs.gprvisualizer.gpr.AutomaticScaleBuilder;
import com.ugcs.gprvisualizer.gpr.DblArray;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.ScaleArrayBuilder;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.ui.AutoGainCheckbox;
import com.ugcs.gprvisualizer.ui.BaseCheckBox;
import com.ugcs.gprvisualizer.ui.BaseSlider;
import com.ugcs.gprvisualizer.ui.DepthSlider;
import com.ugcs.gprvisualizer.ui.DepthWindowSlider;
import com.ugcs.gprvisualizer.ui.GainBottomSlider;
import com.ugcs.gprvisualizer.ui.GainTopSlider;
import com.ugcs.gprvisualizer.ui.LayerVisibilityCheckbox;
import com.ugcs.gprvisualizer.ui.RadiusSlider;
import com.ugcs.gprvisualizer.ui.ThresholdSlider;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class RadarMap extends BaseLayer{

	private RepaintListener listener;
	private Model model;
	private BufferedImage img;
	private LatLon imgLatLon;
	
	private int width = 800;
	private int height = 800;
	
	
	private BaseSlider depthSlider;
	private BaseSlider depthWindowSlider;
	private BaseSlider gainTopSlider;
	private BaseSlider gainBottomSlider;
	private BaseSlider thresholdSlider;
	private BaseSlider radiusSlider;
	private BaseCheckBox autoGainCheckbox;

	
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {

			setActive(showMapButton.isSelected());
			vBox.setVisible(isActive());
			
			if(isActive()) {
				executor.submit(t);
			}else {
				listener.repaint();
			}
			
		}
	};
	
	private ToggleButton showMapButton = new ToggleButton("", ResourceImageHolder.getImageView("light-20.png"));
	{
		showMapButton.setSelected(true);
		showMapButton.setOnAction(showMapListener);
	}
	
	VBox vBox = new VBox();
	//rightBox.setPadding(new Insets(3, 13, 3, 3));

	
	private double[][] scaleArray;
	private ArrayBuilder scaleArrayBuilder;
	private ArrayBuilder autoArrayBuilder;
	
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			
			executor.submit(t);
			
			AppContext.notifyAll(new WhatChanged(Change.adjusting));
		}
	};
	
	public boolean isActive() {
		return model.getSettings().isRadarMapVisible;
	}

	public void setActive(boolean active) {
		model.getSettings().isRadarMapVisible = active;
	}
	
	
//	private ChangeListener<Boolean> showLayerListener = new ChangeListener<Boolean>() {
//		@Override
//		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//			
//			setActive(newValue);
//			vBox.setVisible(isActive());
//			
//			if(isActive()) {
//				executor.submit(t);
//			}else {
//				listener.repaint();
//			}
//		}
//	};
	private ChangeListener<Boolean> autoGainListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			
			gainBottomSlider.updateUI();
			gainTopSlider.updateUI();
			thresholdSlider.updateUI();
			
			//controller.render(RecalculationLevel.BUFFERED_IMAGE);
			executor.submit(t);
		}
	};
	
	
	public RadarMap(Model model, RepaintListener listener) {
		this.listener = listener;
		this.model = model;
		
		autoArrayBuilder = new AutomaticScaleBuilder(model);
		scaleArrayBuilder = new ScaleArrayBuilder(model.getSettings());
		Settings settings = model.getSettings();
		
		depthSlider = new DepthSlider(settings, sliderListener);
		depthWindowSlider = new DepthWindowSlider(settings, sliderListener);
		gainTopSlider = new GainTopSlider(settings, sliderListener);
		gainBottomSlider = new GainBottomSlider(settings, sliderListener);
		thresholdSlider = new ThresholdSlider(settings, sliderListener);
		radiusSlider = new RadiusSlider(settings, sliderListener);
		
		autoGainCheckbox = new AutoGainCheckbox(settings, autoGainListener);
	
		
		
//		new EventHandler<ActionEvent>() {
//		    @Override public void handle(ActionEvent e) {
//		        
//		    	updateBtns();
//		    }
//		});

		
		String cssLayout = "-fx-border-color: gray;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 2;\n" +
                "-fx-border-style: solid;\n";
 
		vBox.setStyle(cssLayout);		
	}
	
	@Override
	public void draw(Graphics2D g2) {
		
		if(!isActive()) {
			return;
		}
		
		BufferedImage _img = img;
		
		//System.out.println(" draw radar" + (_img != null ? "+" : "-"));
		
		if(_img == null) {
			return;
		}
		
		//int width = model.getSettings().width; 
		//int height = model.getSettings().height;
		
		Point2D offst = model.getField().latLonToScreen(imgLatLon);
		
		g2.drawImage(_img, 
			(int)offst.getX() -_img.getWidth()/2, 
			(int)offst.getY() -_img.getHeight()/2, 
			null);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if(changed.isWindowresized()) {
			width = model.getSettings().center_box_width;
			height = model.getSettings().center_box_height;
		}
		
		if(changed.isFileopened() || changed.isZoom() || changed.isAdjusting() || changed.isMapscroll() || changed.isWindowresized()) {
			//System.out.println(" radar start thread");
			executor.submit(t);
		}		
	}
	
	private BufferedImage createHiRes() {
		
		
		DblArray da = new DblArray(width, height);
		
		scaleArray = getArrayBuilder().build();
		
		 
		int start = norm(model.getSettings().layer, 0, model.getMaxHeightInSamples());
		int finish = norm(model.getSettings().layer + model.getSettings().hpage, 0, model.getMaxHeightInSamples());

		for (Trace trace : model.getFileManager().getTraces()) {

			Point2D p = model.getField().latLonToScreen(trace.getLatLon());
			
			double alpha = calcAlpha(trace.getNormValues(), start, finish);
			
			da.drawCircle(
				(int)p.getX() + width/2, 
				(int)p.getY() + height/2, 
				model.getSettings().radius, alpha);
		}
		
		return da.toImg();
	}
	
	private double calcAlpha(float[] values, int start, int finish) {
		double mx = 0;
		double threshold = scaleArray[0][start];
		double factor = scaleArray[1][start];

		for (int i = start; i < finish; i++) {

			mx = Math.max(mx, Math.abs(values[i]));
		}

		double val = Math.max(0, mx - threshold) * factor;

		return Math.max(0, Math.min(val, 200));

	}
	

	private int norm(int i, int min, int max) {

		return Math.min(Math.max(i, min), max - 1);
	}
	
	int r = 5;
	private BufferedImage createLowRes() {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		
		Graphics2D g2 = (Graphics2D)img.getGraphics();
		g2.translate(width/2, height/2);
		g2.setColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
		for(SgyFile sf : model.getFileManager().getFiles()) {
			
			
			drawTrace(g2, sf.getTraces().get(0));
			
			drawTrace(g2, sf.getTraces().get(sf.getTraces().size()-1));
		}
		
		return img;
	}

	private void drawTrace(Graphics2D g2, Trace trace) {
		Point2D p = model.getField().latLonToScreen(trace.getLatLon());
		g2.fillOval((int)p.getX() - r, (int)p.getY()-r, 2*r, 2*r);
	}
	
	
	Thread t = new Thread() {
		public void run() {
			try {
				if(!model.getFileManager().isActive()) {
					return;
				}
				//TODO: show lowres
				//img = createLowRes();
				//imgLatLon = model.getField().getSceneCenter();				
				//listener.repaint();
				
				
				if(executor.getQueue().size() > 0) {
					return;
				}
				
			
				img = createHiRes();
				imgLatLon = model.getField().getSceneCenter();
				
				listener.repaint();
							

			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}

	};

	@Override
	public boolean mousePressed(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public List<Node> getControlNodes() {
		
		vBox.getChildren().clear();
		
		vBox.getChildren().addAll(
			Arrays.asList(
				depthSlider.produce(),
				depthWindowSlider.produce(),
				autoGainCheckbox.produce(),
				gainTopSlider.produce(),
				gainBottomSlider.produce(),
				thresholdSlider.produce(),
				radiusSlider.produce()
			));

		return Arrays.asList(vBox);
	}
	
	@Override
	public List<Node> getToolNodes() {

		
		return Arrays.asList(
			showMapButton);	
		
	}

	private ArrayBuilder getArrayBuilder() {
		if (model.getSettings().autogain) {
			return autoArrayBuilder;
		} else {
			return scaleArrayBuilder;
		}

	}
	
}
