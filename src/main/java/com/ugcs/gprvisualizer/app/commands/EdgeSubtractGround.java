package com.ugcs.gprvisualizer.app.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class EdgeSubtractGround implements Command {

	Model model = AppContext.model;
	//List<Trace> list;
	
	int etsum[] = new int [5];
	
	static class EdgeCoord{
		int index;
		int smp;
		
		public EdgeCoord(int index, int smp) {
			this.index = index;
			this.smp = smp;
		}
	}
	
	class EdgeQueue {
		final int MAX_SIZE = 60;
		final int ENOUGH_SIZE = 45;
		
		private final List<Trace> list;
		private final int edgeType;
		
		private int foundCount=0;
		private int lastRemoved = -1;
		private Queue<EdgeCoord> queue = new LinkedList<EdgeCoord>();
		
		public EdgeQueue(List<Trace> list, int edgeType) {
			this.edgeType = edgeType;
			this.list = list;
		}
		
		void in(int index, int smp) {
			queue.add(new EdgeCoord(index, smp));
			foundCount++;			
		}

		public void clearGroup(int index) {
			
			//			
			int tailLimit = index-MAX_SIZE;
			if(foundCount > ENOUGH_SIZE) {
				int removeFromIndex = Math.max(lastRemoved+1, tailLimit);
				
				//clear edges from removeFromIndex to index
				//for(int i=removeFromIndex; i<=index; i++) {
				for(EdgeCoord ec : queue) {
					if(ec.index >= removeFromIndex) {
						
						//list.get(ec.index).edge[ec.smp] = 0;
						list.get(ec.index).good[ec.smp] = 1;
					}					
				}
				
				lastRemoved = index;
			}			
		}
		
		public void out(int index) {
			int tailLimit = index-MAX_SIZE;
			while(!queue.isEmpty() && queue.peek().index < tailLimit) {
				queue.remove();
				foundCount--;
			}
		}				
	}	
	
	public EdgeSubtractGround () {

	}

	static final int MARGIN = 5;
	@Override
	public void execute(SgyFile file) {
		System.out.println("subtact profiles for " + file.getFile().getName());
		
		//int maxGroundDeep = getMaxGroundSmp(file);
		
		if(file.groundProfile == null) {
			System.out.println("!!!!!!!!!!!!!1 file.groundProfile == null");
			return;
		}
		
		List<HorizontalProfile> hplist = new ArrayList<>();
		//straight horizontal line
		hplist.add(getHorizontal(file.getTraces().size()));
		
		// ground profile
		hplist.add(file.groundProfile);
		
		// ground profile * 2
		hplist.add(multTwice(file.groundProfile));
		
		for(HorizontalProfile hp : hplist) {
			
			int from = -hp.minDeep+MARGIN;
			int to = file.getMaxSamples()-hp.maxDeep-MARGIN;
			
			for(int deep=from; deep < to; deep++) {			
				
				processDeep(file, hp,  deep);
			}
		}
		
		//real clear
		for(Trace trace : file.getTraces()){
			for(int smp=0; smp<trace.good.length; smp++) {
			
				if(trace.good[smp] == 1) {
					trace.edge[smp] = 0;
				}				
			}
		}
		
		
	}

	private HorizontalProfile multTwice(HorizontalProfile groundProfile) {
		HorizontalProfile hp = new HorizontalProfile(groundProfile.deep.length);
		
		for(int i=0; i< groundProfile.deep.length; i++) {
			hp.deep[i] = groundProfile.deep[i]*2;
		}
		
		hp.finish(null);
		
		return hp;
	}

	private HorizontalProfile getHorizontal(int size) {

		HorizontalProfile hp = new HorizontalProfile(size);
		
		hp.finish(null);
		
		return hp;
	}

	public int getMaxGroundSmp(SgyFile file) {
		int maxGroundDeep = 0;
		for(Trace trace : file.getTraces()){
			maxGroundDeep = Math.max(maxGroundDeep, trace.maxindex);
		}
		return maxGroundDeep;
	}

	private void processDeep(SgyFile file, HorizontalProfile hp, int shift) {
		List<Trace> list = file.getTraces();
		
		EdgeQueue[] edges = createQueuesForEdgeType(list);
		
		for(int i=0; i<list.size(); i++) {
			
			Trace trace = list.get(i);
			int smp = hp.deep[i] + shift;
			
			// 1 2 3 4 / check range from 1 above to 1 below
			
			for(int r=0; r<=1; r++) {
				int realsmp = smp+r;
				int edv = trace.edge[realsmp];
				
				edges[edv].in(i, realsmp);
			}
			
			/// remove
			for(int et=1; et<=4; et++) {
				edges[et].clearGroup(i);
			}
			
			//types of edge
			for(int et=1; et<=4; et++) {
				
				edges[et].out(i);
			}
		}		
	}

	public EdgeQueue[] createQueuesForEdgeType(List<Trace> list) {
		EdgeQueue[] edges = new EdgeQueue[5];
		for(int i=0; i<edges.length; i++) {
			edges[i] = new EdgeQueue(list, i);
		}
		return edges;
	}



	@Override
	public String getButtonText() {

		return "Filter edges";
	}

	@Override
	public Change getChange() {

		return Change.justdraw;
	}
	
}
