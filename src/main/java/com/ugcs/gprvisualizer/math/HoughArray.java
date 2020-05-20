package com.ugcs.gprvisualizer.math;

import java.util.Arrays;

import com.ugcs.gprvisualizer.app.Sout;

public class HoughArray {
	public static double[] FACTOR = new double[HoughScan.DISCRET_SIZE];

	static {
		for (int i = 0; i < HoughScan.DISCRET_SIZE; i++) {
			FACTOR[i] = (HoughScan.DISCRET_FROM 
				+ (double) i 
				/ (double) HoughScan.DISCRET_SIZE 
				* (HoughScan.DISCRET_TO - HoughScan.DISCRET_FROM));
		}
	}
	
	double[] ar = new double[HoughScan.DISCRET_SIZE];

	public void clear() {
		Arrays.fill(ar, 0);
	}

	public void add(int afrom, int ato, double value) {

		int from = Math.min(afrom, ato);
		int to = Math.max(afrom, ato);
		if (to < 0 || from >= HoughScan.DISCRET_SIZE) {
			return;
		}

		from = MathUtils.norm(from, 0, HoughScan.DISCRET_SIZE);
		to = MathUtils.norm(to, 0, HoughScan.DISCRET_SIZE);

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
