package com.ugcs.gprvisualizer.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage image;
    private int cx;
    private int cy;
    
    public void setImage(BufferedImage image){
    	this.image = image;
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        if(image != null){
            cx = ((int) this.getSize().getWidth() - image.getWidth())/2;
            cy = ((int) this.getSize().getHeight() - image.getHeight())/2;
        	
        	g.drawImage(image, cx, cy, this);
        }
    }
    
    public int getX() {
    	return cx;
    }

    public int getY() {
    	return cy;
    }

}