package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Point2D;

public class Settings {

	
	public Point2D middleLatLonDgr;
	public Point2D middleLatLonRad;
	
	public int maxsamples = 0;
	
	public int width = 800;
	public int height = 600;
	public int radius = 15;
	public int hpage = 7;
	public int layer = 20; 
    
	public int topscale = 200;
	public int bottomscale = 250;
	public int cutscale = 100;
	public int zoom = 100;
    
	public boolean showpath = true;
	public boolean autogain = false;
	
	public int threshold = 1000;
    
	public int distBetweenTraces = 50;
	public int selectedScanIndex = 200;
	public double kf;
	public double stx;
	public double sty;
	
	
	public int widthZoomKf = 25;
	public int heightZoomKf = 600;
	public int heightStart = 0;
    
	public int getWidth() {
		return (int)(width * zoom / 100.0);
	}
	public int getHeight() {
		return (int)(height * zoom / 100.0);
	}
	
}
