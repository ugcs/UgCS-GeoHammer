package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ugcs.gprvisualizer.app.commands.CancelKmlToFlag;
import com.ugcs.gprvisualizer.app.commands.KmlToFlag;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

		buttonSpreadCoord = commandRegistry.createButton(new SpreadCoordinates(), 
			e -> {
				updateButtons();
				//buttonSpreadCoord.setVisible(false);
			});
			
		var buttons = List.of(commandRegistry.createButton(new BackgroundNoiseRemover()), buttonSpreadCoord);

		HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.setStyle("-fx-padding: 5px;");
		hbox.setDisable(!model.isActive());

		buttons.forEach(b -> {
			b.setMaxWidth(Double.MAX_VALUE);
		});

		HBox.setHgrow(buttons.get(0), Priority.ALWAYS);
		HBox.setHgrow(buttons.get(1), Priority.ALWAYS);

		hbox.getChildren().addAll(buttons);

		return List.of(hbox);
	}

	
	public List<Node> getToolNodes2() {

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
		
		//buttonLevelGround.setGraphic(ResourceImageHolder.getImageView("levelGrnd.png"));

		
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


		List<Node> result = new ArrayList<>();
		
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
			
				buttonRemoveLevel, buttonLevelGround, levelPreview, slider, buttonKmlToFlag, buttonCancelKmlToFlag
		
		    	));		
		} else {
			HBox hbox = new HBox();
			hbox.setSpacing(5);

			buttonRemoveLevel.setMaxWidth(Double.MAX_VALUE);
			buttonLevelGround.setMaxWidth(Double.MAX_VALUE);

			HBox.setHgrow(buttonRemoveLevel, Priority.ALWAYS);
			HBox.setHgrow(buttonLevelGround, Priority.ALWAYS);

			hbox.getChildren().addAll(buttonLevelGround, buttonRemoveLevel);					
			result.addAll(List.of(
					//buttonRemoveLevel, 
					//buttonLevelGround, 
					//levelPreview, 
					slider,
					hbox 
					//buttonKmlToFlag, buttonCancelKmlToFlag
			));
		}

		VBox vbox = new VBox();

		vbox.setDisable(!model.isActive());
		vbox.getChildren().addAll(result);

		return List.of(vbox);
	}

	protected void updateButtons() {

		if (buttonSpreadCoord != null) {
			buttonSpreadCoord.setDisable(!model.isSpreadCoordinatesNecessary());
			//buttonSpreadCoord.setManaged(model.isSpreadCoordinatesNecessary());
		}

		if (buttonLevelGround != null) {
			buttonLevelGround.setDisable(!isGroundProfileExists());
		}
		if (buttonRemoveLevel != null) {
			buttonRemoveLevel.setDisable(!isGroundProfileExists());
		}
		if (levelPreview != null) {
			levelPreview.setDisable(!isGroundProfileExists());
		}
		if (slider != null) {
			slider.setDisable(!isGroundProfileExists());
		}
	}
	
	protected boolean isGroundProfileExists() {
		return !model.getFileManager().getGprFiles().isEmpty() &&
				model.getFileManager().getGprFiles().get(0).groundProfile != null;
	}
	
	public void clearForNewFile() {
		
		levelCalculated = true; 
		updateButtons(); 
	}
	
	@Override
	public void somethingChanged(WhatChanged changed) {

		if (changed.isFileopened() || changed.isUpdateButtons() || changed.isTraceCut()) {
			
			clearForNewFile();
			
			if (buttonSpreadCoord != null) {
				buttonSpreadCoord.setDisable(!model.isSpreadCoordinatesNecessary());
				buttonSpreadCoord.setManaged(model.isSpreadCoordinatesNecessary());
			}

			if (buttonKmlToFlag != null) {
				buttonKmlToFlag.setVisible(model.isKmlToFlagAvailable());
				buttonKmlToFlag.setManaged(model.isKmlToFlagAvailable());
			}

			if (buttonCancelKmlToFlag != null) {
				buttonCancelKmlToFlag.setVisible(model.isKmlToFlagAvailable());
				buttonCancelKmlToFlag.setManaged(model.isKmlToFlagAvailable());
			}
		}
	}

}
