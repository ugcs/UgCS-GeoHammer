package com.ugcs.gprvisualizer.math;

import java.util.Arrays;

import com.ugcs.gprvisualizer.app.Sout;

public class HoughArray {
	
	public static int DISCRET_SIZE = HoughDiscretizer.DISCRET_SIZE;
	public static double[] FACTOR = new double[DISCRET_SIZE];
	double threshold;
	
	double clearness;
	double localMax;
	int localMaxIndex;
	
	static {
		FACTOR[0] = 0.75;
		HoughDiscretizer discr = new HoughDiscretizer();
		for (int i = 1; i < DISCRET_SIZE; i++) {
			
			double gain = (1 + ((double) i / (double) DISCRET_SIZE * 0.15));
			
			
			FACTOR[i] = discr.back(i);
			
			double old = (HoughDiscretizer.FACTORX_FROM 
				+ (double) i 
				/ (double) DISCRET_SIZE 
				* HoughDiscretizer.FACTORX_WIDTH)
				* gain;
			
			Sout.p("factor " + i + " -> " + FACTOR[i] + "  old: " + old);
		}
	}
	
	public static void main(String[] args) {
		
	}

	public static double[] REDUCE = new double[DISCRET_SIZE];

	static {
		for (int i = 0; i < DISCRET_SIZE; i++) {
			REDUCE[i] = 
				1.20 * (HoughDiscretizer.FACTORX_FROM 
						- (double) i / (double) DISCRET_SIZE * 0.02);
			
			Sout.p("reduce " + i + " -> " + REDUCE[i]);
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

//	int getMaxIndex() {
//		double max = 0;
//		int index = 0;
//		for (int i = 0; i < ar.length; i++) {
//
//			double v = ar[i];
//
//			if (v > max) {
//				max = v;
//				index = i;
//			}
//		}
//
//		return index;
//
//	}

	public void calculate() {
		double max = 0;
		int index = 0;
		boolean thresholdReached = false;
		boolean minimumReached = false;
		
		double smallest = 999999.0;
		for (int i = ar.length - 1; i >= 0; i--) {

			double v = ar[i];

			if (thresholdReached) {
				
				smallest = Math.min(smallest, v);
				
				if (smallest < max / 3) {
					minimumReached = true;
				}
				//return index;
			}
			
			if (v > threshold) {
				thresholdReached = true;
				smallest = v;
			}
			
			if (v > max && !minimumReached) {
				max = v;
				index = i;
			}
		}

		localMaxIndex = index;
		localMax = max;
		clearness = smallest / max;
		

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

	public double getClearness() {
		
		return clearness;
	}

	public double getLocalMax() {
		
		return localMax;
	}

	public int getLocalMaxIndex() {
		return localMaxIndex;
	}
	
}
