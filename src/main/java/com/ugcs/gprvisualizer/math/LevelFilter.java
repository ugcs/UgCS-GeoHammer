package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.BackgroundNoiseRemover;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.GroundBandRemovalFilter;
import com.ugcs.gprvisualizer.app.commands.LevelClear;
import com.ugcs.gprvisualizer.app.commands.LevelGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.app.commands.RemoveGroundLevel;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class LevelFilter implements ToolProducer { 

	Button buttonRemoveLevel;
	Button buttonLevelGround;

	Model model;

	public LevelFilter(Model model) {
		this.model = model;
	}

	public void smoothLevel() {
		for(SgyFile sf : model.getFileManager().getFiles()) {
			
			int result[] = new int[sf.getTraces().size()];
			for(int i = 0; i < sf.getTraces().size(); i++) {							
				result[i] = avg(sf.getTraces(), i);				
			}
			
			for(int i = 0; i < sf.getTraces().size(); i++) {
				Trace tr = sf.getTraces().get(i);
				tr.maxindex = result[i];				
			}			
		}
	}

	int R=8;
	private int avg(List<Trace> traces, int i) {
		
		int from = i-R;
		from = Math.max(0, from);
		int to = i+R;
		to = Math.min(to, traces.size()-1);
		int sum = 0;
		int cnt = 0;
		for(int j=from; j<= to; j++) {
			sum += traces.get(j).maxindex;
			cnt++;
		}
		return sum/cnt;
	}

	@Override
	public List<Node> getToolNodes() {
		

		buttonRemoveLevel = CommandRegistry.createButton(new LevelClear(),
			e->{ 
				levelCalculated = false; 
				updateButtons(); 
			});
			
		buttonLevelGround = CommandRegistry.createButton(new LevelGround(), 
			e -> {				
				levelCalculated = false;
				updateButtons();
			});


		return Arrays.asList(
			CommandRegistry.createButton(new BackgroundNoiseRemover()), 
		
			CommandRegistry.createButton(new LevelScanner(), 
					e->{ 
						levelCalculated = true; 
						updateButtons(); 
					}),
			CommandRegistry.createButton(new LevelScanHP(), 
					e->{ 
						levelCalculated = true; 
						updateButtons(); 
					}),
			
				buttonRemoveLevel, buttonLevelGround 
		
			//CommandRegistry.createButton(new GroundBandRemovalFilter()),
			
			//CommandRegistry.createButton(new EdgeSubtractGround()),
			
			
		);				
					
			
		
	}

	boolean levelCalculated = false;
	protected void updateButtons() {
		buttonLevelGround.setDisable(!levelCalculated);
		buttonRemoveLevel.setDisable(!levelCalculated);
	}
	
	public void clearForNewFile() {
		
//		buttonFindLevel.setGraphic(null);
//		buttonLevelGround.setGraphic(null);
		//buttonRemoveBandNoise.setGraphic(null);

		levelCalculated = false; 
		updateButtons(); 
		
		updateButtons();
	}

}
