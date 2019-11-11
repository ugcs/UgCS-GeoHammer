package com.ugcs.gprvisualizer.math;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;

public class HiperTangFilter implements ToolProducer {

	private Model model;
	public HiperTangFilter(Model model){
		this.model = model;
	}
	
	@Override
	public List<Node> getToolNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void execute() {
		for (SgyFile sf : model.getFileManager().getFiles()) {
			List<Trace> lst = sf.getTraces();
			
			for(Trace trace : lst) {
				float values[] = trace.getNormValues();
				for(int i=0; i<values.length; i++) {
					values[i] = (float)Math.tanh(values[i]/900.0) * 900;
				}				
			}			
		}		
	}
}
