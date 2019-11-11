package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;

import com.sun.prism.paint.Color;
import com.ugcs.gprvisualizer.gpr.PaletteBuilder;
import com.ugcs.gprvisualizer.math.BackgroundRemovalFilter;
import com.ugcs.gprvisualizer.math.LevelFilter;
import com.ugcs.gprvisualizer.math.MovingAvg;

public class AmplitudeMatrix {

	float[][] matrix;
	float maxamp;
	List<Trace> trace;
	
	public void init(List<Trace> trace) {
		this.trace = trace;
		matrix = new float[trace.size()][];
		for(int i=0; i< trace.size(); i++) {
			matrix[i] = getAmpVector(trace.get(i).getNormValues());
		}		
	}

	private float[] getAmpVector(float[] values) {
		
		float[] res = new float[values.length];
		colls.add(new ArrayList<>());
		float prevval = 0;
		float max = values[0];
		float min = values[0];
		int zeroind = 0;
		int midind = 0;
		for(int i=0; i < values.length; i++) {
			
			float val = values[i];
			max = Math.max(max, val);
			min = Math.min(min, val);
			
			if(isCrosZero(val, prevval)) {
				midind = i;
			}
			if(isCrosZero(prevval, val)) {
				//float amp = ;
				
				fill(res, zeroind, i, max - min);
				colls.get(colls.size()-1).add(new Grp(zeroind, i, max - min, midind));
				zeroind = i;
				min = val;
				max = val;
			}
			prevval=val;
		}
		fill(res, zeroind, values.length, max - min);
		return res;
	}
	
	class Grp{
		public Grp(int start, int finish, float f, int midind) {
			this.start = start;
			this.finish = finish;
			this.amp = f;
			this.midind =  midind;
		}
		int start;
		int midind;
		int finish;
		float amp;
	}
	
	List<Grp> avgrow = new ArrayList<>();
	List<List<Grp>> colls = new ArrayList<>();
	List<List<Grp>> selected = new ArrayList<>();
	MovingAvg startAvg = new MovingAvg();
	MovingAvg finishAvg = new MovingAvg();
	
	int level[];
	
	public void findLevel() {
		List<Grp> startList = getOrderedDescGrps();
		List<List<Grp>> selected = new ArrayList<>();
		
		for(Grp start : startList) {
			selected.add(findLevelStartingAt(start));
		}
		
		List<Grp> selrow = findBestPath(selected);
		
		
		this.selected.add(selrow);
		
		
		level = new int[selrow.size()];
		
		for(int i=0; i< level.length; i++) {
			 int r = calcAvgHeight(selrow, i);
			 level[i] = r;
			 trace.get(i).maxindex = r;
			 trace.get(i).maxindex2 = r;
		}
	}

	private int calcAvgHeight(List<Grp> selrow, int i) {
		int R = 10;
		int from = i-R;
		from = Math.max(from, 0);
		
		int to = i+R;
		to = Math.min(to, selrow.size()-1);

		double sum = 0;
		double del = 0;
		for(int j=from; j<=to; j++) {
			sum += selrow.get(j).start;
			del += 1;
		}
		
		return (int)Math.round(sum/del);
	}

	private List<Grp> findBestPath(List<List<Grp>> selected) {
		List<Grp> selrow = null;
		long selsum = 0;
		for(List<Grp> row : selected) {
			long sum = getIndignation(row);
			if(selrow == null || sum < selsum) {
				selrow = row;
				selsum = sum;
			}			
		}
		return selrow;
	}
	
	private long getIndignation(List<Grp> row) {
		long res = 0;
		for(int i = 1; i< row.size(); i++) {
			res += Math.abs(row.get(i-1).start-row.get(i).start);
		}
		return res;
	}

	private List<Grp> getOrderedDescGrps() {
		List<Grp> r = new ArrayList(colls.get(0));
		Collections.sort(r, new Comparator<Grp>() {

			@Override
			public int compare(Grp o1, Grp o2) {
				
				return -Float.compare(o1.amp, o2.amp);
			}
		});
		
		
		return r.subList(0, 4);
	}

	List<Grp> findLevelStartingAt(Grp start) {
		
		List<Grp> selected = new ArrayList<>();
		selected.add(start);
		startAvg = new MovingAvg();
		startAvg.add(start.start);
		finishAvg = new MovingAvg();
		finishAvg.add(start.finish);
		
		int x=0;
		for(List<Grp> col : colls) {
			if(x==299) {
				System.out.println("stop");
			}
			
			Grp avggrp = getAvgGrp();
			avgrow.add(avggrp);//tmp
			List<Grp> grpcandidats = find(avggrp, col);
			
			Grp grp = selectbest(grpcandidats, selected);
			startAvg.add(grp.start);
			finishAvg.add(grp.finish);
			selected.add(grp);
			
			x++;
		}
		selected.remove(0);
		return selected;
	}

	private Grp selectbest(List<Grp> grpcandidats, List<Grp> selected) {
//		Grp last = selected.get(selected.size()-1);
//		
//		Grp r1 = findBestByAmp(grpcandidats);
//		
//		Grp r2 = findBestByCommon(grpcandidats, last);
//		
//		if(r1 == r2) {
//			return r1;
//		}
		Grp avggrp = getAvgGrp();
		
	
		return findBestByAmp(findBestByCommonLst(grpcandidats, avggrp));
	}

	private Grp getAvgGrp() {
		Grp avggrp = new Grp(startAvg.get(), finishAvg.get(), 2000, (startAvg.get() + finishAvg.get())/2);
		return avggrp;
	}

	private List<Grp> findBestByCommonLst(List<Grp> grpcandidats, Grp last) {
		List<Pair<Integer, Grp>> lst = new ArrayList<>();
		for(Grp grp : grpcandidats) {
			
			lst.add(Pair.of(com(grp, last), grp));
		}
		
		Collections.sort(lst, new Comparator<Pair<Integer, Grp>>(){

			@Override
			public int compare(Pair<Integer, Grp> arg0, Pair<Integer, Grp> arg1) {
				
				return -Integer.compare(arg0.getKey(), arg1.getKey());
			}			
		});
		
		List<Grp> res = new ArrayList<>();
		res.add(lst.get(0).getValue());
		if(lst.size() > 1 && 
			diffMinor(lst.get(0).getKey(), lst.get(1).getKey())) {
			
			res.add(lst.get(1).getValue());
		}
		return res;
	}
	
	private boolean diffMinor(Integer k1, Integer k2) {
		int thr = Math.max(k1, k2) / 3;
		return Math.abs(k1 - k2) <= thr;
		
	}

	private Grp findBestByCommon(List<Grp> grpcandidats, Grp last) {
		Grp r2 = grpcandidats.get(0);
		for(Grp grp : grpcandidats) {
			
			if(com(grp, last) > com(r2, last)) {
				r2 = grp;
			}
		}
		return r2;
	}

	private Grp findBestByAmp(List<Grp> grpcandidats) {
		Grp r1 = grpcandidats.get(0);
		for(Grp grp : grpcandidats) {
			
			
			if(grp.amp > r1.amp) {
				r1 = grp;
			}
		}
		return r1;
	}

	private int com(Grp g1, Grp g2) {
		
		return 
			Math.min(g1.finish, g2.finish) - 
			Math.max(g1.start, g2.start);
	}

	private List<Grp> find(Grp last, List<Grp> col) {
		List<Grp> result = new ArrayList<>();
		
		for(Grp grp : col) {
			if(isTouched(last, grp)) {
				result.add(grp);
			}
		}
		
		
		return result;
	}

	private boolean isTouched(Grp last, Grp grp) {

		return last.start >= grp.start && last.start <= grp.finish
				||
			   last.finish >= grp.start && last.finish <= grp.finish
			    ||
			   grp.start >= last.start && grp.start <= last.finish
				||
			   grp.finish >= last.start && grp.finish <= last.finish;
	}

	private void fill(float[] res, int zeroind, int to, float amp) {

		for(int i=zeroind; i<to; i++ ) {
			res[i] = amp; 
		}
		
		maxamp = Math.max(maxamp, amp);
	}

	private boolean isCrosZero(float prevval, float val) {
		
		return prevval < 0 && val >= 0;
	}

	public BufferedImage getImg() {
		int[] palette = new PaletteBuilder().build();
		int width = matrix.length;
		int height = matrix[0].length;
		System.out.println("dimension " + width + " " + height );
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    for(int x=0; x<width; x++){
	    	for(int y=0; y<height; y++){
	    	
	    		int  s = (int)(matrix[x][y]/50);
	    		s = Math.min(s, 2000);
	    		buffer[x + y * width] = palette[s];
	    	
	    	}
	    }
	    
	    int grn = Color.GREEN.getIntArgbPre();
	    int red = Color.RED.getIntArgbPre();
	    int levelColor = Color.BLUE.getIntArgbPre();
	    for(List<Grp> path : selected) {
	    	int x =0;
	    	for(Grp grp : path) {

	    		Grp ag = avgrow.get(x);
	    		buffer[x + (ag.start-1) * width] = red;
	    		buffer[x + (ag.finish) * width] = red;
	    		
	    		
	    		buffer[x + grp.start * width] = grn;
	    		buffer[x + (grp.finish-1) * width] = grn;
	    		//for(int y=grp.start; y< grp.midind; y++) {
	    		//	buffer[x + y * width] = grn;
	    		//}
	    		
	    		buffer[x + level[x] * width] = levelColor; 
	    		
	    		x++;
	    	}	    	
	    }
	    
	    return image;
		
	}
	
	
	public static void main(String [] args) throws Exception {
		//File file = new File("d:\\georadarData\\Gas pipes\\2019-07-24-10-43-52-gpr_processed\\2019-07-24-10-43-52-gpr_8.sgy");
		//File file = new File("d:\\georadarData\\Gas pipes\\2019-07-24-10-43-52-gpr_processed");
		//File file = new File("d:\\georadarData\\Greenland\\2018-06-29-22-36-37-gpr-shift_processed");
		File file = new File("d:\\georadarData\\normal soil 1Ghz\\2019-08-30-09-06-30-gpr_processed");
		File[] lst = file.listFiles(FileManager.filter);
		int cnt = 0;
		for(File sfile : lst) {
			execute(sfile, cnt++);
		}
		//File sfile = new File("d:\\georadarData\\Greenland\\2018-06-29-22-08-59-gpr-shift.sgy");		
		//execute(sfile, 0);
	}
	
	public static void execute(File file, int prefix) throws Exception {
		System.out.println(" " + file.getName() + "  ->  " + prefix);
		
		SgyFile sgyFile = new SgyFile();
		//sgyFile.open(new File("d:\\georadarData\\mines\\2019-08-29-12-48-48-gpr_0005.SGY"));
		sgyFile.open(file);
		
		BackgroundRemovalFilter lf = new BackgroundRemovalFilter();
		lf.removeConstantNoise(sgyFile.getTraces());
		
		AmplitudeMatrix m = new AmplitudeMatrix();
		m.init(sgyFile.getTraces());
		
		m.findLevel();
		
		BufferedImage img = m.getImg();
		
		String name = String.format("%02d_image.png", prefix);
		File outputfile = new File(name);
		ImageIO.write(img, "png", outputfile);
		
		//Runtime.getRuntime().exec(outputfile.getAbsolutePath());
		
		 Desktop.getDesktop().open(outputfile);
		
		
		
	}
	
}
