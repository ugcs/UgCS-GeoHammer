package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CleverImageView implements SmthChangeListener, ModeFactory {
	protected Model model;
	
	VerticalCutField field;
	VerticalCutField dragField;
	protected ImageView imageView = new ImageView();
	protected VBox vbox = new VBox();
	ScrollBar s1 = new ScrollBar();
	
	protected BufferedImage img;
	protected int width;
	protected int height;
	protected double threshold = 900;
	private Point dragPoint;
	//private TraceSample click;
	
	
	public CleverImageView(Model model) {
		this.model = model;
		field = new VerticalCutField(model);
		
		initImageView();
		
		s1.setOrientation(Orientation.HORIZONTAL);
		
		s1.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                field.setSelectedTrace(new_val.intValue());
                repaintEvent();
                
            }
        });
		
		vbox.getChildren().addAll(imageView, s1);
		
		
		AppContext.smthListener.add(this);
	}
	
	protected BufferedImage draw(int width,	int height) {
		if(width <= 0 || height <= 0) {
			return null;
		}		

		int vscale = Math.max(1, (int)field.getVScale());
		int hscale = Math.max(1, (int)field.getHScale());
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] buffer = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData() ;
		
		Graphics2D g2 = (Graphics2D)bi.getGraphics();
		g2.setPaint ( Color.DARK_GRAY );
		g2.fillRect ( 0, 0, bi.getWidth(), bi.getHeight() );
		
		g2.translate(width/2, 0);
		
		///
		
		int startTrace = field.getFirstVisibleTrace(width);
		int finishTrace = field.getLastVisibleTrace(width);		
		int lastSample = field.getLastVisibleSample(height);
		
		for(int i=startTrace; i<finishTrace; i++ ) {
			
				Trace trace = model.getFileManager().getTraces().get(i);
				float[] values = trace.getNormValues();
				for(int j = field.getStartSample(); j<Math.min(lastSample, values.length ); j++) {
					Point p = field.traceSampleToScreen(new TraceSample(i, j));
					
		    		int c = (int) (127.0 + Math.tanh(values[j]/threshold) * 127.0);
		    		int color = ((255-c) << 16) + ((c) << 8) + c;
		    		
		    		//buffer[width/2 + p.x + vscale * p.y  * width ] = color;
		    		for(int xt =0; xt < hscale; xt ++) {
		    			for(int yt =0; yt < vscale; yt++) {
		    				buffer[width / 2 + xt + p.x + (p.y   + yt) * width ] = color;
		    			}
		    		}
				}
			
		}
		
		
		///
		return bi;
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
		
		return Arrays.asList(new Button("f"));
	}

	int z = 0;
	protected void initImageView() {
		imageView.setOnScroll(event -> {
	    	//model.getField().setZoom( .getZoom() + (event.getDeltaY() > 0 ? 1 : -1 ) );
			z = z + (event.getDeltaY() > 0 ? 1 : -1 );
			double s = Math.pow(2, z);
			field.setHScale(s);
			field.setVScale(s*2);
			
			
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
        	
        	
        	
        	dragPoint = null;
        	
        	event.consume();
        }
	};
	
	protected EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        

		@Override
        public void handle(MouseEvent event) {
			
//    		if(click == null) {    			
//    			return;
//    		}
    		if(dragPoint == null) {
    			
    			return;
    		}
    		
    		try {
	    		Point point = getLocalCoords(event);
	    		
	    		Point p = new Point(
	    			dragPoint.x - point.x, 
	    			dragPoint.y - point.y);
	    		
	    		
	    		TraceSample sceneCenter = dragField.screenToTraceSample(p);
	    		//dragPoint = point;
	    		System.out.println("move");
	    		field.setSelectedTrace(sceneCenter.getTrace());
	    		s1.setValue(sceneCenter.getTrace());
	    		field.setStartSample(sceneCenter.getSample());
	    		    		
	    		
	    		repaintEvent();
    		}catch(Exception e) {
    			e.printStackTrace();
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
    			(int)(imgCoord.getY() - imageView.getBoundsInLocal().getHeight()/2));
		return p;
	}

	protected EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	
        	
        	dragField = new VerticalCutField(field);
    		dragPoint = getLocalCoords(event);    		
    		//click = dragField.screenToTraceSample(dragPoint);
    		    		
    		repaintEvent();
        	
        }

	};

	protected EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	Point2D p = getLocalCoords(event);
        	
        	
        }
	};
	

	protected void repaintEvent() {
		System.out.println("repaint");
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
		if(changed.isWindowresized()) {
			width = model.getSettings().center_box_width;
			height = model.getSettings().center_box_height - 50;
		}

		updateScroll();
	}

	private void updateScroll() {
		
		s1.setMin(0);
		s1.setMax(model.getFileManager().getTraces().size());
		
		int am = field.getVisibleNumberOfTrace(width);
		System.out.println("scroll amount " + am);
		s1.setVisibleAmount(am);
		s1.setUnitIncrement(am/4);
		s1.setBlockIncrement(am);
	}
	
	
}
