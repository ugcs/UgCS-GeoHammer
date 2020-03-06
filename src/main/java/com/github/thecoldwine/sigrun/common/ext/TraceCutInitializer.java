package com.github.thecoldwine.sigrun.common.ext;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.bcel.internal.classfile.Field;
import com.ugcs.gprvisualizer.gpr.Model;

public class TraceCutInitializer {

	
	
	public List<LatLon> initialRect(Model model,  List<Trace> traces){
		
		
		LatLon c = model.getField().getSceneCenter();
		MapField f = new MapField(model.getField());
		f.setZoom(30);
		
		List<Point2D> scrpos = new ArrayList<>(); 
		for(Trace trace : traces) {
			scrpos.add( f.latLonToScreen(trace.getLatLon()));
		}
		
		//double 
		
		
		
		List<LatLon> points = new ArrayList<>();
		
		
		return points;
	}
	
	
}
