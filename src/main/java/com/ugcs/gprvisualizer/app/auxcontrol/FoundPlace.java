package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
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
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.CleverImageView;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class FoundPlace implements BaseObject, MouseHandler {

	private Color flagColor = Color.getHSBColor((float)Math.random(), 1, 1f); 
	private int traceInFile;
	private VerticalCutPart offset;
	static int R_HOR = ResourceImageHolder.IMG_SHOVEL.getWidth(null)/2;
	static int R_VER = ResourceImageHolder.IMG_SHOVEL.getHeight(null)/2;
		
	static int R_HOR_M = ShapeHolder.flag.getBounds().width/2;
	static int R_VER_M = ShapeHolder.flag.getBounds().height/2;
	
	
	public static FoundPlace loadFromJson(JSONObject json, SgyFile sgyFile) {
		int traceNum = (int)(long)(Long)json.get("trace");		
		//Trace trace = sgyFile.getTraces().get(traceNum);
		
		//int traceNum2 = json.get("trace2") != null ? (int)(long)(Long)json.get("trace2") : traceNum;		
		//Trace trace2 = sgyFile.getTraces().get(traceNum2);
		
		//Trace traceMidl = sgyFile.getTraces().get((traceNum + traceNum2)/2);
		
		return new FoundPlace(traceNum, sgyFile.getOffset());
	}
	
	public FoundPlace(int trace, VerticalCutPart offset) {
		this.offset = offset;
			
		this.traceInFile = trace;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
				
			AppContext.model.getField().setSceneCenter(getTrace().getLatLon());
			
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
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
		
		TraceSample ts = vField.screenToTraceSample(point, offset);
		
		traceInFile = Math.min( offset.getTraces()-1, Math.max(0, ts.getTrace()));
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
		
		return true;
	}
	
	

	@Override
	public void drawOnMap(Graphics2D g2, Field hField) {
		
		//Point2D p1 = hField.latLonToScreen(trace.getLatLon());
		//Point2D p2 = hField.latLonToScreen(trace2.getLatLon());
		//g2.setColor(Color.CYAN);
		//g2.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
		
		Rectangle rect = getRect(hField);
		
		//g2.drawImage(ResourceImageHolder.IMG_SHOVEL, rect.x , rect.y, null);
		
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x , rect.y+rect.height);
		g2.fill(ShapeHolder.flag);
		g2.translate(-rect.x , -(rect.y+rect.height));
	}

	@Override
	public void drawOnCut(Graphics2D g2, VerticalCutField vField) {
		
		Rectangle rect = getRect(vField);
		
		//int x1 = vField.traceToScreen(offset.localToGlobal(traceInFile));
		
//		g2.setColor(Color.CYAN);
//		g2.drawLine(x1, R_VER*2-3, x2, R_VER*2-3);
//		g2.drawLine(x1, R_VER*2-0, x1, R_VER*2-10);
//		g2.drawLine(x2, R_VER*2-0, x2, R_VER*2-10);
		
		//g2.drawImage(ResourceImageHolder.IMG_SHOVEL, rect.x , rect.y, null);
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x , rect.y+rect.height);
		g2.fill(ShapeHolder.flag);
		g2.translate(-rect.x , -(rect.y+rect.height));
		
	}
	
	public Rectangle getRect(VerticalCutField vField) {
		
		//int x = vField.traceToScreen(offset.localToGlobal(trace.indexInFile));
		
		//int x1 = vField.traceToScreen(offset.localToGlobal(trace.indexInFile));
		//int x2 = vField.traceToScreen(offset.localToGlobal(trace2.indexInFile));
		//int x = (x1+x2)/2;
		int x = vField.traceToScreen(offset.localToGlobal(traceInFile));
				
		Rectangle rect = new Rectangle(x, Model.TOP_MARGIN-R_VER_M*2, R_HOR_M*2, R_VER_M*2);
		return rect;
	}
	
	public Rectangle getRect(Field hField) {
		
		//Point2D p1 = hField.latLonToScreen(trace.getLatLon());
		//Point2D p2 = hField.latLonToScreen(trace2.getLatLon());		
		//Point2D p = new Point2D.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
		
		
		
		Trace tr = getTrace();		
		Point2D p =  hField.latLonToScreen(tr.getLatLon());		
		
		Rectangle rect = new Rectangle((int)p.getX(), (int)p.getY()-R_VER_M*2, R_HOR_M*2, R_VER_M*2);
		return rect;
	}

	private Trace getTrace() {
		return AppContext.model.getFileManager().getTraces().get(offset.localToGlobal(traceInFile));
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
		json.put("trace", traceInFile);		
	}

	@Override
	public boolean mousePressHandle(Point2D point, Field field) {
		
		Rectangle r = getRect(field);
		if(r.contains(point)) {
			
			AppContext.model.getVField().setSelectedTrace(offset.localToGlobal(traceInFile));
		
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
			System.out.println(" found place trace " + traceInFile );
			
			return true;
		}
		
		return false;
	}

	@Override
	public BaseObject copy(int traceoffset, VerticalCutPart verticalCutPart) {
		FoundPlace result = new FoundPlace(traceInFile-traceoffset, verticalCutPart); 
		
		return result;
	}

	@Override
	public boolean isFit(int begin, int end) {
		
		return traceInFile >= begin && traceInFile <=end;
	}

}
