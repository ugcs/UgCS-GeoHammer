package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Point2D;

public class Settings {

	public enum RadarMapMode {
		AMPLITUDE,
		SEARCH
	}
	
	public RadarMapMode radarMapMode = RadarMapMode.AMPLITUDE;
	
	public Point2D middleLatLonDgr;
	public Point2D middleLatLonRad;
	
	public boolean isRadarMapVisible = true;
	
	public int center_box_width; 
	public int center_box_height;
	
	public int maxsamples = 400;
	
	public int width = 800;
	public int height = 600;
	public int radius = 15;
	public int hpage = 47;
	public int layer = 40; 
	
	public boolean hyperliveview = false;
	public int hyperkfc = 27; 
	public int hypergoodsize = 150;
	
	public int topscale = 200;
	public int bottomscale = 250;
	public int cutscale = 100;
	public int zoom = 100;
    
	public boolean showpath = true;
	public boolean autogain = true;
	
	public int threshold = 0;
    
	public int distBetweenTraces = 10;
	public int selectedScanIndex = 1;
	public double kf;
	public double stx;
	public double sty;
	
	
	public int widthZoomKf = 30;
	public int heightZoomKf = 100;
	public int heightStart = 0;
    
	public int getWidth() {
		return (int)(width * zoom / 100.0);
	}
	public int getHeight() {
		return (int)(height * zoom / 100.0);
	}
	
}
