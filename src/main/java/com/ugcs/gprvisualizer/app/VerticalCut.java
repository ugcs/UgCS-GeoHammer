package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.function.Consumer;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PaletteBuilder;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class VerticalCut {

	private Model model;
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private int[] palette = new PaletteBuilder().build();
	
	public VerticalCut(Model model) {
		this.model = model;
		
	}
	
	public void recalc() {
		controller.render(null);
	}
		
	public Scene build() {
		
		BorderPane bPane = new BorderPane();   
		//bPane.setTop(); 
		//bPane.setBottom(prepareStatus()); 
		//bPane.setRight(getToolPane()); 
		bPane.setCenter(imageView);
		
		Scene scene = new Scene(bPane, 1024, 768);
		
		return scene;
	}
	
	
	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel obj) {

			if(model.getScans() == null) {
				return;
			}
				
			img = render2_spektr();
			
			Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
				    Image i = SwingFXUtils.toFXImage(img, null);
				    imageView.setImage(i);
	            }
	          });      				
		}
	});

	protected BufferedImage render() {
		
		int width = 1280;
		int height = 1024;//model.getSettings().maxsamples;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    Graphics2D g2 = (Graphics2D)image.getGraphics();
	    
	    
	    
	    int rangx = 10;
	    for(int x=-rangx; x<rangx; x++){
	    	
    		int scanNum = model.getSettings().selectedScanIndex + x;
    		if(scanNum<0 || scanNum >= model.getScans().size()) {
    			continue;
    		}

    		//step between graphics
    		int startx = width/2 + x * 300;
    		
    		g2.setColor(Color.LIGHT_GRAY);
    		g2.drawLine(startx,0, startx, height);
    		
    		g2.setColor(Color.DARK_GRAY);
    		drawGraph(model.getScans().get(scanNum).originalvalues, startx, g2);
    		g2.setColor(Color.WHITE);
    		drawGraph(model.getScans().get(scanNum).values, startx, g2);
    				
	    }

	    
	    
	    return image;
	}

	static int kfy = 2;
	
	protected void drawGraph(float[] values, int startx, Graphics2D g2) {
		
		int x1 = 0;
		int maxy = values.length;
    	for(int y=0; y<maxy; y++){
    		
    		float val = values[y];	    		
    		
    		int x2 = (int)(val / 200);
    		if(y != 0) {
    			g2.drawLine(startx + x1, (y-1)*kfy, startx + x2, y*kfy);
    		}
    		x1 = x2;
    	}
		
	}
	
	protected BufferedImage render2_spektr() {
		
		int width = 1024;
		int height = 768;//model.getSettings().maxsamples;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    for(int x=0; x<width; x++){
    		int scanNum = model.getSettings().selectedScanIndex - width/2 + x;
    		if(scanNum<0 || scanNum >= model.getScans().size()) {
    			continue;
    		}

    		float[] values = model.getScans().get(scanNum).values;
	    	for(int y=0; y<values.length; y++){
	    		
	    		int val = (int)(values[y]/50);
	    		val = Math.max(0, val);
	    		val = Math.min(palette.length-1, val);
	    		buffer[x + y * width] = palette[val];
	    	
	    	}
	    }

	    
	    
	    return image;
	}
	
}
