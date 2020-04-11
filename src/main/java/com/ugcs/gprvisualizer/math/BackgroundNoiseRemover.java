package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.commands.Command;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class BackgroundNoiseRemover implements Command {


	Model model = AppContext.model;
	
	
	@Override
	public void execute(SgyFile file) {
		BackgroundRemovalFilter brf = new BackgroundRemovalFilter();
		
		float[] profile = getAvgProfileByAuxRect(brf);
		
		
		List<Trace> lst = file.getTraces();
	
		float[] subteProfile = null; 
				
		if(lst.size() > 1) {
			//brf.removeConstantNoise(lst);
			if(profile != null) {
				subteProfile = profile;
			}else {
				// remove noise only on the top layers
				int deep = model.getSettings().layer + model.getSettings().hpage;
				subteProfile = brf.prepareNoiseProfile(lst, deep);
			}
			
			brf.subtractProfile(lst, subteProfile);
		}
		
		file.setUnsaved(true);
		
	}

	public float[] getAvgProfileByAuxRect(BackgroundRemovalFilter brf) {
		BaseObject obj = AppContext.cleverImageView.getAuxEditHandler().getSelected();
		
		float[] profile = null;
		
		if(obj != null && obj instanceof AuxRect ) {
			
			AuxRect ar = (AuxRect)obj;
			
			List<Trace> tr = new ArrayList<>();
			for(int i= ar.getTraceStartGlobal(); i< ar.getTraceFinishGlobal(); i++) {
				tr.add(model.getFileManager().getTraces().get(i));
			}			
			
			profile = brf.prepareNoiseProfile(tr, ar.getSampleFinish());
			
			System.out.println(" use aux rect for profile ");
		}
		return profile;
	}

	@Override
	public String getButtonText() {

		return "Background removal";
	}


	@Override
	public Change getChange() {
		
		return Change.traceValues;
	}

	
}
