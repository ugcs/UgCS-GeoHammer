package com.ugcs.gprvisualizer.gpr;

import java.util.List;

public class ConstNoiseRemoveFilter {

	
	public void execute(List<Scan> list){
		
		int traceCount = list.get(0).values.length;
		int halfrange = 50;
		
		float[] avg = new float[list.size()];
		for(int trace = 0; trace < traceCount; trace++){
			
			for(int sc = 0; sc < list.size(); sc++){
			
				int start = norm(sc - halfrange, list.size());
				int finish = norm(sc + halfrange, list.size());
				float sum = 0;
				float div = 0;
				for(int i = start; i<finish; i++){
					float kf = 1;//(1 - Math.abs(trace - i) / halfrange);
					sum += list.get(i).values[trace] * kf;
					div += kf;
				}
			
				avg[sc] = sum / div;
			}
			
			for(int sc = 0; sc < list.size(); sc++){
				
				list.get(sc).values[trace] -= avg[sc];
			}
			
		}
		
		
	}
	
	int norm(int i, int max){
		return i < 0 ? 0 : (i > max ? max : i);
	}
	
}
