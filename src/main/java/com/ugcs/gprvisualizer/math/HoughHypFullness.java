package com.ugcs.gprvisualizer.math;

import com.ugcs.gprvisualizer.app.Sout;

public class HoughHypFullness {

	private int horizontalSize;
	private int searchIndex;
	private int searchEdge;
	
	private boolean isPrintLog;
	int[] pointCount;
	
	public HoughHypFullness(int horizontalSize, int searchIndex, int searchEdge,
			boolean isPrintLog) {
		this.horizontalSize = horizontalSize;
		this.searchIndex = searchIndex;
		this.searchEdge = searchEdge;
		
		this.isPrintLog = isPrintLog;
		
		pointCount = new int[horizontalSize * 2 + 1];
	}
	
	public void add(int tr, int xfd1, int xfd2, int edge) {
		boolean inside = 
				xfd1 <= searchIndex && xfd2 >= searchIndex 
				|| xfd2 <= searchIndex && xfd1 >= searchIndex;
				
		if (edge == searchEdge && inside) {
			
			
			int index = horizontalSize + tr;
			if (index < 0 || index >= pointCount.length) {
				return;
			}
			
			pointCount[index]++;
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
		boolean started = true;
		
		StringBuilder sb = new StringBuilder();
		
		for (int point : pointCount) {
			if(isPrintLog) {
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
		if (group > result) {
			result = group;
		}				
		
		if(isPrintLog) {
			Sout.p("zerogrp: " + result + "  ar:  " + sb.toString());
			
		}

		
		return result;
	}
	
}
