package com.ugcs.gprvisualizer.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.PrismDrawer;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.math.HyperFinder;
import com.ugcs.gprvisualizer.ui.BaseSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProfileView implements SmthChangeListener, ModeFactory {
	
	protected PrismDrawer prismDrawer;	
	protected Model model;
	protected ImageView imageView = new ImageView();
	protected VBox vbox = new VBox();
	protected ScrollBar s1 = new ScrollBar();
	
	protected BufferedImage img;
	protected Image i ;
	protected int width;
	protected int height;
	protected double contrast = 50;	
	
	private ContrastSlider contrastSlider;
	private AspectSlider aspectSlider;
	private HyperbolaSlider hyperbolaSlider;
	private HyperGoodSizeSlider hyperGoodSizeSlider;
	
	private ToggleButton auxModeBtn = new ToggleButton("aux");
	ToolBar toolBar = new ToolBar();
	private Button zoomInBtn = new Button("", ResourceImageHolder.getImageView("zoom-in_20.png" ));
	private Button zoomOutBtn = new Button("", ResourceImageHolder.getImageView("zoom-out_20.png"));
	
	private MouseHandler selectedMouseHandler;   
	private MouseHandler scrollHandler;
	private AuxElementEditHandler auxEditHandler;
	
	private HyperFinder hyperFinder; 
	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			repaintEvent();
		}
	};
	private ChangeListener<Number> aspectSliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			//updateAspect();
			updateScroll();
			repaintEvent();
		}
	};
	
	public ProfileView(Model model) {
		this.model = model;
		
		hyperFinder = new HyperFinder(model);
		
		prismDrawer = new PrismDrawer(model, 0);
		
		contrastSlider = new ContrastSlider(model.getSettings(), sliderListener);
		aspectSlider = new AspectSlider(model.getSettings(), aspectSliderListener);
		hyperbolaSlider = new HyperbolaSlider(model.getSettings(), aspectSliderListener);
		hyperGoodSizeSlider = new HyperGoodSizeSlider(model.getSettings(), sliderListener);
		initImageView();
		
		s1.setOrientation(Orientation.HORIZONTAL);
		s1.setVisible(false);
		s1.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            	getField().setSelectedTrace(new_val.intValue());
                repaintEvent();
                
            }
        });

		scrollHandler = new CleverViewScrollHandler(this);
		auxEditHandler = new AuxElementEditHandler(this);
		
		toolBar.setDisable(true);
		toolBar.getItems().addAll(auxEditHandler.getRightPanelTools());
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().addAll(AppContext.navigator.getToolNodes());
		
		toolBar.getItems().add(getSpacer());
		toolBar.getItems().add(zoomInBtn);
		toolBar.getItems().add(zoomOutBtn);
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().add(hyperLiveViewBtn);
		
		vbox.getChildren().addAll(toolBar, imageView, s1);
		
		zoomInBtn.setOnAction(e -> {
			zoom(1, width/2, height/2);

		});
		zoomOutBtn.setOnAction(e -> {
			zoom(-1, width/2, height/2);
		});
		
		AppContext.smthListener.add(this);
	}
	
	protected BufferedImage draw(int width,	int height) {
		if(width <= 0 || height <= 0 || !model.getFileManager().isActive()) {
			return null;
		}		
		
		List<Trace> traces = model.getFileManager().getTraces();

		ProfileField field = new ProfileField(getField());
		
		BufferedImage bi ;
		if(img != null && img.getWidth() == width && img.getHeight() == height) {
			bi = img;
		}else {
			bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		int[] buffer = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData() ;
		
		Graphics2D g2 = (Graphics2D)bi.getGraphics();
		
		RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2.setRenderingHints(rh);		
		
		clearBitmap(bi, g2);
		
		g2.translate(width/2, 0);

		int startTrace = field.getFirstVisibleTrace(width);
		int finishTrace = field.getLastVisibleTrace(width);		
		
		double contr = Math.pow(1.08, 140-contrast);
		System.out.println(contr);

		prismDrawer.draw(width, height, field, g2, buffer, contr);
		
		drawGroundLevel(field, g2, traces,  startTrace, finishTrace);
		
		drawFileNames(height, field, g2);

		drawAmplitudeMapLevels(field, g2);
		
		drawAuxElements(field, g2);
		
		if(model.getSettings().hyperliveview) {
			hyperFinder.drawHyperbolaLine(g2, field);
		}
		
		///
		return bi;
	}

	final static float dash1[] = {5.0f};
	final static BasicStroke dashed =
	        new BasicStroke(1.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        10.0f, dash1, 0.0f);
	
	private void drawAmplitudeMapLevels(ProfileField field, Graphics2D g2) {
		if(model.getSettings().isRadarMapVisible) {
		
			g2.setColor(Color.MAGENTA);
			g2.setStroke(dashed);			
			
			int y = field.traceSampleToScreen(new TraceSample(0, model.getSettings().layer)).y;
			g2.drawLine(-width/2, y, width/2, y);

			int y2 = field.traceSampleToScreen(new TraceSample(0, model.getSettings().layer + model.getSettings().hpage)).y;
			g2.drawLine(-width/2, y2, width/2, y2);

		}		
	}
	
	private void drawAuxElements(ProfileField field, Graphics2D g2) {
		for(BaseObject bo : model.getAuxElements()) {
			bo.drawOnCut(g2, field);
		}
		if(model.getControls() != null) {
			for(BaseObject bo : model.getControls()) {
				bo.drawOnCut(g2, field);
			}
		}
	}

	private void clearBitmap(BufferedImage bi, Graphics2D g2) {
		g2.setPaint ( Color.DARK_GRAY );
		g2.fillRect ( 0, 0, bi.getWidth(), bi.getHeight() );
	}

	private void drawGroundLevel(ProfileField field, Graphics2D g2, List<Trace> traces, int startTrace, int finishTrace) {
		g2.setColor(Color.GREEN);
		for(int i=startTrace+1; i<finishTrace; i++) {
			Trace trace1 = traces.get(i-1);
			Trace trace2 = traces.get(i);
			
			Point p1 = field.traceSampleToScreenCenter(new TraceSample(i-1,  trace1.maxindex2));
			Point p2 = field.traceSampleToScreenCenter(new TraceSample(i,  trace2.maxindex2));
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	private void drawFileNames(int height, ProfileField field, Graphics2D g2) {
		g2.setColor(Color.WHITE);
		for(SgyFile fl : model.getFileManager().getFiles()) {
			Point p = field.traceSampleToScreen(new TraceSample(fl.getTraces().get(0).indexInSet, 0));
			
			g2.drawLine(p.x, 0, p.x, height);
			g2.drawString(fl.getFile().getName(), p.x + 7, 11);
		}
	}

	private void drawPrism(int width, int height, ProfileField field, int[] buffer, int vOffset, int startTrace, int finishTrace) {
		int lastSample = field.getLastVisibleSample(height-vOffset);
		int vscale = Math.max(1, (int)field.getVScale());
		int hscale = Math.max(1, (int)field.getHScale());
		
		double contr = Math.pow(1.4, contrast+10);
		System.out.println(contr);
		
		for(int i=startTrace; i<finishTrace; i++ ) {
			
				Trace trace = model.getFileManager().getTraces().get(i);
				float[] values = trace.getNormValues();
				for(int j = field.getStartSample(); j<Math.min(lastSample, values.length ); j++) {
					
					Point p = field.traceSampleToScreen(new TraceSample(i, j));
					
		    		int c = (int) (127.0 + Math.tanh(values[j]/contr) * 127.0);
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
		
		Pane sp2 = new Pane();
		ChangeListener<Number> sp2SizeListener = (observable, oldValue, newValue) -> {
			this.setSize((int) (sp2.getWidth()), (int) (sp2.getHeight()));
		};
		sp2.widthProperty().addListener(sp2SizeListener);
		sp2.heightProperty().addListener(sp2SizeListener);
		
		sp2.getChildren().add(vbox);
		
		//sp2.getChildren().add(toolBar);
		
		return sp2;
	}

	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			model.getSettings().hyperliveview = hyperLiveViewBtn.isSelected();
			repaintEvent();
		}
	};
	
	private ToggleButton hyperLiveViewBtn = new ToggleButton("", ResourceImageHolder.getImageView("hypLive.png"));
	{
		hyperLiveViewBtn.setOnAction(showMapListener);
	}


	
	@Override
	public List<Node> getRight() {
		
		return Arrays.asList(
				new HBox( zoomInBtn, zoomOutBtn),
				contrastSlider.produce() , 
				//auxEditHandler.getRight(), 
				aspectSlider.produce(), 
				hyperbolaSlider.produce(),
				hyperGoodSizeSlider.produce()
			);
	}

	int z = 0;
	protected void initImageView() {
		imageView.setOnScroll(event -> {
	    	//model.getField().setZoom( .getZoom() + (event.getDeltaY() > 0 ? 1 : -1 ) );
			int ch = (event.getDeltaY() > 0 ? 1 : -1 );
			
			double ex = event.getSceneX();
			double ey = event.getSceneY();
			
			zoom(ch, ex, ey);
	    } );
		
		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);
		imageView.setOnMouseMoved(mouseMoveHandler);
		imageView.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
		imageView.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		imageView.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, dragReleaseHandler);		
	}

	private void zoom(int ch, double ex, double ey) {
		Point t = getLocalCoords(ex, ey);
		
		TraceSample ts = getField().screenToTraceSample(t);
		
		z = z + ch;
		
		getField().setZoom(getField().getZoom()+ch);
		
		Point t2 = getLocalCoords(ex, ey);
		TraceSample ts2 = getField().screenToTraceSample(t2);
		
		getField().setSelectedTrace(getField().getSelectedTrace() - (ts2.getTrace() - ts.getTrace()));
		
		
		int starts = getField().getStartSample() - (ts2.getSample() - ts.getSample());
		getField().setStartSample(starts);
		
		
		updateScroll();
		repaintEvent();
	
	}

	protected EventHandler dragDetectedHandler = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(MouseEvent mouseEvent) {
	    	
	    	imageView.startFullDrag();
	    	
	    	imageView.setCursor(Cursor.CLOSED_HAND);
	    }
	};
	
	protected EventHandler dragReleaseHandler = new EventHandler<MouseDragEvent>() {
        @Override
        public void handle(MouseDragEvent event) {
        	Point p = getLocalCoords(event);
        	
        	if(selectedMouseHandler != null) {
        		selectedMouseHandler.mouseReleaseHandle(p, getField());
        		selectedMouseHandler = null;
        	}
        	
        	imageView.setCursor(Cursor.DEFAULT);
        	
        	event.consume();
        }
	};
	
	protected EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        
		@Override
        public void handle(MouseEvent event) {
			
        	Point p = getLocalCoords(event);
        	
        	if(model.getSettings().hyperliveview) {
        		TraceSample ts = getField().screenToTraceSample(p);
        		hyperFinder.setPoint(ts);        	
        		repaintEvent();
        	}else {
        		
        		if(selectedMouseHandler != null) {

        			selectedMouseHandler.mouseMoveHandle(p, getField());
        		}else{
	        		if(!auxEditHandler.mouseMoveHandle(p, getField())) {

	        		}
        		}
        	}
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
    			(int)(imgCoord.getY() ));
		return p;
	}

	protected EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	
        	Point p = getLocalCoords(event);
        	if(auxEditHandler.mousePressHandle(p, getField())) {
        		selectedMouseHandler = auxEditHandler; 
        	}else if(scrollHandler.mousePressHandle(p, getField())) {
        		selectedMouseHandler = scrollHandler;        		
        	}else {
        		selectedMouseHandler = null;
        	}
        	
        	imageView.setCursor(Cursor.CLOSED_HAND);
        }
	};

	protected EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	
        	Point p = getLocalCoords(event);
        	
        	if(selectedMouseHandler != null) {
        		selectedMouseHandler.mouseReleaseHandle(p, getField());
        		
        		selectedMouseHandler = null;
        	}        	
        }
	};
	
	protected void repaintEvent() {
		if(!model.isLoading()) {
			controller.render(null);
		}
	}
	
	protected void repaint() {

		img = draw(width, height);
		if(img != null) {
			i = SwingFXUtils.toFXImage(img, null);
		}
		updateWindow();
	}
	
	protected void updateWindow() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if(i == null) {
            		return;
            	}
			    
			    imageView.setImage(i);
            }
          });
	}

	@Override
	public void somethingChanged(WhatChanged changed) {

		if(changed.isFileopened()) {
			s1.setVisible(true);
			toolBar.setDisable(false);
		}
		
		if(changed.isAuxOnMapSelected()) {

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
		s1.setValue(getField().getSelectedTrace());
	}
	

	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel level) {

			repaint();
			
		}
		
	});
	
	public class ContrastSlider extends BaseSlider {
		
		public ContrastSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Contrast";
			units = "";
			tickUnits = 25;
		}

		public void updateUI() {
			slider.setMax(100);
			slider.setMin(0);
			slider.setValue(contrast);
		}
		
		public int updateModel() {
			contrast = (int)slider.getValue();
			return (int)contrast;
		}
	}

	public class AspectSlider extends BaseSlider {
		
		public AspectSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Aspect";
			units = "";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(30);
			slider.setMin(-30);
			slider.setValue(getField().getAspect());
		}
		
		public int updateModel() {
			getField().setAspect((int)slider.getValue());
			return (int)getField().getAspect();
		}
	}
	public class HyperbolaSlider extends BaseSlider {
		
		public HyperbolaSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Hyperbola";
			units = "";
			tickUnits = 200;
		}

		public void updateUI() {
			slider.setMax(400);
			slider.setMin(2);
			slider.setValue(settings.hyperkfc);
		}
		
		public int updateModel() {
			settings.hyperkfc = (int)slider.getValue();
			return (int)settings.hyperkfc;
		}
	}

	public class HyperGoodSizeSlider extends BaseSlider {
		
		public HyperGoodSizeSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Hyper size";
			units = "";
			tickUnits = 200;
		}

		public void updateUI() {
			slider.setMax(350);
			slider.setMin(40);
			slider.setValue(settings.hypergoodsize);
		}
		
		public int updateModel() {
			settings.hypergoodsize = (int)slider.getValue();
			return (int)settings.hypergoodsize;
		}
	}
	
	public void setSize(int width, int height) {
		
		this.width = width;
		this.height = height-Model.TOP_MARGIN;
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

	protected ProfileField getField() {
		return model.getVField();
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}

}
