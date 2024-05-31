package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ugcs.gprvisualizer.app.commands.CancelKmlToFlag;
import com.ugcs.gprvisualizer.app.commands.KmlToFlag;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.UiUtils;
import com.ugcs.gprvisualizer.app.commands.BackgroundNoiseRemover;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.LevelClear;
import com.ugcs.gprvisualizer.app.commands.LevelGround;
import com.ugcs.gprvisualizer.app.commands.LevelManualSetter;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.app.commands.SpreadCoordinates;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;


@Component
public class LevelFilter implements ToolProducer, SmthChangeListener { 

	@Autowired
	private Model model;	
	
	@Autowired
	private UiUtils uiUtils;

	@Autowired
	private CommandRegistry commandRegistry;
	
	private Button buttonRemoveLevel;
	private Button buttonLevelGround;
	private ToggleButton levelPreview;
	Node slider;

	private Button buttonSpreadCoord;
	private Button buttonKmlToFlag;
	private Button buttonCancelKmlToFlag;

	private boolean levelCalculated = true;
	

	
	public LevelFilter() {
	}

//	public void smoothLevel() {
//		for(SgyFile sf : model.getFileManager().getFiles()) {
//			
//			int result[] = new int[sf.getTraces().size()];
//			for(int i = 0; i < sf.getTraces().size(); i++) {
//				result[i] = avg(sf.getTraces(), i);				
//			}
//			
//			for(int i = 0; i < sf.getTraces().size(); i++) {
//				Trace tr = sf.getTraces().get(i);
//				tr.maxindex = result[i];				
//			}			
//		}
//	}

//	int R=8;
//	private int avg(List<Trace> traces, int i) {
//		
//		int from = i-R;
//		from = Math.max(0, from);
//		int to = i+R;
//		to = Math.min(to, traces.size()-1);
//		int sum = 0;
//		int cnt = 0;
//		for(int j=from; j<= to; j++) {
//			sum += traces.get(j).maxindex;
//			cnt++;
//		}
//		return sum/cnt;
//	}

	@Override
	public List<Node> getToolNodes() {
		

		buttonRemoveLevel = commandRegistry.createButton(new LevelClear(),
			e -> { 
				levelCalculated = false; 
				updateButtons(); 
			});
			
		buttonLevelGround = commandRegistry.createButton(new LevelGround(), 
			e -> {				
				levelCalculated = false;
				updateButtons();
			});
		
		buttonLevelGround.setGraphic(ResourceImageHolder.getImageView("levelGrnd.png"));

		
		buttonSpreadCoord = commandRegistry.createButton(new SpreadCoordinates(), 
			e -> {
				buttonSpreadCoord.setVisible(false);
			});

		buttonKmlToFlag = commandRegistry.createButton(new KmlToFlag(),
			e -> {
				//buttonKmlToFlag.setVisible(false);
			});
		buttonCancelKmlToFlag = commandRegistry.createButton(new CancelKmlToFlag(),
			e -> {

			});

		levelPreview = uiUtils.prepareToggleButton("Level preview", null, 
				model.getSettings().levelPreview, 
				Change.justdraw);
		
		slider = uiUtils.createSlider(model.getSettings().levelPreviewShift, Change.justdraw, -50, 50, "shift");


		List<Node> result = new ArrayList<Node>();
		result.add(commandRegistry.createButton(new BackgroundNoiseRemover()));
		
		if (!AppContext.PRODUCTION) {
		    result.addAll(Arrays.asList(
			 
		
			commandRegistry.createButton(new LevelScanner(), "scanLevel.png", 
					e -> {
						levelCalculated = true; 
						updateButtons(); 
					}),
			commandRegistry.createButton(new LevelManualSetter(), 
					e -> {
						levelCalculated = true; 
						updateButtons(); 
					}),
			
				buttonRemoveLevel, buttonLevelGround, levelPreview, slider, buttonSpreadCoord, buttonKmlToFlag, buttonCancelKmlToFlag
		
		    	));		
		} else {		
			
			
			result.addAll(Arrays.asList(
					buttonRemoveLevel, buttonLevelGround, levelPreview, slider, buttonSpreadCoord, buttonKmlToFlag, buttonCancelKmlToFlag
					
					
					));
		}

		String cssLayout = "-fx-border-color: gray;\n"
				+ "-fx-border-insets: 5;\n"
				+ "-fx-border-width: 2;\n"
				+ "-fx-border-style: solid;\n";

		VBox vbox = new VBox();
		vbox.setStyle(cssLayout);

		vbox.setDisable(!isActive());
		vbox.getChildren().addAll(result);

		return List.of(vbox);

		//return result;
	}

	public boolean isActive() {
		return model.isActive();
	}


	protected void updateButtons() {
		buttonLevelGround.setDisable(!isGroundProfileExists());
		buttonRemoveLevel.setDisable(!isGroundProfileExists());
		levelPreview.setDisable(!isGroundProfileExists());
		slider.setDisable(!isGroundProfileExists());
	}
	
	protected boolean isGroundProfileExists() {
		return !model.getFileManager().getFiles().isEmpty() &&
				model.getFileManager().getFiles().get(0).groundProfile != null;
	}
	
	public void clearForNewFile() {
		
		levelCalculated = true; 
		updateButtons(); 
	}
	
	@Override
	public void somethingChanged(WhatChanged changed) {

		if (changed.isFileopened() || changed.isUpdateButtons() || changed.isTraceCut()) {
			
			clearForNewFile();
			
			buttonSpreadCoord.setVisible(model.isSpreadCoordinatesNecessary());
			buttonSpreadCoord.setManaged(model.isSpreadCoordinatesNecessary());

			buttonKmlToFlag.setVisible(model.isKmlToFlagAvailable());
			buttonKmlToFlag.setManaged(model.isKmlToFlagAvailable());
			buttonCancelKmlToFlag.setVisible(model.isKmlToFlagAvailable());
			buttonCancelKmlToFlag.setManaged(model.isKmlToFlagAvailable());

		}
	}

}
