package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.MouseHandler;

public class DragAnchor implements BaseObject, MouseHandler{

	private static final int R = 5;
	
	private VerticalCutField vField;	
	private int trace;
	private int sample;
	private AlignRect alignRect;
	
	private Image img;
	
	private Dimension dim = new Dimension(16, 16);
	
	private boolean visible = true;
	
	
	public DragAnchor(
			VerticalCutField vField,	
			int trace,
			int sample,
			Image img,
			AlignRect alignRect) {
		
		this.vField = vField;	
		this.trace = trace;
		this.sample = sample;
		this.setImg(img);
		this.alignRect = alignRect;
		
		if(img != null) {
			dim = new Dimension(img.getWidth(null), img.getHeight(null));
		}
					
	}
	
	public void signal(Object obj) {
		
	}
	
	@Override
	public void drawOnMap(Graphics2D g2) {
		
	}

	@Override
	public void drawOnCut(Graphics2D g2) {
		if(!isVisible()) {
			return;
		}
		
		Rectangle rect = getRect();
		if(getImg() == null) {
			g2.setColor(Color.MAGENTA);
			g2.fillOval(rect.x, rect.y, rect.width, rect.height);
		}else {
			g2.drawImage(getImg(), rect.x, rect.y, null);
		}		
	}

	protected Rectangle getRect() {
		TraceSample ts = new TraceSample(getTrace(), getSample());
		Point scr = vField.traceSampleToScreen(ts);		
		Rectangle rect = alignRect.getRect(scr, dim); //new Rectangle(scr.x-R, scr.y-R, R*2, R*2);
		return rect;
	}

	@Override
	public boolean isPointInside(Point localPoint) {
		if(!isVisible()) {
			return false;
		}
		
		
		Rectangle rect = getRect();
		
		return rect.contains(localPoint);
	}

	@Override
	public boolean mousePressHandle(Point localPoint) {
		
		if(isPointInside(localPoint)) {
			signal(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint) {

		
		
		return true;
	}

	@Override
	public boolean mouseMoveHandle(Point point) {
		if(!isVisible()) {
			return false;
		}

		TraceSample ts = vField.screenToTraceSample(point);
		setTrace(ts.getTrace());
		setSample(ts.getSample());
		
		signal(null);
		return true;
	}
	
	public int getTrace() {
		return trace;
	}

	public void setTrace(int t) {
		trace = t;
	}
	
	public int getSample() {
		return sample;
	}

	public void setSample(int s) {
		sample = s;
	}

	public List<BaseObject> getControls(){
		return null;
	}

	protected Image getImg() {
		return img;
	}

	protected void setImg(Image img) {
		this.img = img;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public void saveTo(JSONObject json) {
		// TODO Auto-generated method stub
		
	}
	
}
