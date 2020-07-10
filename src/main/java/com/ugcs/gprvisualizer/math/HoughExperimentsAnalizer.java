package com.ugcs.gprvisualizer.math;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.Sout;

public class HoughExperimentsAnalizer {
	
	public boolean print = false;
	SgyFile file;
	
	int left;
	int right;
	int bottom;
	
	public HoughExperimentsAnalizer(SgyFile file) {
		this.file = file;
	}
	
	public HoughExperiments debug(int tr, int smp, double heightShift) {
		print = true;
		int goodHeadEdge = findHeaderEdge(tr, smp);
		Sout.p("goodHeadEdge = " + goodHeadEdge);
		if (goodHeadEdge == 0) {
			
			goodHeadEdge = 4;
		}
		
		
		left = tr;
		right = tr;
		bottom = smp;
		HoughExperiments he = HoughExperiments.f(file, tr, smp, heightShift, goodHeadEdge, true);		
				
		left = Math.min(left, he.traceFrom);
		right = Math.max(right, he.traceTo);
		bottom = Math.max(bottom, he.lastSmp);		
		
		Sout.p(" l " + left + " r " + right + "  b " + bottom );
		
		Set<HoughExperiments> s = new HashSet<>();
		s.add(he);
		
		addPoints(s, tr, left, right, smp + 1, bottom, goodHeadEdge);
		
		he.criteriaGoodCount();
		he.criteriaRealWidth();
		he.criteriaGoodBadRatio();				
		
//		Sout.p("gc  " + he.criteriaGoodCount());
//		Sout.p("rw  " + he.criteriaRealWidth());
//		Sout.p("gbr " + he.criteriaGoodBadRatio());				
	
		return he;
	}
	
	public boolean analize(int tr, int smp) {
		
		
		//1 
		int goodHeadEdge = findHeaderEdge(tr, smp);
		
		if (goodHeadEdge == 0) {
			return false;
		}
		
		Set<HoughExperiments> l = initHE(tr, smp, goodHeadEdge);
		
		
		addPoints(l, tr, left, right, smp + 1, bottom, goodHeadEdge);
		 
		filterByGoodBadCount(l);
		
		return !l.isEmpty();
		
	}

	private void filterByGoodBadCount(Set<HoughExperiments> l) {
		
		Iterator<HoughExperiments> it = l.iterator();
		while (it.hasNext()) {
			HoughExperiments he = it.next();
			
			if(!he.criteriaGoodCount()) {
				it.remove();
				
				//Sout.p("goodcnt");
			} else if (!he.criteriaRealWidth()) {
				it.remove();
				
				//Sout.p("realw ");
			} else if (!he.criteriaGoodBadRatio()) {
				it.remove();
				
				//Sout.p("gbratio ");
			}			
		}		
	}

	public int findHeaderEdge(int tr, int smp) {
		int[] edge = new int[5];
		int r = 5;
		for (int s = smp - 1; s <= smp; s++ ) {
			for (int t = tr - r; t <= tr + r; t++) {
				
				edge[file.getEdge(tr, s)]++;
				
			}
		}
		
		int bestIndex = 1;
		for (int i = 2; i <= 4; i++) {
			if (edge[i] > edge[bestIndex]) {
				bestIndex = i;
			}
		}
		
		int val = edge[bestIndex];
		
		if (print) { Sout.p("hdr edge val " + val);}
		
		if (val > r * 2 * 3 / 4) {
			return bestIndex;
		}
		
		return 0;
	}

	private void addPoints(Set<HoughExperiments> l, int tr, int left, int right, int top, int bottom, int goodHeadEdge) {
		for (int h = top; h < bottom; h++) {
			for (int w = tr; w >= left; w--) {
				checkPoint(l, w, h, goodHeadEdge);
			}
			for (int w = tr; w <= right; w++) {
				checkPoint(l, w, h, goodHeadEdge);
			}
		}
		
	}

	private void checkPoint(Set<HoughExperiments> l, int w, int h, int goodHeadEdge) {
		if (file.getTraces().get(w).edge[h] == goodHeadEdge) {
			for (HoughExperiments he : l) {
				he.addPoint(w, h);
			}
		}
		
	}

	private Set<HoughExperiments> initHE(int tr, int smp, int goodHeadEdge) {
		
		Set<HoughExperiments> set = new HashSet<>();
		
		left = tr;
		right = tr;
		bottom = smp;
		for (double heightShift = 0; heightShift < 140; heightShift += 10) {
			HoughExperiments he = HoughExperiments.f(file, tr, smp, heightShift, goodHeadEdge, false);
			
			
			left = Math.min(left, he.traceFrom);
			right = Math.max(right, he.traceTo);
			bottom = Math.max(bottom, he.lastSmp);
			
			set.add(he);
		}
		
		return set;
	}
	
	
	

}
