package com.ugcs.gprvisualizer.app;

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
		
		Scene scene = new Scene(bPane, 400, 512);
		
		return scene;
	}
	
	
	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel obj) {

			if(model.getScans() == null) {
				return;
			}
				
			img = render();
			
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
		
		int width = 300;
		int height = model.getSettings().maxsamples;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData() ;	    
	    
	    for(int x=0; x<width; x++){
    		int scanNum = model.getSettings().selectedScanIndex - width/2 + x;
    		if(scanNum<0 || scanNum >= model.getScans().size()) {
    			continue;
    		}

	    	for(int y=0; y<height; y++){
	    		
	    		int val = (int)(model.getScans().get(scanNum).values[y]/50);
	    		val = Math.max(0, val);
	    		val = Math.min(palette.length-1, val);
	    		buffer[x + y * width] = palette[val];
	    	
	    	}
	    }
	    
	    return image;
	}
	
}
