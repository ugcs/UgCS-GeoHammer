package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class LevelFilter implements ToolProducer {

	Button buttonNoise = new Button("noise-");
	Button buttonFindLevel = new Button("find level");
	Button buttonSet = new Button("leveling");

	Model model;

	public LevelFilter(Model model) {
		this.model = model;
	}

	public void execute() {

		for (SgyFile sf : model.getFileManager().getFiles()) {
			List<Trace> lst = sf.getTraces();
			removeConstantNoise(lst);
		
		}
		//removeConstantNoise(model.getFileManager().getTraces());


		// leveling(lst);
	}

	public void findGroundLevel() {
		for (SgyFile sf : model.getFileManager().getFiles()) {
			List<Trace> lst = sf.getTraces();
	
			findGroundLevel(lst);
		}
	}
	
	private void findGroundLevel(List<Trace> lst) {
		List<List<Trace>> continGrps = new ArrayList<>();
		int lastMaxIndex = -1;

		
		Set<Integer> st = getMaxAmpList(lst.get(0));
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			st = getNearestMax(st, trace);
			trace.max.addAll(st);
			
			trace.maxindex = getMaxAmpIndex(trace, 1, trace.getNormValues().length);
			if (lastMaxIndex == -1 || Math.abs(lastMaxIndex - trace.maxindex) > WND) {
				continGrps.add(new ArrayList<>());
			}
			lastMaxIndex = trace.maxindex;
			continGrps.get(continGrps.size() - 1).add(trace);

			trace.maxindex2 = trace.maxindex;
		}

		int largesIndex = getLargestGrpIndex(continGrps);
		System.out.println("largesIndex " + largesIndex + " size " + continGrps.get(largesIndex).size());
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
				t.maxindex2 = getMaxAmpIndex(t, max - WND, max + WND);
				max = t.maxindex2;
			}
		}
		continGrps.remove(j);
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
				t.maxindex2 = getMaxAmpIndex(t, max - 2, max + 2);
				max = t.maxindex2;
			}
		}
		continGrps.remove(j);
	}

	public void removeConstantNoise(List<Trace> lst) {
		float avg[] = new float[lst.get(100).getNormValues().length];

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			arraySum(avg, trace.getNormValues());
		}

		arrayDiv(avg, lst.size());

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float normval[] = Arrays.copyOf(trace.getNormValues(), trace.getNormValues().length);
			arraySub(normval, avg);

			trace.setNormValues(normval);
		}
	}

	protected void leveling(List<Trace> lst) {
		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float values[] = trace.getNormValues();

			System.arraycopy(values, trace.maxindex2, values, 0, values.length - trace.maxindex2);
		}
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
					arraySum(avg, trace2.getNormValues());
					cnt++;
				}
			}

			arrayDiv(avg, cnt);

			trace.setOriginalValues(avg);

			// System.arraycopy(values, trace.maxindex2, values, 0,
			// values.length-trace.maxindex2);
		}

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);
			arraySub(trace.getNormValues(), trace.getOriginalValues());
		}
	}

	private void arraySum(float avg[], float add[]) {
		for (int i = 0; i < avg.length && i < add.length; i++) {
			avg[i] += add[i];
		}
	}

	private void arraySub(float avg[], float add[]) {
		for (int i = 0; i < avg.length && i < add.length; i++) {
			avg[i] -= add[i];
		}
	}

	private void arrayDiv(float avg[], float divider) {
		for (int i = 0; i < avg.length; i++) {
			avg[i] /= divider;
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
		buttonNoise.setOnAction(e -> {

			execute();
		});
		
		buttonFindLevel.setOnAction(e -> {

			findGroundLevel();
		});


		buttonSet.setOnAction(e -> {

			leveling(model.getFileManager().getTraces());
		});

		return Arrays.asList(buttonNoise, buttonFindLevel, buttonSet);
	}

}
