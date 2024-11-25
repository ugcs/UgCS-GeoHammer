package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

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

	//@Autowired
	private final Model model;
	
	//@Autowired
	private final Broadcast broadcast;
	
	public Navigator(Model model, Broadcast broadcast) {
		this.model = model;
		this.broadcast = broadcast;
	}

	@Override
	public List<Node> getToolNodes() {
		Button backBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ARROW_LEFT, new Button());
		//new Button("", ResourceImageHolder.getImageView("arrow_left_20.png"));
		Button fitBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.FIT, new Button());
		//new Button("", ResourceImageHolder.getImageView("fit_20.png"));		
		Button nextBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ARROW_RIGHT, new Button());
		//new Button("", ResourceImageHolder.getImageView("arrow_right_20.png"));
		
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
		int index = model.getSgyFileIndexByTrace(model.getProfileField().getMiddleTrace());
		
		index = Math.min(model.getFileManager().getGprFiles().size() - 1, index + 1);
		SgyFile sgyFile = model.getFileManager().getGprFiles().get(index);  
				
		fitFile(sgyFile);
	}

	public void fitBack() {
		int index = model.getSgyFileIndexByTrace(model.getProfileField().getMiddleTrace());
		
		index = Math.max(0, index - 1);
		SgyFile sgyFile = model.getFileManager().getGprFiles().get(index);  
				
		fitFile(sgyFile);
	}

	public void fitCurrent() {
		SgyFile sgyFile = model.getSgyFileByTrace(model.getProfileField().getMiddleTrace());
		model.chartsZoomOut();
		fitFile(sgyFile);
	}

	private void fitFile(SgyFile sgyFile) {
		if (sgyFile == null) {
			return;
		}
		
		model.getProfileField().setMiddleTrace(
				(sgyFile.getOffset().getStartTrace() 
				+ sgyFile.getOffset().getFinishTrace()) 
				/ 2);
		
		int maxSamples = sgyFile.getOffset().getMaxSamples();
		int tracesCount = sgyFile.getTraces().size(); 
		
		model.getProfileField().fit(maxSamples, tracesCount);
		
		broadcast.notifyAll(new WhatChanged(Change.justdraw));
	}

}
