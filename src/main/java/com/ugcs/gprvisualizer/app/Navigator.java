package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class Navigator implements ToolProducer {

	private Model model;
	
	private Button backBtn = new Button("", ResourceImageHolder.getImageView("arrow_left_20.png"));
	private Button fitBtn = new Button("", ResourceImageHolder.getImageView("fit_20.png"));		
	private Button nextBtn = new Button("", ResourceImageHolder.getImageView("arrow_right_20.png"));
	
	
	public Navigator(Model model) {
		this.model = model;
		
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
		
		//int sizeTrace = sgyFile.getOffset().getFinishTrace() - sgyFile.getOffset().getStartTrace();
		
		int maxSamples = sgyFile.getOffset().getMaxSamples();
		
		//screen =  (int)(model.getMaxHeightInSamples() * getVScale()
		
		double vScale = (double)model.getVField().getViewDimension().height / (double)maxSamples;
		
		double z = Math.log(vScale) / Math.log(ProfileField.ZOOM_A);
		
		model.getVField().setZoom((int)z);
		model.getVField().setStartSample(0);
		
		double h = (double)(model.getVField().getViewDimension().width - 20) / ((double)sgyFile.getTraces().size());
		
		double realAspect = h / model.getVField().getVScale();
		
		//model.getVField().setAspect(Math.log(realAspect) / Math.log(ProfileField.ASPECT_A));
		model.getVField().setAspectReal(realAspect);
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
	}
	
	@Override
	public List<Node> getToolNodes() {
	
		return Arrays.asList(backBtn, fitBtn, nextBtn);
	}

	

}
