package com.ugcs.gprvisualizer.gpr;

import java.util.ArrayList;

import com.ugcs.gprvisualizer.draw.LocalScan;
import com.ugcs.gprvisualizer.math.Point3D;

public class ScreenCoordinatesCalculator {

	private Model model;
	public ScreenCoordinatesCalculator(Model model) {
		this.model = model;
	}
	
	public void calcLocalCoordinates(){
		
		double mdlWidth = model.getBounds().width;
		double mdlHeight = model.getBounds().height;

		double minX = model.getBounds().x;
		double minY = model.getBounds().y;
		
		double wx = model.getSettings().getWidth();
		double wy = model.getSettings().getHeight();
		
		double kf;
		if( wx/wy > mdlWidth/mdlHeight ) {
			kf = wy / mdlHeight; 
		}else {
			kf = wx / mdlWidth;
		}
		kf = kf*0.92;
		
		model.getSettings().kf = kf;
		double stx = (wx - mdlWidth * kf) / 2;
		double sty = (wy - mdlHeight * kf) / 2;
		model.getSettings().stx = stx;
		model.getSettings().sty = sty;
		
		model.setLocalScans(new ArrayList<>());
		
	    for(Scan scan : model.getScans()){
	    	Point3D p = scan.point;
			
	    	double cx = (p.x - minX) * kf;
	    	double cy = (p.y - minY) * kf;
	    	
	    	int dx = (int)(wx - cx - stx );
	    	int dy = (int)(wy - cy - sty );
	    	
	    	model.getLocalScans().add(new LocalScan(scan, dx, dy, false));
	    }
		
	}
	
	public void findNearestScan(int scrx, int scry) {
		double localX = scrx;
		double localY = scry;
		
		double currentDist = -1;
		int selectedScanIndex = -1; 
		
		for(int i = 0; i < model.getLocalScans().size(); i++) {
			LocalScan scan = model.getLocalScans().get(i);
			
			double dist = getDist2(localX, localY, scan.getLocalX(), scan.getLocalY());
			if(selectedScanIndex == -1 || dist < currentDist) {
				selectedScanIndex = i;
				currentDist = dist;
			}
		}
		model.getSettings().selectedScanIndex = selectedScanIndex;
		
	}

	private double getDist(double localX, double localY, int localX2, int localY2) {
		return Math.sqrt(getDist2(localX, localY, localX2, localY2));
	}
	
	private double getDist2(double localX, double localY, int localX2, int localY2) {
		
		double x = (localX - localX2);
		double y = (localY - localY2);
		
		return x*x + y*y;
	}
	

	
}
