package com.ugcs.gprvisualizer.math;

import com.ugcs.gprvisualizer.app.Sout;

public 	class RealSizeCalculator {
	public boolean print;
	int[] columnExistsLeft;	
	int[] columnExistsRight;
	int[] smpLeft;	
	int[] smpRight;
	
	int pin;
	int gap;
	
	int startleft;
	int startright;
	
	public RealSizeCalculator(int pin, int from, int to, int gap, int startleft, int startright) {
		this.pin = pin;
		this.gap = gap;
		columnExistsLeft = new int[pin - from + 1];	
		columnExistsRight = new int[to - pin + 1];
		smpLeft = new int[pin - from + 1];	
		smpRight = new int[to - pin + 1];
		
		this.startleft = pin - startleft;
		this.startright = startright - pin;
		
	}
	
	public void add(int tr, int smp) {
		if (tr < pin) {
			int ind = pin - tr;
			if (ind < columnExistsLeft.length) {
				columnExistsLeft[ind] = 1;
				smpLeft[ind] = Math.max(smpLeft[ind], smp);
			}
		} else {
			int ind = tr - pin;
			if (ind < columnExistsRight.length) {
				columnExistsRight[ind] = 1;
				smpRight[ind] = Math.max(smpRight[ind], smp);
			}				
		}
	}
	
	public int getContinuous(int[] exst, int start) {
		//boolean fst = false;
		int fndgapsize = 0;
		int lastgood = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append(start).append(" - ");
		for (int i = start; i < exst.length; i++) {
			if (print) {
				//sb.append(exst[i]).append(" ");
			}
			
			if (exst[i] == 1) {
				fndgapsize = 0;
				lastgood = i;
			} else {
				fndgapsize++;
				if (fndgapsize > gap) {
					return lastgood;
				}
			}
		}
		
		if (print) {
			//Sout.p(sb.toString());
		}
		return lastgood;
	}
	
	public int getLeft() {
		return getContinuous(columnExistsLeft, startleft);
	}
	
	public int getRight() {
		return getContinuous(columnExistsRight, startright);
	}
	
	public int getLeftSmp(int i) {
		return smpLeft[i];
	}
	
	public int getRightSmp(int i) {
		return smpRight[i];
	}
	
}
