package com.ugcs.gprvisualizer.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.Broadcast;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.RadarMapScan;
import com.ugcs.gprvisualizer.gpr.ArrayBuilder;
import com.ugcs.gprvisualizer.gpr.DblArray;
import com.ugcs.gprvisualizer.gpr.MedianScaleBuilder;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.ScaleArrayBuilder;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.gpr.Settings.RadarMapMode;
import com.ugcs.gprvisualizer.math.ScanProfile;
import com.ugcs.gprvisualizer.ui.AutoGainCheckbox;
import com.ugcs.gprvisualizer.ui.BaseCheckBox;
import com.ugcs.gprvisualizer.ui.BaseSlider;
import com.ugcs.gprvisualizer.ui.GainBottomSlider;
import com.ugcs.gprvisualizer.ui.GainTopSlider;
import com.ugcs.gprvisualizer.ui.RadiusSlider;
import com.ugcs.gprvisualizer.ui.ThresholdSlider;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

@Component
public class RadarMap extends BaseLayer {

	private static final double MIN_CIRCLE_THRESHOLD = 2.0;
	
	@Autowired
	private Model model;
	
	@Autowired
	private Broadcast broadcast;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	private BufferedImage img;
	private LatLon imgLatLon;
	
	private BaseSlider gainTopSlider;
	private BaseSlider gainBottomSlider;
	private BaseSlider thresholdSlider;
	private BaseSlider radiusSlider;
	private BaseCheckBox autoGainCheckbox;

	
	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {

			setActive(showMapButtonAlg.isSelected() || showMapButtonAmp.isSelected());
			vertBox.setDisable(!isActive());
			
			
			if (showMapButtonAlg.isSelected()) {
				model.getSettings().radarMapMode = RadarMapMode.SEARCH;
			}
			if (showMapButtonAmp.isSelected()) {
                model.getSettings().radarMapMode = RadarMapMode.AMPLITUDE;
                model.getSettings().getHyperliveview().setFalse();
			}
			
			if (isActive()) {
				executor.submit(imgRecalcThread);
			} else {
				getRepaintListener().repaint();
			}
			
		}
	};
	
	ToggleGroup group = new ToggleGroup();
	private ToggleButton showMapButtonAmp = 
			new ToggleButton("", ResourceImageHolder.getImageView("light-20.png"));
	
	{
		showMapButtonAmp.setTooltip(new Tooltip("Toggle amplitude map layer"));
		showMapButtonAmp.setSelected(true);
		showMapButtonAmp.setOnAction(showMapListener);
		showMapButtonAmp.setToggleGroup(group);
	}
	
	private ToggleButton showMapButtonAlg = 
			new ToggleButton("", ResourceImageHolder.getImageView("floodlight-20.png"));
	
	{
		showMapButtonAlg.setTooltip(new Tooltip("Toggle algorithm search layer"));
		showMapButtonAlg.setSelected(false);
		showMapButtonAlg.setOnAction(showMapListener);
		showMapButtonAlg.setToggleGroup(group);
	}
	
	public void selectAlgMode() {
		showMapButtonAlg.setSelected(true);
		
		showMapListener.handle(null);
	}
	
	private VBox vertBox = new VBox();
	private ArrayBuilder scaleArrayBuilder;
	private ArrayBuilder autoArrayBuilder;
	
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, 
				Number oldValue, Number newValue) {
			
			executor.submit(imgRecalcThread);
			
			broadcast.notifyAll(new WhatChanged(Change.adjusting));
		}
	};
	
	public boolean isActive() {
		return model.getSettings().isRadarMapVisible;
	}

	public void setActive(boolean active) {
		model.getSettings().isRadarMapVisible = active;
	}
	
	private ChangeListener<Boolean> autoGainListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, 
				Boolean oldValue, Boolean newValue) {
			
			gainBottomSlider.updateUI();
			gainTopSlider.updateUI();
			thresholdSlider.updateUI();
			executor.submit(imgRecalcThread);
		}
	};
	
	
	public RadarMap() {
		super();
	}
	
	@PostConstruct
	public void init() {
		
		
		autoArrayBuilder = new MedianScaleBuilder(model);
		scaleArrayBuilder = new ScaleArrayBuilder(model.getSettings());
		Settings settings = model.getSettings();
		
		gainTopSlider = new GainTopSlider(settings, sliderListener);
		gainBottomSlider = new GainBottomSlider(settings, sliderListener);
		thresholdSlider = new ThresholdSlider(settings, sliderListener);
		radiusSlider = new RadiusSlider(settings, sliderListener);
		
		autoGainCheckbox = new AutoGainCheckbox(settings, autoGainListener);
		
		String cssLayout = "-fx-border-color: gray;\n" 
				+ "-fx-border-insets: 5;\n"
                + "-fx-border-width: 2;\n"
                + "-fx-border-style: solid;\n";
 
		vertBox.setStyle(cssLayout);		
	}
	
	//draw on the map window prepared image
	@Override
	public void draw(Graphics2D g2) {
		
		if (!isActive()) {
			return;
		}
		
		BufferedImage tmpImg = img;
		
		if (tmpImg == null) {
			return;
		}
		
		Point2D offst = model.getField().latLonToScreen(imgLatLon);
		
		g2.drawImage(tmpImg, 
			(int) offst.getX() - tmpImg.getWidth() / 2, 
			(int) offst.getY() - tmpImg.getHeight() / 2, 
			null);
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if (changed.isFileopened() 
				|| changed.isTraceCut() 
				|| changed.isTraceValues() 
				|| changed.isTraceValues()) {
			autoArrayBuilder.clear();
			scaleArrayBuilder.clear();
		}
		
		if (changed.isAdjusting()) {
			//autoArrayBuilder.clear();
			scaleArrayBuilder.clear();
		}
		
		if (changed.isTraceCut() 
				|| changed.isTraceValues() 
				|| changed.isFileopened() 
				|| changed.isZoom() 
				|| changed.isAdjusting() 
				|| changed.isMapscroll() 
				|| changed.isWindowresized()) {
			
			//System.out.println("RadarMap start asinq");
			executor.submit(imgRecalcThread);
		}		
	}

	// prepare image in thread
	private BufferedImage createHiRes() {
		//Sout.p("hires start");
		
		MapField field = new MapField(model.getField());
		
		imgLatLon = field.getSceneCenter();
		
		DblArray da = new DblArray(getDimension().width, getDimension().height);

		int[] palette;
		if (model.getSettings().radarMapMode == RadarMapMode.AMPLITUDE) {
				
			// fill file.amplScan
			commandRegistry.runForFiles(new RadarMapScan(getArrayBuilder()));
			
			palette = DblArray.paletteAmp;
		} else {
			palette = DblArray.paletteAlg;
		}

		drawCircles(field, da);
		
		//Sout.p("hires fin");
		return da.toImg(palette);
	}

	public void drawCircles(MapField field, DblArray da) {
		for (SgyFile file : model.getFileManager().getFiles()) {
			
			ScanProfile profile = getFileScanProfile(file);
			
			List<Trace> traces = file.getTraces();
			if (profile != null) {
				drawFileCircles(field, da, file, profile, traces);
			}
		}
	}

	public ScanProfile getFileScanProfile(SgyFile file) {
		ScanProfile profile;
		if (model.getSettings().radarMapMode == RadarMapMode.AMPLITUDE) {
			profile = file.amplScan;
		} else {
			profile = file.algoScan;
		}
		return profile;
	}

	public void drawFileCircles(MapField field, DblArray da, SgyFile file, 
			ScanProfile profile, List<Trace> traces) {
		
		int radius = model.getSettings().radius;
		int centerX = getDimension().width / 2;
		int centerY = getDimension().height / 2;
		
		for (int i = 0; i < file.size(); i++) {
			Trace trace = traces.get(i);
			
			double alpha = profile.intensity[i];
			int effectRadius = profile.radius != null ? profile.radius[i] : radius;
			
			if (alpha > MIN_CIRCLE_THRESHOLD) {				
			
				Point2D p = field.latLonToScreen(trace.getLatLon());
				
				da.drawCircle(
					(int) p.getX() + centerX, 
					(int) p.getY() + centerY, 
					effectRadius, 
					alpha);
				
			}
		}
	}
	
	Thread imgRecalcThread = new Thread() {
		public void run() {
			try {
				if (!model.getFileManager().isActive()) {
					return;
				}
				
				if (executor.getQueue().size() > 0) {
					return;
				}
			
				img = createHiRes();
				
				
				getRepaintListener().repaint();
							

			} catch (Exception e) {
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
		
		vertBox.getChildren().clear();
		
		vertBox.getChildren().addAll(
			Arrays.asList(
				//depthSlider.produce(),
				//depthWindowSlider.produce(),
				autoGainCheckbox.produce(),
				gainTopSlider.produce(),
				gainBottomSlider.produce(),
				thresholdSlider.produce(),
				radiusSlider.produce()
			));

		return Arrays.asList(vertBox);
	}
	
	@Override
	public List<Node> getToolNodes() {

		return Arrays.asList(
			showMapButtonAmp, showMapButtonAlg);	
		
	}

	private ArrayBuilder getArrayBuilder() {
		if (model.getSettings().autogain) {
			return autoArrayBuilder;
		} else {
			return scaleArrayBuilder;
		}

	}
	
}
