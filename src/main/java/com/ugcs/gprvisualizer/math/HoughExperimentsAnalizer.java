package com.ugcs.gprvisualizer.math;

import java.util.Collection;
import java.util.Collections;
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
		
		Set<HoughExperiments> st = findGoodHypList(tr, smp);
		
		
		print = true;
		int goodHeadEdge;
		
		if (st.isEmpty()) {
			goodHeadEdge = findHeaderEdge(tr, smp);
		} else {
			HoughExperiments hp = st.iterator().next();
			heightShift = hp.shift;
			goodHeadEdge = hp.lookingEdge;
		}
		
		Sout.p("goodHeadEdge = " + goodHeadEdge + "  shift = " + heightShift);
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
		
		
		
	
		return he;
	}
			
			

	public boolean analize(int tr, int smp) {
		
		
		//1 
		Set<HoughExperiments> l = findGoodHypList(tr, smp);
		
		return !l.isEmpty();
		
	}

	public Set<HoughExperiments> findGoodHypList(int tr, int smp) {
		int goodHeadEdge = findHeaderEdge(tr, smp);
		
		if (goodHeadEdge == 0) {
			return Collections.EMPTY_SET;
		}
		
		Set<HoughExperiments> l = initHE(tr, smp, goodHeadEdge);
		
		
		addPoints(l, tr, left, right, smp + 1, bottom, goodHeadEdge);
		 
		filterByGoodBadCount(l);
		return l;
	}

	private void filterByGoodBadCount(Set<HoughExperiments> l) {
		
		Iterator<HoughExperiments> it = l.iterator();
		while (it.hasNext()) {
			HoughExperiments he = it.next();
			
			if(he.criteriaGoodCount()
				&& he.criteriaRealWidth()
				&& he.criteriaRealMinLeft()
				&& he.criteriaRealMinRight()
				&& he.criteriaRealMinHight()
				&& he.criteriaGoodBadRatio()) {
				//good
			} else {
				it.remove();			
				
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
		for (double heightShift = -30; heightShift < 160; heightShift += 5) {
			HoughExperiments he = HoughExperiments.f(file, tr, smp, heightShift, goodHeadEdge, false);
			
			
			left = Math.min(left, he.traceFrom);
			right = Math.max(right, he.traceTo);
			bottom = Math.max(bottom, he.lastSmp);
			
			set.add(he);
		}
		
		return set;
	}
	
	
	

}
