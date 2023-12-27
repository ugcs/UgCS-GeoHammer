package com.ugcs.gprvisualizer.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.Sout;

public class HoughExperimentsAnalizer {
	
	public static final int SHIFT_TO = 240;
	public static final int SHIFT_FROM = -30;
	public boolean print = false;
	SgyFile file;
	
	int left;
	int right;
	int bottom;
	
	
//	int headLow;
//	int headHi;
//	int foundEdge = 0;

	
	public HoughExperimentsAnalizer(SgyFile file) {
		this.file = file;
	}
	
			

	public boolean analize(int tr, int smp) {
		
		
		//1 
		List<HoughExperiments> l = findGoodHypList(tr, smp);
		
		return !l.isEmpty();
		
	}

	public List<HoughExperiments> findGoodHypList(int tr, int smp) {
		
		if (!findHeaderEdge2(tr, smp)) {
			if (print) {
				Sout.p("~~ !findHeaderEdge2");
			}
			return Collections.emptyList();
		}
		
		for (Header hdr : foundHeader) {
			List<HoughExperiments> l = initHE(tr, smp, hdr);
			
			if (l.isEmpty()) {
				continue;
			}

			addPoints(l, tr, left, right, smp + 1, bottom, hdr.edge);
			 
			filterByGoodBadCount(l);

			if (!l.isEmpty()) {
				return l;
			}
			
			
		}
		
		if (print) {
			Sout.p("~~ all filtered");
		}
		
		return Collections.emptyList();
	}

	private void filterByGoodBadCount(List<HoughExperiments> l) {
		
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
	
	static class Header {
		int edge;
		int headLow;
		int headHi; 	
		
		public Header(int edge, int headLow, int headHi) {
			this.edge = edge;
			this.headLow = headLow;
			this.headHi = headHi; 				
		}
	}

	List<Header> foundHeader = new LinkedList<>();
	
	public boolean findHeaderEdge2(int tr, int smp) {

		
		//foundEdge = 0;
		foundHeader.clear();
		
		boolean f = false;
		
		int mintr = (tr - new HoughExperiments(file, tr, smp, SHIFT_FROM, 0).get1stRowLeft()) * 75 / 100;
		int maxtr = (tr - new HoughExperiments(file, tr, smp, SHIFT_TO, 0).get1stRowLeft()) * 130 / 100;
		
		
		for (int edge = 1; edge <= 4; edge++) {
			
			int leftc =  tr - find(-1, edge, tr, smp);
			int rightc = find(+1, edge, tr, smp) - tr;
			
			if (print) {
				Sout.p("mn: " + mintr + " l: " + leftc + " r: " + rightc + " mx: " + maxtr );
			}
			
			if (leftc < mintr || rightc < mintr || leftc > maxtr || rightc > maxtr) {
				continue;
			}
			
			if (bigDiff(leftc, rightc)) {
				continue;
			}
			
			
			// shift - size
			//headLow = ;
			//headHi = ;
			//foundEdge = edge;
			
			foundHeader.add(new Header(edge, Math.min(leftc, rightc), Math.max(leftc, rightc)));	
			f = true;
		}
		
		return f;
	}
	
	private int find(int step, int edge, int tr, int smp) {
		int gap = 0;
		int index = tr;
		int lastgood = tr;
		while (gap < 2 && index >=0 && index < file.size()) {
			if (file.getEdge(index, smp) == edge || file.getEdge(index, smp - 1) == edge) {
				gap = 0;
				lastgood = index;
			} else {
				gap++;
				
			}
			index += step;
			
		}
		
		return lastgood;
	}

	private boolean bigDiff(int left, int right) {
		
		return left > right * 2 || right > left * 2;
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
		
		//if (print) { Sout.p("hdr edge val " + val);}
		
		if (val > r * 2 * 3 / 4) {
			return bestIndex;
		}
		
		return 0;
	}

	private void addPoints(List<HoughExperiments> l, int tr, int left, int right, int top, int bottom, int goodHeadEdge) {
		for (int h = top; h < bottom; h++) {
			for (int w = tr; w >= left; w--) {
				checkPoint(l, w, h, goodHeadEdge);
			}
			for (int w = tr; w <= right; w++) {
				checkPoint(l, w, h, goodHeadEdge);
			}
		}
		
	}

	private void checkPoint(List<HoughExperiments> l, int w, int h, int goodHeadEdge) {
		if (file.getTraces().get(w).edge[h] == goodHeadEdge) {
			for (HoughExperiments he : l) {
				he.addPoint(w, h);
			}
		}
		
	}
	
	List<HoughExperiments> set = new LinkedList<>();
	
	long fulltm = 0;
	long cr1count = 0;
	long cr1badcount = 0;
	private List<HoughExperiments> initHE(int tr, int smp, Header hdr) {
		
		
		prepareList();		
		
		left = tr;
		right = tr;
		bottom = smp;
		for (double heightShift = SHIFT_FROM; heightShift < SHIFT_TO; heightShift += 5) {
				
			HoughExperiments he = new HoughExperiments(file, tr, smp, heightShift, hdr.edge);
			
			if (!he.criteriaHead(hdr.headLow, hdr.headHi)) {
				cr1badcount++;
				continue;
			}
			cr1count++;
			
			he.init();
			
			left = Math.min(left, he.traceFrom);
			right = Math.max(right, he.traceTo);
			bottom = Math.max(bottom, he.lastSmp);
			
			set.add(he);
		}
		
		
		
		return set;
	}

	

	private void prepareList() {
		set.clear();
	}
	
	
	
	
//	public HoughExperiments createHE(SgyFile file, int tr, int smp, double heightShift, int lookingEdge,
//			boolean print) {
//		
//		HoughExperiments he = new HoughExperiments(file, tr, smp, heightShift, lookingEdge);
//		he.print = print;
//
//		//long tm = System.currentTimeMillis();	
//		
//		he.init();
//		
//		//fulltm += (System.currentTimeMillis() - tm);
//		
//		return he;
//	}
	
	
	
	public HoughExperiments debug(int tr, int smp, double heightShift) {
		print = true;
		List<HoughExperiments> st = findGoodHypList(tr, smp);
		
		
		int goodHeadEdge;
		
		if (st.isEmpty()) {
			Sout.p("~~ head criteria false");
			goodHeadEdge = findHeaderEdge(tr, smp);
		} else {
			HoughExperiments hp = st.iterator().next();
			heightShift = hp.shift;
			goodHeadEdge = hp.lookingEdge;
		}
		
		Sout.p("goodHeadEdge     = " + goodHeadEdge + "      shift = " + heightShift);
		if (goodHeadEdge == 0) {
			
			goodHeadEdge = 4;
		}
		
		
		left = tr;
		right = tr;
		bottom = smp;
		HoughExperiments he = new HoughExperiments(file, tr, smp, heightShift, goodHeadEdge);
		he.print = true;
				
		he.init();
		
		left = Math.min(left, he.traceFrom);
		right = Math.max(right, he.traceTo);
		bottom = Math.max(bottom, he.lastSmp);		
		
		//Sout.p(" l " + left + " r " + right + "  b " + bottom );
		
		List<HoughExperiments> s = new ArrayList<>();
		s.add(he);
		
		addPoints(s, tr, left, right, smp + 1, bottom, goodHeadEdge);
		
		
		
	
		return he;
	}
			


}
