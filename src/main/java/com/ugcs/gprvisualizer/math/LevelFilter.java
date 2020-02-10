package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.AmplitudeMatrix;
import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class LevelFilter implements ToolProducer {

	Button buttonNoise = new Button("background removal");
	Button buttonFindLevel = new Button("find ground level");
	Button buttonSmoothLevel = new Button("Smooth ground level");
	Button buttonSet = new Button("level ground");

	Model model;

	public LevelFilter(Model model) {
		this.model = model;
	}

	public void removeConstantNoise() {
		BackgroundRemovalFilter brf = new BackgroundRemovalFilter();
		for (SgyFile sf : model.getFileManager().getFiles()) {
			List<Trace> lst = sf.getTraces();
			
			if(lst.size() > 1) {
				brf.removeConstantNoise(lst);
			}
		}
		model.getChanges().add(FileChangeType.BACKGROUND_NOISE_REMOVED);
		
		AppContext.notifyAll(new WhatChanged(Change.traceValues));
	}

	public void findGroundLevel() {
		for (SgyFile sf : model.getFileManager().getFiles()) {
			List<Trace> lst = sf.getTraces();
	
			//findGroundLevel(lst);
			AmplitudeMatrix am = new AmplitudeMatrix();
			am.init(lst);
			am.findLevel();
		}
		
		AppContext.notifyAll(new WhatChanged(Change.traceValues));
	}
	
	private void findGroundLevel(List<Trace> lst) {
		List<List<Trace>> continGrps = new ArrayList<>();
		int lastMaxIndex = -1;

		
		Set<Integer> st = getMaxAmpList(lst.get(0));
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			st = getNearestMax(st, trace);
			trace.max.addAll(st);
			
			trace.maxindex = getMaxAmpIndex(trace, model.getSettings().heightStart, trace.getNormValues().length);
			trace.maxindex2 = trace.maxindex;
			
			if (lastMaxIndex == -1 || Math.abs(lastMaxIndex - trace.maxindex) > WND) {
				continGrps.add(new ArrayList<>());
			}
			lastMaxIndex = trace.maxindex;
			continGrps.get(continGrps.size() - 1).add(trace);

			
		}

		int largesIndex = getLargestGrpIndex(continGrps);

		while (continGrps.size() > largesIndex + 1) {
			combineTwoGroupsFirst(continGrps, largesIndex, largesIndex + 1);
		}
		while (continGrps.size() > 1) {
			combineTwoGroupsSecond(continGrps, continGrps.size() - 2, continGrps.size() - 1);
		}
	}
	
	

	private Set<Integer> getNearestMax(Set<Integer> st, Trace trace) {
		
		Set<Integer> ss = new HashSet<>();
		
		for(Integer i : st) {
			int ni = getMaxAmpIndex(trace, i-WND-1, i+WND);
			ss.add(ni);
		}
		
		
		return ss;
	}

	private Set<Integer> getMaxAmpList(Trace trace) {
		
		Set<Integer> res = new HashSet<>();
		
		float[] values = trace.getNormValues();
		int maxIndex = -1;
		float maxamp = -1;
		float absmaxamp = -1;
		float lastminus = 0;
		
		for(int i=1; i<values.length; i++) {

			if (values[i] < 0 && values[i - 1] >= 0) {
				lastminus = 0;
			}
			if (values[i] < lastminus) {
				lastminus = values[i];
			}

			float camp = (values[i] - lastminus);
			if (maxamp < camp) {
				maxIndex = i;
				maxamp = (values[i] - lastminus);
				absmaxamp = Math.max(absmaxamp, maxamp);
			}
			
			if(i - maxIndex > 2 && maxamp > absmaxamp/3) {
				res.add(maxIndex);
				maxamp = -1;
				
			}
		
		}
		
		return res;
	}

	private int getLargestGrpIndex(List<List<Trace>> continGrps) {
		int max = 0;
		for (int i = 0; i < continGrps.size(); i++) {
			if (continGrps.get(i).size() > continGrps.get(max).size()) {
				max = i;
			}
		}
		return max;
	}

	static final int WND = 2;
	static final int GRP_SIZE = 100;

	void combineTwoGroupsFirst(List<List<Trace>> continGrps, int i, int j) {
		List<Trace> g1 = continGrps.get(i);
		List<Trace> g2 = continGrps.get(j);
		int max = g1.get(g1.size() - 1).maxindex2;
		g1.addAll(g2);
		if (g2.size() < GRP_SIZE) {
			for (Trace t : g2) {
				t.maxindex2 = getMaxAmpIndex(t, max - WND-1, max + WND);
				max = t.maxindex2;
			}
		}
		continGrps.remove(j);
	}
	
	public void smoothLevel() {
		for(SgyFile sf : model.getFileManager().getFiles()) {
			
			int result[] = new int[sf.getTraces().size()];
			for(int i = 0; i < sf.getTraces().size(); i++) {							
				result[i] = avg(sf.getTraces(), i);				
			}
			
			for(int i = 0; i < sf.getTraces().size(); i++) {
				Trace tr = sf.getTraces().get(i);
				tr.maxindex2 = result[i];				
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
			sum += traces.get(j).maxindex2;
			cnt++;
		}
		return sum/cnt;
	}

	void combineTwoGroupsSecond(List<List<Trace>> continGrps, int i, int j) {
		List<Trace> g1 = continGrps.get(i);
		List<Trace> g2 = continGrps.get(j);
		int max = g2.get(0).maxindex2;
		int g1size = g1.size();
		g1.addAll(g2);
		if (g1size < GRP_SIZE) {
			for (int index = g1size - 1; index >= 0; index--) {
				Trace t = g1.get(index);
				t.maxindex2 = getMaxAmpIndex(t, max - WND-1, max + WND);
				max = t.maxindex2;
			}
		}
		continGrps.remove(j);
	}

	protected void leveling(List<Trace> lst) {
		int minlev = model.getFileManager().getTraces().get(0).maxindex2;
		for(Trace trace : model.getFileManager().getTraces()) {
			minlev = Math.min(minlev, trace.maxindex2);
		}
		
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float values[] = trace.getNormValues();

			System.arraycopy(values, trace.maxindex2-minlev, values, 0, values.length - (trace.maxindex2-minlev));
			
			trace.maxindex2 = 0;
		}
		
		model.getChanges().add(FileChangeType.LEVEL_TO_GROUND);
		
		AppContext.notifyAll(new WhatChanged(Change.traceValues));
	}

	protected void groundremov(List<Trace> lst) {
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float values[] = trace.getNormValues();

			float avg[] = new float[lst.get(100).getOriginalValues().length];

			int cnt = 0;
			for (int i2 = index - 25; i2 < index + 25; i2++) {
				if (i2 > 0 && i2 < lst.size()) {
					Trace trace2 = lst.get(index);
					ArrayMath.arraySum(avg, trace2.getNormValues());
					cnt++;
				}
			}

			ArrayMath.arrayDiv(avg, cnt);

			trace.setOriginalValues(avg);
		}

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);
			ArrayMath.arraySub(trace.getNormValues(), trace.getOriginalValues());
		}
	}

	private int getMaxIndex(Trace trace) {
		int maxIndex = -1;
		float[] values = trace.getNormValues();
		for (int i = 0; i < values.length; i++) {
			if (maxIndex == -1 || values[maxIndex] < values[i]) {
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	private int getMaxAmpIndex(Trace trace, int from, int to) {

		int maxIndex = -1;
		float maxamp = -1;
		float lastminus = 0;
		float[] values = trace.getNormValues();

		to = Math.min(to, values.length - 1);
		from = Math.max(from, 1);

		for (int i = from; i <= to; i++) {
			if (values[i] < 0 && values[i - 1] >= 0) {
				lastminus = 0;
			}
			if (values[i] < lastminus) {
				lastminus = values[i];
			}

			float camp = values[i] - lastminus;
			if (maxamp < camp) {
				maxIndex = i;
				maxamp = (values[i] - lastminus);
			}
		}

		return maxIndex;
	}

	private int getAvgAround(List<Trace> traces, int trind) {

		int avg = 0;
		int cnt = 0;
		for (int i = trind - 10; i < trind + 10; i++) {
			if (i >= 0 && i < traces.size() && i != trind) {
				avg += traces.get(i).maxindex;
				cnt++;
			}
		}

		return avg / cnt;
	}

	@Override
	public List<Node> getToolNodes() {
//		buttonShiftAvg.setOnAction(e -> {
//
//			AvgShiftFilter f = new AvgShiftFilter(model);
//			f.execute();			
//			
//			buttonShiftAvg.setGraphic(new ImageView(ResourceImageHolder.FXIMG_DONE));
//		});
//
//		buttonShiftAvgRemoval.setOnAction(e -> {
//
//			AvgShiftFilter f = new AvgShiftFilter(model);
//			f.execute2();			
//			
//			buttonShiftAvgRemoval.setGraphic(new ImageView(ResourceImageHolder.FXIMG_DONE));
//		});

		
		buttonNoise.setOnAction(e -> {

			removeConstantNoise();
			
			buttonNoise.setGraphic(new ImageView(ResourceImageHolder.FXIMG_DONE));
		});
		
		buttonFindLevel.setOnAction(e -> {

			findGroundLevel();
			
			buttonFindLevel.setGraphic(new ImageView(ResourceImageHolder.FXIMG_DONE));
		});

		buttonSmoothLevel.setOnAction(e -> {

			smoothLevel();
		});

		buttonSet.setOnAction(e -> {

			leveling(model.getFileManager().getTraces());
			
			buttonSet.setGraphic(new ImageView(ResourceImageHolder.FXIMG_DONE));
		});

		//buttonShiftAvg, buttonShiftAvgRemoval, 
		return Arrays.asList(buttonNoise, buttonFindLevel, buttonSet);
	}

	public void clearForNewFile() {
		buttonNoise.setGraphic(null);
		buttonFindLevel.setGraphic(null);
		buttonSmoothLevel.setGraphic(null);
		buttonSet.setGraphic(null);
	}
	
	
}
