package com.ugcs.gprvisualizer.app;

import java.util.Set;

import com.ugcs.gprvisualizer.gpr.Model;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProfileScroll extends Canvas {

	private Model model;
	static final int HEIGHT = 24;
	static final int SIDE_WIDTH = 20;
	static final int CENTER_MARGIN = 5; 
	static final int V_MARGIN = 4;
	static final int V_GRAY_MARGIN = 7;
	double start;
	double finish;
	
	double pressX;
	double pressXInBar;
	
	ChangeListener<Number> changeListener;
	
	public void setChangeListener(ChangeListener<Number> changeListener) {
		this.changeListener = changeListener;
	}
	
	interface MouseSInput {
		Rectangle getRect();
		
		void move(Point2D localPoint);
	}
	
	
	MouseSInput leftInput = new MouseSInput() {

		@Override
		public Rectangle getRect() {
			return getLeftBar();
		}

		@Override
		public void move(Point2D localPoint) {
			
			double barStart = localPoint.getX() - pressXInBar;
			start = barStart + SIDE_WIDTH + CENTER_MARGIN;
			
			recalcField();
			
			draw();			
			changeListener.changed(null, null, null);
			//recalc back
		}
	};
	MouseSInput rightInput = new MouseSInput() {

		@Override
		public Rectangle getRect() {
			return getRightBar();
		}

		@Override
		public void move(Point2D localPoint) {
			
			double barStart = localPoint.getX() - pressXInBar;
			
			
			finish = barStart - CENTER_MARGIN;
			
			//recalc back			
			recalcField();
			
			draw();
			changeListener.changed(null, null, null);
			
		}
	};
	MouseSInput centerInput = new MouseSInput() {

		@Override
		public Rectangle getRect() {
			return getCenterBar();
		}

		@Override
		public void move(Point2D localPoint) {
			
			double barStart = localPoint.getX() - pressXInBar;
			double centerPos = barStart + CENTER_MARGIN + (finish - start) / 2;
			
			centerPos = Math.min(Math.max(centerPos, 0), getWidth());
			
			//finish = barStart;
			double tracesFull = model.getFileManager().getTraces().size();
			
			double trCenter = centerPos * tracesFull / getWidth();
			
			
			model.getVField().setSelectedTrace((int) trCenter);
			
			double rectWidth = finish - start;
			start = centerPos - rectWidth / 2;
			finish = centerPos + rectWidth / 2;
			

			draw();
			
			changeListener.changed(null, null, trCenter);
		}
	};	
	
	MouseSInput selected;
	Set<MouseSInput> bars = Set.of(centerInput, leftInput, rightInput); 
 
	
	public ProfileScroll(Model model) {
		this.model = model;
		
		setWidth(400);
		setHeight(HEIGHT);		
		
        widthProperty().addListener(evt -> recalc());
        heightProperty().addListener(evt -> recalc());
        

        
		this.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
		this.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		this.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, 
				dragReleaseHandler);		
		this.setOnMouseReleased(mouseReleaseHandler);  
	}
	
	protected EventHandler<MouseEvent> mouseReleaseHandler = 
			new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {        	
        	selected = null;
        	
        }
	};
	
	protected EventHandler<MouseEvent> dragDetectedHandler = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(MouseEvent mouseEvent) {
	    	
	    	selected = null;
	    	
			javafx.geometry.Point2D imgCoord = getLocal(mouseEvent);        	
	    	
	    	for (MouseSInput msi : bars) {
	    		Rectangle r = msi.getRect();
	    		if (r.contains(imgCoord)) {
	    			
	    			selected = msi;
	    			pressX = imgCoord.getX();
	    			pressXInBar = imgCoord.getX() - r.getX();
	    		}
	    	}
	    	
	    	ProfileScroll.this.startFullDrag();
	    	ProfileScroll.this.setCursor(Cursor.CLOSED_HAND);	    	
	    	
	    }

	};

	public javafx.geometry.Point2D getLocal(MouseEvent mouseEvent) {
		javafx.geometry.Point2D sceneCoords = 
				new javafx.geometry.Point2D(
						mouseEvent.getSceneX(), mouseEvent.getSceneY());
		
    	javafx.geometry.Point2D imgCoord = sceneToLocal(sceneCoords);
		return imgCoord;
	}
	
	protected EventHandler<MouseDragEvent> dragReleaseHandler = 
			new EventHandler<MouseDragEvent>() {
        @Override
        public void handle(MouseDragEvent event) {

        	selected = null;
        	
        	ProfileScroll.this.setCursor(Cursor.DEFAULT);
        	
        	event.consume();
        }
	};
	
	protected EventHandler<MouseEvent> mouseMoveHandler = 
			new EventHandler<MouseEvent>() {
        
		@Override
        public void handle(MouseEvent event) {
			if (selected != null) {
			
				javafx.geometry.Point2D imgCoord = getLocal(event);
			
				selected.move(imgCoord);
			}
        	
        }
	};
	
	Rectangle getCenterBar() {
		return new Rectangle(start - CENTER_MARGIN, 0, 
				finish - start + 2 * CENTER_MARGIN, HEIGHT);
	}
	
	Rectangle getLeftBar() {
		return new Rectangle(start - SIDE_WIDTH - CENTER_MARGIN, 
				0, SIDE_WIDTH, HEIGHT);
	}
	
	Rectangle getRightBar() {
		return new Rectangle(finish + CENTER_MARGIN, 0, 
				SIDE_WIDTH, HEIGHT);
	}
	
	void recalc() {
		
		if (!model.isActive()) {
			GraphicsContext gc = this.getGraphicsContext2D();	
			gc.clearRect(0, 0, getWidth(), getHeight());
			return;
		}
		
		int width = (int) getWidth();
		int height = (int) getHeight();
		
		double tracesFull = model.getFileManager().getTraces().size();
		double center = model.getVField().getSelectedTrace();
		
		double tracesVisible = model.getVField().getVisibleNumberOfTrace();
		
		double centerPos =  center / tracesFull * (double) width;
		double rectWidth = tracesVisible / tracesFull * (double) width;
		
		start = centerPos - rectWidth / 2;
		finish = centerPos + rectWidth / 2;
		
		draw();
	}
	
	public void draw() {
		GraphicsContext gc = this.getGraphicsContext2D();	
		gc.clearRect(0, 0, getWidth(), getHeight());
		
		gc.setFill(Color.GRAY);
		gc.fillRect(0, V_GRAY_MARGIN, 
				getWidth(), getHeight() - 2 * V_GRAY_MARGIN);
		
		gc.setFill(Color.BLUE);
		Rectangle c = getCenterBar();

		gc.strokeRoundRect(c.getX() + CENTER_MARGIN, 
				c.getY() + V_MARGIN, 
				c.getWidth() - 2 * CENTER_MARGIN, 
				c.getHeight() - 2 * V_MARGIN, 
				10, 10);
		
		double centerX = c.getX() + c.getWidth() / 2;
		gc.strokeLine(centerX, 0, centerX, HEIGHT);
		
		gc.setFill(Color.AQUAMARINE);
		Rectangle l = getLeftBar();
		

		gc.strokeRoundRect(l.getX() + V_MARGIN, l.getY() + V_MARGIN, 
				l.getWidth() - 2 * V_MARGIN, l.getHeight() - 2 * V_MARGIN, 10, 10);

		gc.setFill(Color.AQUAMARINE);
		Rectangle r = getRightBar();
		gc.strokeRoundRect(r.getX() + V_MARGIN, r.getY() + V_MARGIN, 
				r.getWidth() - 2 * V_MARGIN, r.getHeight() - 2 * V_MARGIN, 10, 10);
		
	}
	
    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return HEIGHT;
    }	
	
	private Node makeDraggable(final Node node) {
		class DragContext {
			double mouseAnchorX;
			double mouseAnchorY;
			double initialTranslateX;
			double initialTranslateY;

		}
		
		final DragContext dragContext = new DragContext();
		final Group wrapGroup = new Group(node);

		wrapGroup.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				mouseEvent.consume();
			}
		});

		wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
					dragContext.mouseAnchorX = mouseEvent.getX();
					dragContext.mouseAnchorY = mouseEvent.getY();
					dragContext.initialTranslateX = node.getTranslateX();
					dragContext.initialTranslateY = node.getTranslateY();
			}
		});

		wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
					node.setTranslateX(
							dragContext.initialTranslateX 
							+ mouseEvent.getX() 
							- dragContext.mouseAnchorX);
					
					node.setTranslateY(
							dragContext.initialTranslateY 
							+ mouseEvent.getY() 
							- dragContext.mouseAnchorY);
			}
		});

		return wrapGroup;

	}
	
	public void recalcField() {
		double scrCenter = (finish + start) / 2;			
		double scrWidth = (finish - start);
		
		double visibletracesCount = scrWidth / (double) getWidth() 
				* (double) model.getTracesCount();
		double hsc = getWidth() / visibletracesCount;
		double aspect = hsc / model.getVField().getVScale();
		
		double trCenter = scrCenter / (double) getWidth() * (double) model.getTracesCount();
		model.getVField().setSelectedTrace((int) trCenter);
		model.getVField().setAspectReal(aspect);
	}
}
