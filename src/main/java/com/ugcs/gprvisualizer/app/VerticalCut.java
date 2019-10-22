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
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.ui.BaseSlider;
import com.ugcs.gprvisualizer.ui.DepthSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class VerticalCut {

	private Model model;
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private int[] palette = new PaletteBuilder().build();
	
	private BaseSlider widthZoomSlider;
	private BaseSlider heightZoomSlider;
	private BaseSlider heightStartSlider; 

	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			recalc();
		}
	};
	
	public VerticalCut(Model model) {
		this.model = model;
		
		widthZoomSlider = new WidthZoomSlider(model.getSettings(), sliderListener);
		heightZoomSlider = new HeightZoomSlider(model.getSettings(), sliderListener);
		heightStartSlider = new HeightStartSlider(model.getSettings(), sliderListener);
	}
	
	public void recalc() {
		controller.render(null);
	}
		
	public Scene build() {
		
		BorderPane bPane = new BorderPane();   
		//bPane.setTop(); 
		//bPane.setBottom(prepareStatus()); 
		bPane.setRight(getToolPane()); 
		bPane.setCenter(imageView);
		
		Scene scene = new Scene(bPane, 400, 300);
		
		
		return scene;
	}
	
	private Node getToolPane() {
		VBox vBox = new VBox(); 
		vBox.setPadding(new Insets(3, 13, 3, 3));
		
		vBox.getChildren().add(widthZoomSlider.produce());
		vBox.getChildren().add(heightZoomSlider.produce());
		vBox.getChildren().add(heightStartSlider.produce());
		
		//widthZoomSlider.updateUI();
		//vBox.getChildren().add(distSlider.produce());
		return vBox;
	}
	
	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel obj) {

			if(model.getScans() == null) {
				return;
			}
				
			//img = render2_spektr();
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
		
		int width = 1280;
		int height = 1024;//model.getSettings().maxsamples;
		
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    
	    Graphics2D g2 = (Graphics2D)image.getGraphics();
	    
	    
	    
	    int rangx = 4;
	    for(int x=-rangx; x<rangx; x++){
	    	System.out.print("+");
    		int scanNum = model.getSettings().selectedScanIndex + x;
    		if(scanNum<0 || scanNum >= model.getScans().size()) {
    			continue;
    		}

    		//step between graphics
    		int startx = width/2 + x * 350;
    		
    		g2.setColor(Color.CYAN);
    		g2.drawLine(startx,0, startx, height);
    		
    		g2.setColor(Color.DARK_GRAY);
    		drawGraph(model.getScans().get(scanNum).originalvalues, startx, g2);
    		g2.setColor(Color.RED);
    		drawGraph(model.getScans().get(scanNum).values, startx, g2);
    				
	    }

	    
	    
	    return image;
	}

	
	
	protected void drawGraph(float[] values, int startx, Graphics2D g2) {
	
		float kfy = (float)model.getSettings().heightZoomKf / 100.0f;
		int x1 = 0;
		int heightstart = model.getSettings().heightStart;
		int maxy = values.length - heightstart;
    	for(int y=0; y<maxy; y++){
    		
    		float val = values[y + heightstart];
    		
    		int x2 = (int)(val / (float)model.getSettings().widthZoomKf);
    		if(y != 0) {
    			g2.drawLine(startx + x1, (int)((y-1)*kfy), startx + x2, (int)(y*kfy));
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

	public class WidthZoomSlider extends BaseSlider {
		
		public WidthZoomSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "width zoom";
			units = "%";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(200);		
			slider.setMin(1);
			slider.setValue(settings.widthZoomKf);
		}
		
		public int updateModel() {
			settings.widthZoomKf = (int)slider.getValue();
			return settings.widthZoomKf;
		}
	}

	public class HeightZoomSlider extends BaseSlider {
		
		public HeightZoomSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "height zoom";
			units = "%";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(1500);		
			slider.setMin(10);
			slider.setValue(settings.heightZoomKf);
		}
		
		public int updateModel() {
			settings.heightZoomKf = (int)slider.getValue();
			return settings.heightZoomKf;
		}
	}
	public class HeightStartSlider extends BaseSlider {
		
		public HeightStartSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "start";
			units = "samples";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(400);		
			slider.setMin(0);
			slider.setValue(settings.heightStart);
		}
		
		public int updateModel() {
			settings.heightStart = (int)slider.getValue();
			return settings.heightStart;
		}
	}
	
}
