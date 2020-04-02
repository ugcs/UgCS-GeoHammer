package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class Navigator implements ToolProducer {

	private Model model;
	
	private Button backBtn = new Button("", ResourceImageHolder.getImageView("arrow_left_20.png"));
	private Button fitBtn = new Button("", ResourceImageHolder.getImageView("fit_20.png"));		
	private Button nextBtn = new Button("", ResourceImageHolder.getImageView("arrow_right_20.png"));
	
	
	public Navigator(Model model) {
		this.model = model;
		
		backBtn.setTooltip(new Tooltip("Fit previous SGY file to window"));
		fitBtn.setTooltip(new Tooltip("Fit current SGY file to window"));
		nextBtn.setTooltip(new Tooltip("Fit next SGY file to window"));

		
		fitBtn.setOnAction(e -> {
			
			SgyFile sgyFile = model.getSgyFileByTrace(model.getVField().getSelectedTrace());
			
			fitFile(sgyFile);
		});

		backBtn.setOnAction(e -> {
			
			int index = model.getSgyFileIndexByTrace(model.getVField().getSelectedTrace());
			
			index = Math.max(0, index - 1);
			SgyFile sgyFile = model.getFileManager().getFiles().get(index);  
					
			fitFile(sgyFile);
		});

		nextBtn.setOnAction(e -> {
			
			int index = model.getSgyFileIndexByTrace(model.getVField().getSelectedTrace());
			
			index = Math.min(model.getFileManager().getFiles().size()-1, index + 1);
			SgyFile sgyFile = model.getFileManager().getFiles().get(index);  
					
			fitFile(sgyFile);
		});
		
	}

	private void fitFile(SgyFile sgyFile) {
		if(sgyFile == null) {
			return;
		}
		
		model.getVField().setSelectedTrace((sgyFile.getOffset().getStartTrace()+sgyFile.getOffset().getFinishTrace())/2);
		
		int maxSamples = sgyFile.getOffset().getMaxSamples();
		int tracesCount = sgyFile.getTraces().size(); 
		
		fit(maxSamples, tracesCount);
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
	}

	public void fit(int maxSamples, int tracesCount) {
		double vScale = (double)model.getVField().getViewDimension().height / (double)maxSamples;
		double z = Math.log(vScale) / Math.log(ProfileField.ZOOM_A);
		
		model.getVField().setZoom((int)z);
		model.getVField().setStartSample(0);
		
		double h = (double)(model.getVField().getViewDimension().width - model.getVField().getLeftRuleRect().width - 20) / ((double)tracesCount);
		
		double realAspect = h / model.getVField().getVScale();

		model.getVField().setAspectReal(realAspect);
		
		
	}

	public void fitFull() {
		
		
		model.getVField().setSelectedTrace(model.getTracesCount()/2);
		
		int maxSamples = model.getMaxHeightInSamples();

		fit(maxSamples*2, model.getTracesCount());
	}
	
	@Override
	public List<Node> getToolNodes() {
	
		return Arrays.asList(backBtn, fitBtn, nextBtn);
	}

	

}
