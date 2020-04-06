package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class ConstPlace extends BaseObjectImpl implements BaseObject, MouseHandler {

	private LatLon latLon;
	private int traceInFile;
	private VerticalCutPart offset;
	static int R_HOR = 8;
	static int R_VER = 5;
		
	public static ConstPlace loadFromJson(JSONObject json, Model model, SgyFile sgyFile) {
		int traceNum = (int)(long)(Long)json.get("trace");
		
		return new ConstPlace(traceNum, null, sgyFile.getOffset());
	}
	
	public ConstPlace(int trace, LatLon latLon,  VerticalCutPart offset) {
		this.offset = offset;
		this.latLon = latLon;
			
		this.traceInFile = trace;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
				
			AppContext.model.getField().setSceneCenter(getTrace().getLatLon());
			
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point, ProfileField vField) {
		
		return false;
	}
	
	

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		
		
		Rectangle rect = getRect(hField);

		g2.setColor(Color.BLACK);
		g2.fillOval(rect.x+1 , rect.y+1, rect.width, rect.height);
		
		g2.setColor(Color.ORANGE);
		g2.fillOval(rect.x , rect.y, rect.width, rect.height);
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		setClip(g2, vField.getClipTopMainRect());
		
		Rectangle rect = getRect(vField);
		
		g2.setColor(Color.BLACK);
		g2.fillOval(rect.x+2 , rect.y+2, rect.width, rect.height);
		
		g2.setColor(Color.ORANGE);
		g2.fillOval(rect.x , rect.y, rect.width, rect.height);
	}
	
	public Rectangle getRect(ProfileField vField) {
		
		int x = vField.traceToScreen(offset.localToGlobal(traceInFile));
				
		Rectangle rect = new Rectangle(x-R_HOR, 5, R_HOR*2, R_VER*2);
		return rect;
	}
	
	public Rectangle getRect(MapField hField) {
		
		Point2D p =  hField.latLonToScreen(latLon);
		
		Rectangle rect = new Rectangle((int)p.getX()-R_HOR, (int)p.getY()-R_VER*2, R_HOR*2, R_VER*2);
		return rect;
	}

	private Trace getTrace() {
		return AppContext.model.getFileManager().getTraces().get(offset.localToGlobal(traceInFile));
	}

	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		
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
	public boolean saveTo(JSONObject json) {
		//json.put("trace", traceInFile);
		return false;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		
		Rectangle r = getRect(field);
		if(r.contains(point)) {
			
			AppContext.model.getVField().setSelectedTrace(offset.localToGlobal(traceInFile));
		
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
			return true;
		}
		
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
