package com.ugcs.gprvisualizer.math;

import java.util.Arrays;

public class HoughHypFullness {

	private int horizontalSize;
	private int searchIndex;
	private int searchEdge;
	
	private boolean isPrintLog;
	int[] pointCount;
	double[] amplitudes;
	
	int outsidecount = 0;
	int insidecount = 0;
	double normFactor;
	
	public HoughHypFullness(int horizontalSize, int searchIndex, int searchEdge,
			boolean isPrintLog,
			double normFactor) {
		this.horizontalSize = horizontalSize;
		this.searchIndex = searchIndex;
		this.searchEdge = searchEdge;
		
		this.isPrintLog = isPrintLog;
		
		this.normFactor = normFactor;
		
		pointCount = new int[horizontalSize * 2 + 1];
		amplitudes = new double[horizontalSize * 2 + 1];
	}
	
	
	
	public void add(int tr, int xfd1, int xfd2, int edge, float ampValue) {
		boolean inside = 
				xfd1 <= searchIndex && xfd2 >= searchIndex; 
				//|| xfd2 <= searchIndex && xfd1 >= searchIndex;
				
		if (edge == searchEdge) {
			if (inside) {
				int index = horizontalSize + tr;
				if (index < 0 || index >= pointCount.length) {
					return;
				}
				
				pointCount[index]++;
				
				amplitudes[index] = Math.max(amplitudes[index], Math.abs(ampValue));
			} else {
				//
				if (xfd2 < searchIndex && xfd2 > searchIndex - HoughDiscretizer.OUTSIDE_STEPS) {
					outsidecount++;
				}
				if (xfd1 > searchIndex && xfd1 < searchIndex + HoughDiscretizer.INSIDE_STEPS) {
					insidecount++;
				}
				
			}
		}
	}
	
	/**
	 * Max zero group in % of all length.
	 * @return
	 */
	public double getMaxGap() {
		
		int maxZeroGroup = getMaxZeroGroup();
		
		return (double) maxZeroGroup / (double) horizontalSize;
	}

	/**
	 * except start and finish gaps.
	 * @return
	 */
	private int getMaxZeroGroup() {
		int result = 0;
		int group = 0;
		boolean started = false;
		
		StringBuilder sb = new StringBuilder();
		
		for (int point : pointCount) {
			if (isPrintLog) {
				sb.append(point);
				sb.append("");
			}
			
			if (point == 0) {
				if (started) {
					group++;
				}
			} else {
				started = true;
				if (group > result) {
					result = group;					
				}				
				group = 0;
			}			
		}
		//if (group > result) {
		//	result = group;
		//}				
		
		if (isPrintLog) {
			System.out.println("zerogrp: " + result + "  ar:  " + sb.toString());
			
		}

		
		return result;
	}
	
	
	
	public double getBorderWeakness() {
		//left2 left1 center right1 right2
		int lpos = amplitudes.length * 1 / 8;
		int rpos = amplitudes.length * 7 / 8;
		int clpos = amplitudes.length * 2 / 8;
		int crpos = amplitudes.length * 6 / 8;
		
		
		double left2 = getRangeValue(0, lpos); 
		double left1 = getRangeValue(lpos, clpos);
		double center = getRangeValue(clpos, crpos);
		double right1 = getRangeValue(crpos, rpos);
		double right2 = getRangeValue(rpos, amplitudes.length);
		
		double k1 = norm(left2 / left1);
		double k2 = norm(left1 / center);
		double k3 = norm(right1 / center);
		double k4 = norm(right2 / right1);
		
		return (k1 + k2 + k3 + k4) / 4;
	}
	
	static class Avg {
		double cnt = 0;
		double sum = 0;
		
		public void add(double e) {
			cnt++;
			sum += e;
		}
		
		public double avg() {
			if (cnt > 0) {
				return sum / cnt;
			} else {
				// 1 - worst value
				return 1;
			}
		}
	}
	
	private double getRangeValue(int start, int finish) {
		Avg avg = new Avg();
		Arrays.stream(amplitudes, start, finish).forEach(e -> {
			if (e > 0) {
				avg.add(e);
			}
		});
		
		return avg.avg();
	}
	
	public double getLeftCount() {
		double s = 0;
		for(int i = 0; i < pointCount.length / 2; i++ ) {
			s+= pointCount[i];
		}
		
		return s;
	}

	public double getRightCount() {
		double s = 0;
		for(int i = pointCount.length / 2 + 1; i < pointCount.length; i++ ) {
			s+= pointCount[i];
		}
		
		return s;		
	}
	
	public int getDistantPointsCount() {
		int threshold = (int) ((double) horizontalSize * 0.3);
		
		return 
			getDistantPointsCount(-1, threshold)
			+ getDistantPointsCount(+1, threshold);
		
		
	}
	private int getDistantPointsCount(int side, int threshold) {
		 
		int result = 0;
		int zerogrp = 0;
		boolean startCount = false;
		
		for(int i = pointCount.length / 2 + 1; i < pointCount.length && i >= 0; i+= side ) {
			if (pointCount[i] == 0) {
				zerogrp++;
				
				if (zerogrp > threshold) {
					startCount = true;
				}
			} else {
				zerogrp = 0;
				
				if (startCount) {
					result++;
				}
			}
		}
		
	
		return result;
	}
	

	public double getMaxAmpl() {
		
		return Arrays.stream(amplitudes).max().getAsDouble();		
	}
	
	// 0  -  1
	private static final double FROM = 0.6;
	
	private double norm(double k) {
		
		//0.6-1 ->  0-1 ..
		
		return Math.min(1.2,
				Math.max(0, k - FROM) / (1 - FROM));
	}

	public double getOutsideCount() {
		return outsidecount * normFactor;
	}

	public double getInsideCount() {
		return insidecount * normFactor;
	}
}
