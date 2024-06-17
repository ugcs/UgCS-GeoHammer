package com.ugcs.gprvisualizer.app.commands;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SampleNormalizer;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.BackgroundRemovalFilter;

public class BackgroundNoiseRemover implements Command {


	Model model = AppContext.model;
	
	
	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		BackgroundRemovalFilter brf = new BackgroundRemovalFilter();
		
		List<Trace> lst = file.getTraces();
	
		float[] subteProfile = null; 
				
		if (lst.size() > 1) {
			//int deep = model.getSettings().layer + model.getSettings().hpage;
			int deep = file.getMaxSamples();
			
			subteProfile = brf.prepareNoiseProfile(lst, deep);
			brf.subtractProfile(lst, subteProfile);
		}

		//new SampleNormalizer().normalize(lst);

		file.setUnsaved(true);
		
	}

	@Override
	public String getButtonText() {
		return "Remove background";
		//return "Background removal";
	}


	@Override
	public Change getChange() {
		
		return Change.traceValues;
	}

	
}
