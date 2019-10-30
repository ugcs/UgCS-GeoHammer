package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.gpr.Model;

public class LevelFilter {

	public void execute(Model model) {
		
		List<Trace> lst = model.getFileManager().getTraces();
		
		
		float avg[] = new float[lst.get(100).getOriginalValues().length];
		
		
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			
			arraySum(avg, trace.getOriginalValues());
		}
		
		arrayDiv(avg, lst.size());
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			
			float normval[] = Arrays.copyOf(trace.getOriginalValues(), trace.getOriginalValues().length);
			arraySub(normval, avg);
			
			trace.setNormValues(normval);
		}

		
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);

			trace.maxindex = getMaxAmpIndex(trace);
			trace.maxindex2 = trace.maxindex;
		}
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);			
			int avgs = getAvgAround(lst, index);
			if(Math.abs(trace.maxindex - avgs) > 2) {
				trace.maxindex2 = avgs;
			}
		}
		
		
		leveling(lst);
	}
	
	protected void leveling(List<Trace> lst) {
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);			

			float values[] = trace.getNormValues();
			
			System.arraycopy(values, trace.maxindex2, values, 0, values.length-trace.maxindex2); 
		}
	}
	
	protected void groundremov(List<Trace> lst) {
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);			
			
			float values[] = trace.getNormValues();
			
			float avg[] = new float[lst.get(100).getOriginalValues().length];
			
			
			int cnt = 0;
			for(int i2=index - 25; i2<index+25; i2++) {
				if(i2 > 0 && i2 < lst.size()) {
					Trace trace2 = lst.get(index);
					arraySum(avg, trace2.getNormValues());
					cnt++;
				}
			}
			
			arrayDiv(avg, cnt);
			
			trace.setOriginalValues(avg);
			
			
			//System.arraycopy(values, trace.maxindex2, values, 0, values.length-trace.maxindex2); 
		}
		
		for(int index=0; index<lst.size(); index++) {
			Trace trace = lst.get(index);
			arraySub(trace.getNormValues(), trace.getOriginalValues());
		}
	}

	private void arraySum(float avg[], float add[]) {
		for(int i = 0; i<avg.length && i<add.length; i++) {
			avg[i] += add[i];
		}
	}

	private void arraySub(float avg[], float add[]) {
		for(int i = 0; i<avg.length && i<add.length; i++) {
			avg[i] -= add[i];
		}
	}

	private void arrayDiv(float avg[], float divider) {
		for(int i = 0; i<avg.length; i++) {
			avg[i] /= divider;
		}
	}

	private int getMaxIndex(Trace trace) {
		int maxIndex = -1;
		float [] values = trace.getNormValues();
		for(int i=0; i<values.length; i++) {
			if(maxIndex == -1 || values[maxIndex] < values[i] ) {
				maxIndex = i;
			}			
		}
		
		return maxIndex;
	}

	private int getMaxAmpIndex(Trace trace) {
		int maxIndex = -1;
		float maxamp = -1;
		float lastminus = 0;
		float [] values = trace.getNormValues();
		for(int i=1; i<values.length; i++) {
			if(values[i] < 0 && values[i-1] >= 0) {
				lastminus = 0;
			}			
			if(values[i] < lastminus) {
				lastminus = values[i];
			}			
			
			if(maxIndex == -1 || maxamp < (values[i]-lastminus) ) {
				maxIndex = i;
				maxamp = (values[i]-lastminus);
			}			
		}
		
		return maxIndex;
	}

	private int getAvgAround(List<Trace> traces, int trind) {
		
		int avg = 0;
		int cnt = 0;
		for(int i=trind-10; i<trind+10; i++) {
			if(i >=0 && i < traces.size() && i != trind) {
				avg += traces.get(i).maxindex;
				cnt ++;
			}			
		}
		
		return avg / cnt;
	}
	
}
