package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

@Component
public class Navigator implements ToolProducer {

	@Autowired
	private Model model;
	
	@Autowired
	private Broadcast broadcast;
	
	public Navigator() {
		
	}
	
	public Navigator(Model model) {
		this.model = model;		
	}

	@Override
	public List<Node> getToolNodes() {
		Button backBtn = new Button("", 
				ResourceImageHolder.getImageView("arrow_left_20.png"));
		Button fitBtn = new Button("", 
				ResourceImageHolder.getImageView("fit_20.png"));		
		Button nextBtn = new Button("", 
				ResourceImageHolder.getImageView("arrow_right_20.png"));
		
		backBtn.setTooltip(new Tooltip("Fit previous file to window"));
		fitBtn.setTooltip(new Tooltip("Fit current file to window"));
		nextBtn.setTooltip(new Tooltip("Fit next file to window"));
		
		fitBtn.setOnAction(e -> {
			fitCurrent();
		});

		backBtn.setOnAction(e -> {
			
			fitBack();
		});

		nextBtn.setOnAction(e -> {
			
			fitNext();
		});
		
		return Arrays.asList(backBtn, fitBtn, nextBtn);
	}
	

	public void fitNext() {
		int index = model.getSgyFileIndexByTrace(model.getVField().getSelectedTrace());
		
		index = Math.min(model.getFileManager().getFiles().size() - 1, index + 1);
		SgyFile sgyFile = model.getFileManager().getFiles().get(index);  
				
		fitFile(sgyFile);
	}

	public void fitBack() {
		int index = model.getSgyFileIndexByTrace(model.getVField().getSelectedTrace());
		
		index = Math.max(0, index - 1);
		SgyFile sgyFile = model.getFileManager().getFiles().get(index);  
				
		fitFile(sgyFile);
	}

	public void fitCurrent() {
		SgyFile sgyFile = model.getSgyFileByTrace(model.getVField().getSelectedTrace());
		model.chartsZoomOut();
		fitFile(sgyFile);
	}

	private void fitFile(SgyFile sgyFile) {
		if (sgyFile == null) {
			return;
		}
		
		model.getVField().setSelectedTrace(
				(sgyFile.getOffset().getStartTrace() 
				+ sgyFile.getOffset().getFinishTrace()) 
				/ 2);
		
		int maxSamples = sgyFile.getOffset().getMaxSamples();
		int tracesCount = sgyFile.getTraces().size(); 
		
		fit(maxSamples, tracesCount);
		
		broadcast.notifyAll(new WhatChanged(Change.justdraw));
	}

	public void fit(int maxSamples, int tracesCount) {
		double vertScale = (double) model.getVField().getViewDimension().height 
				/ (double) maxSamples;
		double zoom = Math.log(vertScale) / Math.log(ProfileField.ZOOM_A);
		
		model.getVField().setZoom((int) zoom);
		model.getVField().setStartSample(0);
		
		double h = (double) (model.getVField().getViewDimension().width 
				- model.getVField().getLeftRuleRect().width - 20) 
				/ ((double) tracesCount);
		
		double realAspect = h / model.getVField().getVScale();

		model.getVField().setAspectReal(realAspect);
		
		
	}

	public void fitFull() {		
		model.getVField().setSelectedTrace(model.getTracesCount() / 2);
		
		int maxSamples = model.getMaxHeightInSamples();

		fit(maxSamples * 2, model.getTracesCount());
	}
}
