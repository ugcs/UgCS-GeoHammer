package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.gpr.Model;

public class FoundPlace implements BaseObject, MouseHandler {

	//private int trace;
	private Trace trace;
	private VerticalCutPart offset;
	static int R_HOR = ResourceImageHolder.IMG_SHOVEL.getWidth(null)/2;
	static int R_VER = ResourceImageHolder.IMG_SHOVEL.getHeight(null)/2;
		
	public static FoundPlace loadFromJson(JSONObject json, Model model, SgyFile sgyFile) {
		int traceNum = (int)(long)(Long)json.get("trace");
		
		Trace trace = sgyFile.getTraces().get(traceNum);
		return new FoundPlace(trace, sgyFile.getOffset());
	}
	
	public FoundPlace(Trace trace, VerticalCutPart offset) {
		this.offset = offset;
			
		this.trace = trace;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
			//select
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, VerticalCutField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point, VerticalCutField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawOnMap(Graphics2D g2, Field hField) {
		
		Point2D p = hField.latLonToScreen(trace.getLatLon());
		
		Image img = ResourceImageHolder.IMG_SHOVEL;
		g2.drawImage(img, (int)p.getX() - img.getWidth(null)/2 , (int)p.getY() - img.getHeight(null), null);
		//g2.fillOval((int)p.getX()-R, (int)p.getY()-R/2, R*2, R);
	
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, VerticalCutField vField) {
		
		Rectangle rect = getRect(vField);
		
		g2.drawImage(ResourceImageHolder.IMG_SHOVEL, rect.x , rect.y, null);
	}
	
	public Rectangle getRect(VerticalCutField vField) {
		
		int x = vField.traceToScreen(offset.localToGlobal(trace.indexInFile));		
		Rectangle rect = new Rectangle(x-R_HOR, 0, R_HOR*2, R_VER*2);
		return rect;
	}
	

	@Override
	public boolean isPointInside(Point localPoint, VerticalCutField vField) {
		
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BaseObject> getControls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveTo(JSONObject json) {
		json.put("trace", trace.indexInFile);		
	}

}
