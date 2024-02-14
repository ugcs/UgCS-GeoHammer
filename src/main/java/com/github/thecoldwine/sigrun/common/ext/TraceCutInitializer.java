package com.github.thecoldwine.sigrun.common.ext;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.gpr.Model;

public class TraceCutInitializer {

	
	
	public List<LatLon> initialRect(Model model,  List<Trace> traces){
		
		
		//LatLon c = model.getField().getPathCenter();
		MapField f = new MapField(model.getField());
		f.setZoom(30);
		
		
		
		List<Point2D> scrpos = new ArrayList<>();
		double sx = 0;
		double sy = 0;
		for (Trace trace : traces) {
			
			Point2D p = f.latLonToScreen(trace.getLatLon());
			sx += p.getX();
			sy += p.getY();
			scrpos.add(p);
		}
		Point2D center = new Point2D.Double(sx / traces.size(), sy / traces.size());
		
		double maxrad = 0;
		//double
		int[] angcount = new int[180];
		for (int i = 50; i < scrpos.size(); i++) {
			Point2D p1 = scrpos.get(i - 50);
			Point2D p2 = scrpos.get(i);
			
			double ang = Math.atan2(p1.getY() - p2.getY(), p1.getX() - p2.getX());
			int angdgr =  (int) (ang * 180 / Math.PI);
  
			for (int j = -9; j <= 9; j++) {
				angcount[(j + angdgr + 360) % 180]++;
			}
			
			
			maxrad = Math.max(maxrad, center.distance(p1));
		}
		int maxindex = 0;
		for (int i = 0; i < 180; i++) {			
			if (angcount[maxindex] < angcount[i]) {
				maxindex = i;
			}
		}
		
		System.out.println(" best degree " + maxindex);
		
		double ang = ((double) maxindex + 45) * Math.PI / 180.0; 
		
		List<LatLon> points = new ArrayList<>();
		
		points.add(f.screenTolatLon(new Point2D.Double(
				Math.cos(ang) * maxrad + center.getX(),
				Math.sin(ang) * maxrad + center.getY())));
		
		ang += Math.PI / 2;
		
		points.add(f.screenTolatLon(new Point2D.Double(
				Math.cos(ang) * maxrad + center.getX(),
				Math.sin(ang) * maxrad + center.getY())));
		
		ang += Math.PI / 2;
		
		points.add(f.screenTolatLon(new Point2D.Double(
				Math.cos(ang) * maxrad + center.getX(),
				Math.sin(ang) * maxrad + center.getY())));
		
		ang += Math.PI / 2;
				
		points.add(f.screenTolatLon(new Point2D.Double(
				Math.cos(ang) * maxrad + center.getX(),
				Math.sin(ang) * maxrad + center.getY())));
				
		points.add(1, getMiddleOf(points.get(0), points.get(1)));
		points.add(3, getMiddleOf(points.get(2), points.get(3)));		
		points.add(5, getMiddleOf(points.get(4), points.get(5)));		
		points.add(7, getMiddleOf(points.get(6), points.get(0)));				
		
		return points;
	}

	private LatLon getMiddleOf(LatLon latLon, LatLon latLon2) {
		return new LatLon((latLon.getLatDgr() + latLon2.getLatDgr()) / 2, (latLon.getLonDgr() + latLon2.getLonDgr()) / 2);
	}
	
}
