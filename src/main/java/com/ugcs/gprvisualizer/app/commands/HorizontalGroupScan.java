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
	private static final int []LOOKINGORDER = {0, -1, 1 /*, -2, 2*/};
	
	@Override
	public void execute(SgyFile file) {
		System.out.println("  -HorizontalGroupScan- " + file.getFile().getName());
		List<HorizontalProfile> result = new ArrayList<>();
		
		Trace trace = file.getTraces().get(0);
		int miss_count = 0;
		for(int start_smp=4; start_smp<file.getMaxSamples()-4; start_smp++) {
			
			HorizontalProfile hp = new HorizontalProfile(file.getTraces().size());			
			
			if(trace.edge[start_smp]==0) {
				continue;				
			}			
			
			int example = trace.edge[start_smp];
			int last_smp = start_smp;
			
			for(Trace tr : file.getTraces()) {
				int found_smp = findExampleAround(example, last_smp, tr);
				if(found_smp == -1) {
					
					hp.deep[tr.indexInFile] = last_smp;
					miss_count++;
				}else {
					hp.deep[tr.indexInFile] = found_smp;
					miss_count=0;
					
					last_smp = found_smp;
				}
				
				if(miss_count>3) {
					hp = null;
					break;
				}				
			}
			if(hp != null) {
				hp.finish(file.getTraces());
				//System.out.println("  hp found  ");
				result.add(hp);
			}
		}
		
		
		List<HorizontalProfile> tmpStraight = new ArrayList<>();
		List<HorizontalProfile> tmpCurve = new ArrayList<>();
		for(HorizontalProfile hp : result) {
			if(hp.height <= 2) {
				tmpStraight.add(hp);
			}else {
				tmpCurve.add(hp);
			}
		}
		
		if(tmpStraight.isEmpty() || tmpCurve.isEmpty()) {
			return;
		}
		System.out.println("  -top- ");
		HorizontalProfile brightestTop = getBrightest(tmpStraight);
		brightestTop.color = Color.RED;
		
		System.out.println("  -grnd- ");
		HorizontalProfile brightestGrn = getBrightest(tmpCurve);
		brightestGrn.color = Color.RED;
		
		HorizontalProfile bott = new HorizontalProfile(file.getTraces().size());
		for(int i=0; i<file.getTraces().size(); i++ ) {
			
			bott.deep[i] = Math.max(0, brightestGrn.deep[i] + (brightestGrn.deep[i] - brightestTop.deep[i]));
			
		}
		bott.finish(file.getTraces());
		bott.color = Color.RED;
		
		
		//result = new ArrayList<>();
		//result.add(brightestTop);
		//result.add(brightestGrn);
		result.add(bott);
		file.profiles = result;
		
	}

	public HorizontalProfile getBrightest(List<HorizontalProfile> tmpStraight) {
		int maxi = 0;
		int mini = 0;
		for(int i=0; i< tmpStraight.size(); i++) {
			
			HorizontalProfile hpi = tmpStraight.get(i);
			System.out.println("  avg of curve " + hpi.avgval);
			
			if(hpi.avgval > tmpStraight.get(maxi).avgval) {
				maxi = i;
			}
			if(hpi.avgval < tmpStraight.get(mini).avgval) {
				mini = i;
			}
		}
		int mid = (maxi+mini)/2;
		
		System.out.println(mini + " <-min " + mid + " max->  " + maxi);
		
		HorizontalProfile brightest = tmpStraight.get(mid);
		return brightest;
	}

	public int findExampleAround(int example, int last_smp, Trace tr) {
		for(int ord=0; ord <LOOKINGORDER.length; ord++) {
			int smp = last_smp+LOOKINGORDER[ord];
			if(smp >= 0 && smp<200 && tr.edge[smp] == example) {
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
