package com.ugcs.gprvisualizer.app;

import java.util.Set;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
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

	static final int HEIGHT = 24;
	static final int SIDE_WIDTH = 20;
	static final int CENTER_MARGIN = 5; 
	static final int V_MARGIN = 4;
	static final int V_GRAY_MARGIN = 7;

	double start;
	double finish;
	
	double pressX;
	double pressXInBar;

	private final Model model;
	private ChangeListener<Number> changeListener;
	private final ScrollableData scrollable;

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

	private MouseSInput centerInput = new MouseSInput() {

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
			double tracesFull = model.getGprTracesCount();
			
			double trCenter = centerPos * tracesFull / getWidth();
			
			
			scrollable.setMiddleTrace((int) trCenter);
			
			double rectWidth = finish - start;
			start = centerPos - rectWidth / 2;
			finish = centerPos + rectWidth / 2;
			

			draw();
			
			changeListener.changed(null, null, trCenter);
		}
	};	
	
	private MouseSInput selected;
	private Set<MouseSInput> bars = Set.of(centerInput, leftInput, rightInput);

	public ProfileScroll(Model model, ScrollableData scrollable) {
		this.model = model;
		this.scrollable = scrollable;
        
		this.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
		this.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		this.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, 
				dragReleaseHandler);		
		this.setOnMouseReleased(mouseReleaseHandler);  
	}

	@Override
	public void resize(double width, double height) {
		if (width >= 0 && Math.abs(getWidth() - width) > 1) {
			System.out.println("ProfileScroll.resize = " + width + " " + height);
			setWidth(width);
			setHeight(height);
		}
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double minWidth(double height) {
		return 50; // Minimum reasonable width
	}

	@Override
	public double maxWidth(double height) {
		return Double.MAX_VALUE;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return HEIGHT;
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
		
		if (!model.isActive() || model.getGprTracesCount() == 0) {
		//	GraphicsContext gc = this.getGraphicsContext2D();
		//	gc.clearRect(0, 0, getWidth(), getHeight());
		//	return;
		}
		
		int width = (int) getWidth();
		int height = (int) getHeight();

		double tracesFull = 0;
		double center;
		double tracesVisible;
		if (model.getGprTracesCount() != 0) {
			tracesFull = model.getGprTracesCount();
			center = scrollable.getMiddleTrace();
			tracesVisible = scrollable.getVisibleNumberOfTrace();
		} else {
			if (scrollable instanceof SensorLineChart) {
				tracesFull = scrollable.getTracesCount();
			}
			center = tracesFull / 2;
			tracesVisible = tracesFull;
		}

		double centerPos =  center / tracesFull * (double) width;
		double rectWidth = tracesVisible / tracesFull * (double) width;
		
		start = centerPos - rectWidth / 2;
		finish = centerPos + rectWidth / 2;
		
		draw();
	}
	
	private void draw() {
		GraphicsContext gc = this.getGraphicsContext2D();	
		gc.clearRect(0, 0, getWidth(), getHeight());
		
		gc.setFill(Color.GRAY);
		gc.fillRect(0, V_GRAY_MARGIN, 
				getWidth(), getHeight() - 2 * V_GRAY_MARGIN);
		
		//gc.setFill(Color.BLUE);
		
		Rectangle c = getCenterBar();

		gc.strokeRoundRect(c.getX() + CENTER_MARGIN, 
				c.getY() + V_MARGIN, 
				c.getWidth() - 2 * CENTER_MARGIN, 
				c.getHeight() - 2 * V_MARGIN, 
				10, 10);
		
		double centerX = c.getX() + c.getWidth() / 2;
		gc.strokeLine(centerX, 0, centerX, HEIGHT);
		
		//gc.setFill(Color.AQUAMARINE);
		Rectangle l = getLeftBar();
		

		gc.strokeRoundRect(l.getX() + V_MARGIN, l.getY() + V_MARGIN, 
				l.getWidth() - 2 * V_MARGIN, l.getHeight() - 2 * V_MARGIN, 10, 10);

		//gc.setFill(Color.AQUAMARINE);
		Rectangle r = getRightBar();
		gc.strokeRoundRect(r.getX() + V_MARGIN, r.getY() + V_MARGIN, 
				r.getWidth() - 2 * V_MARGIN, r.getHeight() - 2 * V_MARGIN, 10, 10);
		
	}
	
	private void recalcField() {
		double scrCenter = (finish + start) / 2;			
		double scrWidth = (finish - start);
		
		double visibletracesCount = scrWidth / (double) getWidth() 
				* (double) scrollable.getTracesCount();
		double hsc = getWidth() / visibletracesCount;
		double aspect = hsc / scrollable.getVScale();
		
		double trCenter = scrCenter / (double) getWidth() * (double) model.getGprTracesCount();
		scrollable.setMiddleTrace((int) trCenter);
		scrollable.setRealAspect(aspect);
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

		//wrapGroup.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
		//	public void handle(final MouseEvent mouseEvent) {
		//		mouseEvent.consume();
		//	}
		//});

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
}
