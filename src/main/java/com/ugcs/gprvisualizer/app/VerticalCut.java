package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.function.Consumer;

import com.github.thecoldwine.sigrun.common.ext.Trace;
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
import javafx.stage.Stage;

public class VerticalCut {

	private Model model;
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private int[] palette = new PaletteBuilder().build();
	
	private BaseSlider widthZoomSlider;
	private BaseSlider heightZoomSlider;
	private BaseSlider heightStartSlider; 
	private BaseSlider selectedTraceSlider; 
	private BaseSlider distBetweenTracesSlider;
	
	BorderPane bPane = new BorderPane();
	VBox vbox = new VBox();
	private Stage verticalCutStage;
	
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
		
		selectedTraceSlider = new SelectedTraceSlider(model.getSettings(), sliderListener);
		distBetweenTracesSlider = new DistBetweenTracesSlider(model.getSettings(), sliderListener);
		
	}
	
	public void recalc() {
		controller.render(null);
	}
		

	public void init() {
		
		verticalCutStage = new Stage();
		verticalCutStage.setTitle("vertical cut");
		verticalCutStage.setScene(build());
		
	}

	public void show() {
		
		verticalCutStage.show();
		
	}

	public Scene build() {
		
		   
		//bPane.setTop(); 
		//bPane.setBottom(prepareStatus()); 
		bPane.setRight(getToolPane());
		
		vbox.getChildren().add(imageView);
		
		bPane.setCenter(vbox);
		
		Scene scene = new Scene(bPane, 600, 700);
		
		
		return scene;
	}
	
	private Node getToolPane() {
		VBox vBox = new VBox(); 
		vBox.setPadding(new Insets(3, 13, 3, 3));
		
		vBox.getChildren().add(widthZoomSlider.produce());
		vBox.getChildren().add(heightZoomSlider.produce());
		vBox.getChildren().add(heightStartSlider.produce());
		vBox.getChildren().add(selectedTraceSlider.produce());
		vBox.getChildren().add(distBetweenTracesSlider.produce());

		
		//widthZoomSlider.updateUI();
		//vBox.getChildren().add(distSlider.produce());
		return vBox;
	}
	
	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel obj) {

//			if(model.getScans() == null) {
//				return;
//			}
				
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
		
		int width = (int)vbox.getWidth();
		int height = (int)vbox.getHeight();//model.getSettings().maxsamples;
				
	    BufferedImage image = new BufferedImage(width-60, height-60, BufferedImage.TYPE_INT_RGB);
	    
	    Graphics2D g2 = (Graphics2D)image.getGraphics();
	    
	    
	    
	    //draw hor grid
	    g2.setColor(Color.GRAY);
	    for(int i=0; i<30; i++) {
	    	
	    	int h = (int)((i-model.getSettings().heightStart) * getHZoom());
	    	g2.drawLine(0, h, width, h);
	    }
	    
	    int rangx = width / model.getSettings().distBetweenTraces / 2;
	    for(int x=-rangx; x<rangx; x++){
	    	
    		int scanNum = model.getSettings().selectedScanIndex + x;
    		if(scanNum<0 || scanNum >= model.getFileManager().getTraces().size()) {
    			continue;
    		}

    		//step between graphics
    		int startx = width/2 + x * model.getSettings().distBetweenTraces;
    		
    		g2.setColor(Color.DARK_GRAY);
    		g2.drawLine(startx,0, startx, height);
    		
    		g2.setColor(Color.GRAY);
    		
    		Trace trace = model.getFileManager().getTraces().get(scanNum);
    		float [] values = trace.getOriginalValues();
    		//drawGraph(values, startx, g2);
    		
    		g2.setColor(Color.RED);
    		drawGraph(trace.getNormValues(), startx, g2);
    	
    		if(trace.isEnd()) {
    			g2.setColor(Color.LIGHT_GRAY);
    			g2.drawLine(startx,0, startx, height/2);
    		}
    		int rad = 2;
    		for(Integer i : trace.max) {
    			g2.setColor(Color.YELLOW);    		
    			g2.fillOval(startx - rad, 
    					(int)((i-model.getSettings().heightStart) * getHZoom()), 
    					rad*2, rad*2);
    		}
    		g2.setColor(Color.GREEN);    		
    		g2.drawOval(startx - rad, 
    			(int)((trace.maxindex2-model.getSettings().heightStart) * getHZoom()), 
    			rad*2, rad*2);
    		
	    }

	    
	    
	    return image;
	}

	
	
	protected void drawGraph(float[] values, int startx, Graphics2D g2) {
	
		float kfy = getHZoom();
		
		double delitel = Math.pow(1.173, model.getSettings().widthZoomKf);
		
		int x1 = 0;
		int heightstart = model.getSettings().heightStart;
		int maxy = values.length - heightstart;
    	for(int y=0; y<maxy; y++){
    		
    		double val = values[y + heightstart];
    		
    		int x2 = (int)(val / delitel);
    		if(y != 0) {
    			int y1 = (int)((y-1)*kfy);
    			int y2 = (int)(y*kfy);
    			
    			if(x1 != 0 || x2 != 0 ) {
    				g2.drawLine(startx + x1, y1, startx + x2, y2);
    			}
    			
    		}
    		x1 = x2;
    	}
		
	}

	private float getHZoom() {
		float kfy = (float)model.getSettings().heightZoomKf / 10.0f;
		return kfy;
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
			slider.setMax(100);		
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
			slider.setMax(250);		
			slider.setMin(5);
			slider.setValue(settings.heightZoomKf);
		}
		
		public int updateModel() {
			settings.heightZoomKf = (int)slider.getValue();
			return settings.heightZoomKf;
		}
	}
	
	
	public class SelectedTraceSlider extends BaseSlider {
		
		public SelectedTraceSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "selected";
			units = "samples";
			tickUnits = 1000;
		}

		public void updateUI() {
			//slider.setMax(model.getFileManager().getTraces().size()-1);
			int mx = model.getFileManager().isActive() ? model.getFileManager().getTraces().size()-1 : 1; 
			slider.setMax(mx);
			slider.setMin(0);
			//slider.set
			slider.setValue(settings.selectedScanIndex);
		}
		
		public int updateModel() {
			settings.selectedScanIndex = (int)slider.getValue();
			return settings.selectedScanIndex;
		}
	}

	public class DistBetweenTracesSlider extends BaseSlider {
		
		public DistBetweenTracesSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "distance";
			units = "px";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(500);		
			slider.setMin(1);
			slider.setValue(settings.distBetweenTraces);
		}
		
		public int updateModel() {
			settings.distBetweenTraces = (int)slider.getValue();
			return settings.distBetweenTraces;
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
			slider.setMax(500);		
			slider.setMin(0);
			slider.setValue(settings.heightStart);
		}
		
		public int updateModel() {
			settings.heightStart = (int)slider.getValue();
			return settings.heightStart;
		}
	}
	
}
