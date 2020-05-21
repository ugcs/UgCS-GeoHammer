package com.ugcs.gprvisualizer.math;

import java.util.Arrays;

import com.ugcs.gprvisualizer.app.Sout;

public class HoughArray {
	
	public static int DISCRET_SIZE = HoughDiscretizer.DISCRET_SIZE;
	public static double[] FACTOR = new double[DISCRET_SIZE];
	double threshold;
	
	static {
		FACTOR[0] = 0.75;
		for (int i = 1; i < DISCRET_SIZE; i++) {
			
			double gain = (1 + ((double) i / (double) DISCRET_SIZE * 0.15));
			
			FACTOR[i] = (HoughDiscretizer.FACTORX_FROM 
				+ (double) i 
				/ (double) DISCRET_SIZE 
				* HoughDiscretizer.FACTORX_WIDTH)
				* gain;
		}
	}

	public static double[] REDUCE = new double[DISCRET_SIZE];

	static {
		for (int i = 0; i < DISCRET_SIZE; i++) {
			REDUCE[i] = 
				1.20 * (HoughDiscretizer.FACTORX_FROM 
						- (double) i / (double) DISCRET_SIZE * 0.02);
			
			Sout.p(""+ REDUCE[i]);
		}
	}
	
	double[] ar = new double[DISCRET_SIZE];

	public HoughArray(double threshold) {
		this.threshold = threshold;
	}

	public void clear() {
		Arrays.fill(ar, 0);
	}

	public void add(int afrom, int ato, double value) {

		int from = Math.min(afrom, ato);
		int to = Math.max(afrom, ato);
		if (to < 0 || from >= DISCRET_SIZE) {
			return;
		}

		from = MathUtils.norm(from, 0, DISCRET_SIZE);
		to = MathUtils.norm(to, 0, DISCRET_SIZE);

		for (int i = from; i <= to; i++) {
			ar[i] += value * FACTOR[i];
		}
	}

	int getMaxIndex() {
		double max = 0;
		int index = 0;
		for (int i = 0; i < ar.length; i++) {

			double v = ar[i];

			if (v > max) {
				max = v;
				index = i;
			}
		}

		return index;

	}

	int getLocalMaxIndex() {
		double max = 0;
		int index = 0;
		boolean thresholdReached = false;
		for (int i = ar.length - 1; i >= 0; i--) {

			double v = ar[i];

			if(thresholdReached && v < max / 3) {
				return index;
			}
			
			if(v > threshold) {
				thresholdReached = true;
			}
			
			if (v > max) {
				max = v;
				index = i;
			}
		}

		return index;

	}
	
	double getMax() {

		int index = getMaxIndex();
		double v = ar[index];

		return v;
	}

	void print() {
		StringBuilder sb = new StringBuilder();
		sb.append(" debug a ");
		for (int i = 0; i < ar.length; i++) {
			sb.append(" ");
			sb.append(ar[i]);
		}
		Sout.p(sb.toString());
	}

}
