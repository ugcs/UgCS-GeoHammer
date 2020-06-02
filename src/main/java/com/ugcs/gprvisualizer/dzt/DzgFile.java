package com.ugcs.gprvisualizer.dzt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.Sout;

/**
 * satellite file with GPS coorinates.
 * @author Kesha
 *
 */
public class DzgFile {

	static class Item {
		String line1;
		String line2;
		LatLon ll;
	}
	
	private Map<Integer, Item> traceToLatLonMap = new HashMap<>();
	private NavigableSet<Integer> indexSet = new TreeSet<Integer>();
	
	private boolean exist = false;
	private boolean necessaryToSave = false;
	
	public DzgFile cut(int from, int to) {
		
		DzgFile dzgFile = new DzgFile();
		dzgFile.necessaryToSave = true;
		
		Integer start = indexSet.floor(from);
		if (start == null) {
			start = from;
		}
		Integer finish = indexSet.ceiling(to);
		if (finish == null) {
			finish = to;
		}

		
		for (int i = start; i <= finish; i++) {
			if (indexSet.contains(i)) {
				
				
				Item item = traceToLatLonMap.get(i);
				
				int cutindex = i-from;
				dzgFile.indexSet.add(cutindex);
				dzgFile.traceToLatLonMap.put(cutindex, item);
			}
		}
		
		return dzgFile;
	}
	
	public LatLon getLatLonForTraceNumber(int trace) {
		
		if (!exist) {
			return null;
			//new LatLon(71.7601908, 53.1166144 + trace * 0.0001);
		}
		
		
		Integer i1 = indexSet.floor(trace);
		Integer i2 = indexSet.higher(trace);
		
		Item l1 = traceToLatLonMap.get(i1);
		Item l2 = traceToLatLonMap.get(i2);
		
		//LatLon l1 = ;
		//LatLon l2 = ;
		
		if (l1 == null || (i2 != null && i2 == trace)) {
			return l2.ll;
		}
		if (l2 == null || (i1 != null && i1 == trace)) {
			return l1.ll;
		}
		
		double kf = ((double) (trace - i1)) / ((double) (i2 - i1));
		return new LatLon(
				l1.ll.getLatDgr() + (l2.ll.getLatDgr() - l1.ll.getLatDgr()) * kf,   
				l1.ll.getLonDgr() + (l2.ll.getLonDgr() - l1.ll.getLonDgr()) * kf
				);
		
	}	
	
	
	public void load(File file) {
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			
			String line = reader.readLine();
			while (line != null) {
				
				processLine(line);
				
				line = reader.readLine();
			}			
			
			reader.close();
			
			exist = true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void save(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			
			for (Integer i : indexSet) {
				Item ll = traceToLatLonMap.get(i);
				
				String[] v = ll.line1.split(",");
				
				v[1] = ""+i;
				
				writer.append(String.join(",", v));
				writer.newLine();					
				writer.append(ll.line2);
				writer.newLine();
				writer.newLine();
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void processLine(String line) {
		
		if (line.startsWith("$GSSIS")) {
			
			int trace = Integer.valueOf(line.split(",")[1]);
			indexSet.add(trace);
			
			Item item = new Item();
			item.line1 = line;
			
			traceToLatLonMap.put(indexSet.last(), item);
			
			//Sout.p("t " + trace);
			
		} else if (line.startsWith("$GPGGA")) {
			
			//Gps gps = coordinates.get(coordinates.size() - 1);
			
			String[] values = line.split(",");
			
			//$GPGGA,231444.00, 6329.70208479,N, 14540.97532375,W ,1,7,1.6,1434.359,M,7.543,M,,*44
			
			double northSouth = ("N".equals(values[3]) ? 1 : -1);
			double westEast = ("E".equals(values[5]) ? 1 : -1);
			
			double lat = Double.valueOf(values[2]) * northSouth;
			double lon = Double.valueOf(values[4]) * westEast;
			
			double rlon = SgyFile.convertDegreeFraction(lon);
			double rlat = SgyFile.convertDegreeFraction(lat);
			
			LatLon ll = new LatLon(rlat, rlon);

			Item item = traceToLatLonMap.get(indexSet.last());
			item.line2 = line;
			item.ll = ll;			
			
			//Sout.p("g " + ll.toString());
		}
	}	

	public boolean isNecessaryToSave() {
		return necessaryToSave;
	}
	
}
