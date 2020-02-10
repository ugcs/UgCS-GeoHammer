package com.ugcs.gprvisualizer.app;

import java.awt.Point;

import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;

public class ProfileScroll extends Canvas{

	Model model = AppContext.model;
	static final int HEIGHT = 30;
	static final int SIDE_WIDTH = 20; 
	double start;
	double finish;
	
	interface MouseSInput{
		boolean isInside(Point2D localPoint);
		void move(Point2D localPoint);
	}
	
	MouseSInput leftInput = new MouseSInput() {

		@Override
		public boolean isInside(Point2D localPoint) {
			return getLeftBar().contains(localPoint);
		}

		@Override
		public void move(Point2D localPoint) {
			
			//start = ;
		}
		
	};
	
	
	public ProfileScroll(){
		setWidth(400);
		setHeight(HEIGHT);		
		
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
        
         //makeDraggable(new Label("ssdf"));
        //add
        //setB
	}
	
	Rectangle getCenterBar() {
		return new Rectangle(start, 0, finish-start, HEIGHT);
	}
	Rectangle getLeftBar() {
		return new Rectangle(start-SIDE_WIDTH , 0, SIDE_WIDTH, HEIGHT);
	}
	Rectangle getRightBar() {
		return new Rectangle(finish, 0, SIDE_WIDTH, HEIGHT);
	}
	
	void draw() {
		
		if(!model.getFileManager().isActive()) {
			return;
		}
		
		int width = (int)getWidth();
		int height = (int)getHeight();
		
		//System.out.println(" profile scroll   " +width + " " +height );
		
		double tracesFull = model.getFileManager().getTraces().size();
		double center = model.getVField().getSelectedTrace();
		
		double tracesVisible = model.getVField().getVisibleNumberOfTrace(width);
		
		double centerPos =  center / tracesFull * (double)width;
		double rectWidth = tracesVisible / tracesFull * (double)width;
		
		start = centerPos-rectWidth/2;
		finish = centerPos+rectWidth/2;
		
		GraphicsContext gc = this.getGraphicsContext2D();	
		gc.clearRect(0, 0, width, height);
		
		
		gc.setFill(Color.BLUE);
		Rectangle c = getCenterBar();
		gc.fillRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
		
		gc.setFill(Color.AQUAMARINE);
		Rectangle l = getLeftBar();
		gc.fillRect(l.getX(), l.getY(), l.getWidth(), l.getHeight());

		gc.setFill(Color.AQUAMARINE);
		Rectangle r = getRightBar();
		gc.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
		
		//gc.fillRect(10, 5, 20, 20);
		//gc.fillRect(width-30, 5, 20, 20);
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
	
	// -[- -|- -]-
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
				//if (dragModeActiveProperty.get()) {
					// disable mouse events for all children
					mouseEvent.consume();
				//}
			}
		});

		wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				//if (dragModeActiveProperty.get()) {
					// remember initial mouse cursor coordinates
					// and node position
					dragContext.mouseAnchorX = mouseEvent.getX();
					dragContext.mouseAnchorY = mouseEvent.getY();
					dragContext.initialTranslateX = node.getTranslateX();
					dragContext.initialTranslateY = node.getTranslateY();
				//}
			}
		});

		wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				//if (dragModeActiveProperty.get()) {
					// shift node from its initial position by delta
					// calculated from mouse cursor movement
					node.setTranslateX(dragContext.initialTranslateX + mouseEvent.getX() - dragContext.mouseAnchorX);
					node.setTranslateY(dragContext.initialTranslateY + mouseEvent.getY() - dragContext.mouseAnchorY);
				//}
			}
		});

		return wrapGroup;

	}

}
