package com.ugcs.gprvisualizer.app.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

/**
 * find cohesive lines in edges  
 * 
 */
public class HorizontalGroupScan implements Command {

	private Model model = AppContext.model;
	private static final int []LOOKINGORDER = {0, -1, 1 , -2, 2};
	
	@Override
	public void execute(SgyFile file) {
		List<HorizontalProfile> result = new ArrayList<>();
		
		Trace trace = file.getTraces().get(0);
		
		
		for(int start_smp=4; start_smp<file.getMaxSamples()-4; start_smp++) {
			
			HorizontalProfile hp = new HorizontalProfile(file.getTraces().size());			
			
			if(trace.edge[start_smp]==0) {
				continue;				
			}			
			
			int example = trace.edge[start_smp];
			int last_smp = start_smp;
			
			int index = 0;
			int miss_count = 0;
			int all_miss_count = 0;
			for(Trace tr : file.getTraces()) {
				int found_smp = findExampleAround(example, last_smp, tr);
				if(found_smp == -1) {
					
					hp.deep[index] = last_smp;
					miss_count++;
					all_miss_count++;
				}else {
					hp.deep[index] = found_smp;
					miss_count=0;
					
					last_smp = found_smp;
				}
				
				if(miss_count>6 || all_miss_count > index/3+10) {
					hp = null;
					break;
				}				
				index++;
			}
			if(hp != null) {
				hp.finish(file.getTraces());
				//System.out.println("  hp found  ");
				result.add(hp);
			}
		}
		
		
		file.profiles = result;
	}

	public int findExampleAround(int example, int last_smp, Trace tr) {
		
		int max = tr.getNormValues().length-1;
		
		for(int ord=0; ord <LOOKINGORDER.length; ord++) {
			int smp = last_smp+LOOKINGORDER[ord];
			if(smp >= 0 && smp < max && tr.edge[smp] == example) {
				return smp;
			}
		}
		return -1;
	}

	@Override
	public String getButtonText() {
		
		return "Cohesive scan";
	}

	@Override
	public Change getChange() {
		
		return Change.justdraw;
	}

}
