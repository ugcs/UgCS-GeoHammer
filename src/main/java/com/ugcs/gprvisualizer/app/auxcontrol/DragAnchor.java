package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.MouseHandler;

public class DragAnchor extends BaseObjectImpl implements BaseObject, MouseHandler{

	//private static final int R = 5;
	
	private MutableInt trace = new MutableInt();
	public MutableInt getTraceMtl() {
		return trace;
	}
	
	private MutableInt sample = new MutableInt();
	public MutableInt getSampleMtl() {
		return sample;
	}
	
	private AlignRect alignRect;
	private VerticalCutPart offset;
	
	private Image img;
	
	private Dimension dim = new Dimension(16, 16);
	
	private boolean visible = true;
	
	
	public DragAnchor(				
			//MutableInt trace,
			//MutableInt sample,
			Image img,
			AlignRect alignRect,
			VerticalCutPart offset) {
		
		this.offset = offset;
		//this.trace = trace;
		//this.sample = sample;
		this.setImg(img);
		this.alignRect = alignRect;
		
		if(img != null) {
			dim = new Dimension(img.getWidth(null), img.getHeight(null));
		}
					
	}
	
	public void signal(Object obj) {
		
	}
	
	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		//is not visible on the map view
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		if(!isVisible()) {
			return;
		}

		g2.setClip(vField.getClipMainRect().x, vField.getClipMainRect().y, vField.getClipMainRect().width, vField.getClipMainRect().height);
		
		Rectangle rect = getRect(vField);
		if(getImg() == null) {
			g2.setColor(Color.MAGENTA);
			g2.fillOval(rect.x, rect.y, rect.width, rect.height);
		}else {
			g2.drawImage(getImg(), rect.x, rect.y, null);
		}		
	}

	public Rectangle getRect(ProfileField vField) {
		TraceSample ts = new TraceSample(offset.localToGlobal(this.getTrace()), getSample());
		Point scr = vField.traceSampleToScreen(ts);		
		Rectangle rect = alignRect.getRect(scr, dim); //new Rectangle(scr.x-R, scr.y-R, R*2, R*2);
		return rect;
	}

	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		if(!isVisible()) {
			return false;
		}
		
		
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		
		if(isPointInside(localPoint, vField)) {
			signal(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {

		
		
		return true;
	}

	@Override
	public boolean mouseMoveHandle(Point point, ProfileField vField) {
		if(!isVisible()) {
			return false;
		}

		TraceSample ts = vField.screenToTraceSample(point, offset);
		setTrace(ts.getTrace());
		setSample(ts.getSample());
		
		signal(null);
		return true;
	}
	
	public int getTrace() {
		return trace.getValue();
	}

	public void setTrace(int t) {
		trace.setValue(t);
	}
	
	public int getSample() {
		return sample.getValue();
	}

	public void setSample(int s) {
		sample.setValue(s);
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
	public boolean saveTo(JSONObject json) {
		
		return false;
		
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFit(int begin, int end) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
