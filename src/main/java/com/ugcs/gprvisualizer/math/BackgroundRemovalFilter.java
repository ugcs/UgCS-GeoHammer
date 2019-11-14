package com.ugcs.gprvisualizer.math;

import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;

public class BackgroundRemovalFilter {
	public void removeConstantNoise(List<Trace> lst) {
		float avg[] = new float[lst.get(1).getNormValues().length];

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			ArrayMath.arraySum(avg, trace.getNormValues());
		}

		ArrayMath.arrayDiv(avg, lst.size());

		for (int index = 0; index < lst.size(); index++) {
			Trace trace = lst.get(index);

			float normval[] = Arrays.copyOf(trace.getNormValues(), trace.getNormValues().length);
			ArrayMath.arraySub(normval, avg);

			trace.setNormValues(normval);
		}
	}

}
