package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.github.thecoldwine.sigrun.common.ext.AreaType;
import com.github.thecoldwine.sigrun.common.ext.AuxElement;
import com.github.thecoldwine.sigrun.common.ext.AuxRect;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.PrismModeFactory.ThresholdSlider;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.draw.PrismDrawer;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.ui.BaseSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CleverImageView implements SmthChangeListener, ModeFactory {
	
	
	protected Model model;
	protected ImageView imageView = new ImageView();
	protected VBox vbox = new VBox();
	protected ScrollBar s1 = new ScrollBar();
	
	protected BufferedImage img;
	protected int width;
	protected int height;
	protected double contrast = 900;
	
	private ThresholdSlider contrastSlider;
	private ToggleButton auxModeBtn = new ToggleButton("aux");
	
	
	private MouseHandler scrollHandler;
	private AuxElementEditHandler auxEditHandler;
	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			repaintEvent();
		}
	};
	
	
	public CleverImageView(Model model) {
		this.model = model;
		
		contrastSlider = new ThresholdSlider(model.getSettings(), sliderListener);
		
		initImageView();
		
		s1.setOrientation(Orientation.HORIZONTAL);
		
		s1.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            	getField().setSelectedTrace(new_val.intValue());
                repaintEvent();
                
            }
        });
		

		
		vbox.getChildren().addAll(imageView, s1);
		
		scrollHandler = new CleverViewScrollHandler(this);
		auxEditHandler = new AuxElementEditHandler(this);
		
		AppContext.smthListener.add(this);
	}
	
	protected BufferedImage draw(int width,	int height) {
		if(width <= 0 || height <= 0 || !model.getFileManager().isActive()) {
			return null;
		}		

		VerticalCutField field = new VerticalCutField(getField());
		
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] buffer = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData() ;
		
		Graphics2D g2 = (Graphics2D)bi.getGraphics();
		g2.setPaint ( Color.DARK_GRAY );
		g2.fillRect ( 0, 0, bi.getWidth(), bi.getHeight() );
		
		g2.translate(width/2, 0);
		
		///

		int startTrace = field.getFirstVisibleTrace(width);
		int finishTrace = field.getLastVisibleTrace(width);		
		
		
		new PrismDrawer(model, 0).draw(width, height, field, g2, buffer, contrast);
		
		//drawPrism(width, height, field, buffer, TOP_MARGIN, startTrace, finishTrace);
		
		//
		
		
		drawLevel(field, g2, startTrace, finishTrace);
		
		drawFileNames(height, field, g2);
		
//		drawFoundPoints(field, g2);
		
		for(BaseObject bo : model.getAuxElements()) {
			bo.drawOnCut(g2, getField());
		}
		if(model.getControls() != null) {
			for(BaseObject bo : model.getControls()) {
				bo.drawOnCut(g2, getField());
			}
		}
		
		///
		return bi;
	}

//	private void drawFoundPoints(VerticalCutField field, Graphics2D g2) {
//		for(Trace trace : model.getFoundTrace()) {
//			Point p = field.traceSampleToScreen(new TraceSample(trace.indexInSet, 0));
//			
//			g2.drawImage(ResourceImageHolder.IMG_SHOVEL, p.x-ResourceImageHolder.IMG_SHOVEL.getWidth(null)/2 , 0, null);
//		}
//	}

	private void drawLevel(VerticalCutField field, Graphics2D g2, int startTrace, int finishTrace) {
		g2.setColor(Color.GREEN);
		for(int i=startTrace+1; i<finishTrace; i++) {
			Trace trace1 = model.getFileManager().getTraces().get(i-1);
			Trace trace2 = model.getFileManager().getTraces().get(i);
			
			Point p1 = field.traceSampleToScreenCenter(new TraceSample(i-1,  trace1.maxindex2));
			Point p2 = field.traceSampleToScreenCenter(new TraceSample(i,  trace2.maxindex2));
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	private void drawFileNames(int height, VerticalCutField field, Graphics2D g2) {
		g2.setColor(Color.WHITE);
		for(SgyFile fl : model.getFileManager().getFiles()) {
			Point p = field.traceSampleToScreen(new TraceSample(fl.getTraces().get(0).indexInSet, 0));
			
			g2.drawLine(p.x, 0, p.x, height);
			g2.drawString(fl.getFile().getName(), p.x + 7, 11);
		}
	}

	private void drawPrism(int width, int height, VerticalCutField field, int[] buffer, int vOffset, int startTrace, int finishTrace) {
		int lastSample = field.getLastVisibleSample(height-vOffset);
		int vscale = Math.max(1, (int)field.getVScale());
		int hscale = Math.max(1, (int)field.getHScale());
		
		for(int i=startTrace; i<finishTrace; i++ ) {
			
				Trace trace = model.getFileManager().getTraces().get(i);
				float[] values = trace.getNormValues();
				for(int j = field.getStartSample(); j<Math.min(lastSample, values.length ); j++) {
					
					Point p = field.traceSampleToScreen(new TraceSample(i, j));
					
		    		int c = (int) (127.0 + Math.tanh(values[j]/contrast) * 127.0);
		    		int color = ((c) << 16) + ((c) << 8) + c;
		    		
		    		//buffer[width/2 + p.x + vscale * p.y  * width ] = color;
		    		for(int xt=0; xt < hscale; xt ++) {
		    			for(int yt =0; yt < vscale; yt++) {
		    				buffer[width / 2 + xt + p.x + (vOffset + p.y + yt) * width ] = color;
		    			}
		    		}
				}
			
		}
	}
	

	@Override
	public void show() {
	
		updateScroll();
		repaintEvent();
	}

	@Override
	public Node getCenter() {
		
		return vbox;
	}

	@Override
	public List<Node> getRight() {
		
		return Arrays.asList(contrastSlider.produce() , auxEditHandler.getRight());
	}

	int z = 0;
	protected void initImageView() {
		imageView.setOnScroll(event -> {
	    	//model.getField().setZoom( .getZoom() + (event.getDeltaY() > 0 ? 1 : -1 ) );
			
			Point t = getLocalCoords(event.getSceneX(), event.getSceneY());
			TraceSample ts = getField().screenToTraceSample(t);
			
			z = z + (event.getDeltaY() > 0 ? 1 : -1 );
			double s = Math.pow(1.2, z);
			getField().setHScale(s*2);
			getField().setVScale(s);
			
			Point t2 = getLocalCoords(event.getSceneX(), event.getSceneY());
			TraceSample ts2 = getField().screenToTraceSample(t2);
			
			getField().setSelectedTrace(getField().getSelectedTrace() - (ts2.getTrace() - ts.getTrace()));
			
			
			int starts = getField().getStartSample() - (ts2.getSample() - ts.getSample());
			getField().setStartSample(starts);
				
			
			
			
			repaintEvent();
			updateScroll();
	    } );
		
		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);		
		imageView.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
		imageView.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		imageView.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, dragReleaseHandler);		
	}
	
	protected EventHandler dragDetectedHandler = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(MouseEvent mouseEvent) {
	    	
	    	imageView.startFullDrag();
	    }
	};
	
	protected EventHandler dragReleaseHandler = new EventHandler<MouseDragEvent>() {
        @Override
        public void handle(MouseDragEvent event) {
        	
        	Point p = getLocalCoords(event);
        	if(!auxEditHandler.mouseReleaseHandle(p, getField())) {
        		scrollHandler.mouseReleaseHandle(p, getField());
        	}
        	//getMouseHandler().mouseReleaseHandle();
        	
        	event.consume();
        }
	};
	
	protected EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        
		@Override
        public void handle(MouseEvent event) {
			
        	Point p = getLocalCoords(event);
        	if(!auxEditHandler.mouseMoveHandle(p, getField())) {
        		scrollHandler.mouseMoveHandle(p, getField());
        	}
			
			//getMouseHandler().mouseMoveHandle(getLocalCoords(event));
        	
        }
	};
	
	private Point getLocalCoords(MouseEvent event) {
		
		return getLocalCoords(event.getSceneX(), event.getSceneY());
	
	}
	protected Point getLocalCoords(double x, double y) {
		javafx.geometry.Point2D sceneCoords  = new javafx.geometry.Point2D(x, y);
    	javafx.geometry.Point2D imgCoord = imageView.sceneToLocal(sceneCoords );        	
    	Point p = new Point(
    			(int)(imgCoord.getX() - imageView.getBoundsInLocal().getWidth()/2), 
    			(int)(imgCoord.getY() ));//- imageView.getBoundsInLocal().getHeight()/2
		return p;
	}

	protected EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	
        	Point p = getLocalCoords(event);
        	if(!auxEditHandler.mousePressHandle(p, getField())) {
        		scrollHandler.mousePressHandle(p, getField());
        	}
        	
        	//getMouseHandler().mousePressHandle(getLocalCoords(event));
        }
	};

	protected EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	
        	//getMouseHandler().mouseReleaseHandle(getLocalCoords(event));
        	Point p = getLocalCoords(event);
        	if(!auxEditHandler.mouseReleaseHandle(p, getField())) {
        		scrollHandler.mouseReleaseHandle(p, getField());
        	}
        	
        }
	};
	
	protected void repaintEvent() {
		controller.render(null);
	}
	
	protected void repaint() {
		//System.out.println("repaint");
		img = draw(width, height);
		
		updateWindow();
	}
	
	protected void updateWindow() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if(img == null) {
            		return;
            	}
			    Image i = SwingFXUtils.toFXImage(img, null);
			    imageView.setImage(i);
            }
          });
	}

	@Override
	public void somethingChanged(WhatChanged changed) {

		if(changed.isAuxOnMapSelected()) {
			//field.setSelectedTrace(selectedTrace);
		}
		
		repaintEvent();
		updateScroll();
	}

	private void updateScroll() {
		if(!model.getFileManager().isActive()) {
			return;
		}
		
		s1.setMin(0);
		s1.setMax(model.getFileManager().getTraces().size());
		
		int am = getField().getVisibleNumberOfTrace(width);
		s1.setVisibleAmount(am);
		s1.setUnitIncrement(am/4);
		s1.setBlockIncrement(am);
	}
	

	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel level) {

			repaint();
			
		}
		
	});
	
	public class ThresholdSlider extends BaseSlider {
		
		public ThresholdSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Contrast";
			units = "";
			tickUnits = 1000;
		}

		public void updateUI() {
			slider.setMax(15000);
			slider.setMin(50);
			//slider.set
			slider.setValue(contrast);
		}
		
		public int updateModel() {
			contrast = (int)slider.getValue();
			return (int)contrast;
		}
	}

	public void setSize(int width, int height) {
		
		this.width = width;
		this.height = height-30;
		getField().setViewDimension(new Dimension(this.width, this.height));
		
		repaintEvent();
		
	}

	MouseHandler getMouseHandler() {
		if(auxModeBtn.isSelected()) {
			return auxEditHandler;
		}else {
			return scrollHandler;
		}
	}

	void setScrollHandler(MouseHandler scrollHandler) {
		this.scrollHandler = scrollHandler;
	}

	protected VerticalCutField getField() {
		return model.getVField();
	}
	
}
