package com.ugcs.gprvisualizer.gpr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.ugcs.gprvisualizer.math.DronMath;
import com.ugcs.gprvisualizer.math.EcefLla;
import com.ugcs.gprvisualizer.math.GPSPoint;
import com.ugcs.gprvisualizer.math.Point3D;

import Jama.Matrix;

public class CoordinateManager {
	
	private	Settings settings;
	private	Model model;
	private DepthSpectrum depthSpectrum;
	
	public CoordinateManager(Settings settings, Model model) {
		this.settings = settings;
		this.model = model;
		
		
		depthSpectrum = new DepthSpectrum(settings);
	}
	
	public Scan getSelectedScan(){
		if(settings.selectedScanIndex >= 0) {
			return model.getScans().get(settings.selectedScanIndex);
		}
		return null;
	}
	
	public void filter(){
		
		System.out.println("start filter");
		new ConstNoiseRemoveFilter().execute(model.getScans());
		System.out.println("finish filter");		
	}
	
	
	public void imgProfile(){
		int width = 2048;
	    int height = 1024;
	    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = bufferedImage.createGraphics();
	    g2d.setColor(Color.white);
	    g2d.fillRect(0, 0, width, height);
	    
	    
	    int trace = 467;
	    float kf= 14;
	    
	    float[] values = model.getScans().get(trace).values;
	    
	    g2d.setColor(Color.black);
	    for(int i=1; i< values.length; i++){
	    	int i0 = i-1;
	    	
	    	int x1 = i0*4;
	    	int y1 = (int)(values[i0]/kf) + height/2;
	    	int x2 = i*4;
	    	int y2 = (int)(values[i]/kf) + height/2;
	    	
			g2d.drawLine(x1, y1, x2, y2);
	    	
			if(i % 20 == 0){
				g2d.drawString(""+i, x1, 20);
			}
	    	
	    }
	    
	    saveImg(bufferedImage, "profile.png");
	}	
	

	private void saveImg(BufferedImage bufferedImage, String fname) {
		File file = new File(fname);
		try {
			ImageIO.write(bufferedImage, "png", file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DepthSpectrum getDepthSpectrum() {
		return depthSpectrum;
	}
}
