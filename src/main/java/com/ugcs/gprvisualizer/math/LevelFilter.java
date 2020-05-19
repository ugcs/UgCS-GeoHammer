package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.commands.BackgroundNoiseRemover;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.LevelClear;
import com.ugcs.gprvisualizer.app.commands.LevelGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;


@Component
public class LevelFilter implements ToolProducer, SmthChangeListener { 

	@Autowired
	private Model model;

	@Autowired
	private CommandRegistry commandRegistry;
	
	private Button buttonRemoveLevel;
	private Button buttonLevelGround;

	private boolean levelCalculated = false;
	

	
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


		return Arrays.asList(
			commandRegistry.createButton(new BackgroundNoiseRemover()), 
		
			commandRegistry.createButton(new LevelScanner(), 
					e -> {
						levelCalculated = true; 
						updateButtons(); 
					}),
			commandRegistry.createButton(new LevelScanHP(), 
					e -> {
						levelCalculated = true; 
						updateButtons(); 
					}),
			
				buttonRemoveLevel, buttonLevelGround 
		
		);				
					
			
		
	}

	protected void updateButtons() {
		buttonLevelGround.setDisable(!levelCalculated);
		buttonRemoveLevel.setDisable(!levelCalculated);
	}
	
	public void clearForNewFile() {
		
		levelCalculated = false; 
		updateButtons(); 
	}
	
	@Override
	public void somethingChanged(WhatChanged changed) {

		if (changed.isFileopened()) {
			
			clearForNewFile();
		}		
	}

}
