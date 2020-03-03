package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProfileView;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

public class FoundPlace extends BaseObjectImpl implements BaseObject, MouseHandler {

	static int R_HOR = ResourceImageHolder.IMG_SHOVEL.getWidth(null)/2;
	static int R_VER = ResourceImageHolder.IMG_SHOVEL.getHeight(null)/2;
	static int R_HOR_M = ShapeHolder.flag.getBounds().width/2;
	static int R_VER_M = ShapeHolder.flag.getBounds().height/2;

	public static Stroke SELECTED_STROKE = new BasicStroke(2.0f);
	
	final static float dash1[] = {7.0f, 2.0f};
	static Stroke VERTICAL_STROKE = 	
			new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, dash1, 0.0f);

	private Color flagColor = Color.getHSBColor((float)Math.random(), 0.9f, 0.97f); 
	private int traceInFile;
	private VerticalCutPart offset;	
	
	public int getTraceInFile() {
		return traceInFile;
	}
	
	public static FoundPlace loadFromJson(JSONObject json, SgyFile sgyFile) {
		int traceNum = (int)(long)(Long)json.get("trace");		
		return new FoundPlace(traceNum, sgyFile.getOffset());
	}
	
	public FoundPlace(int trace, VerticalCutPart offset) {
		this.offset = offset;
			
		this.traceInFile = trace;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		
		if(isPointInside(localPoint, vField)) {
				
			AppContext.model.getField().setSceneCenter(getTrace().getLatLon());
			
			AppContext.notifyAll(new WhatChanged(Change.mapscroll));
			
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
		
		TraceSample ts = vField.screenToTraceSample(point, offset);
		
		traceInFile = Math.min( offset.getTraces()-1, Math.max(0, ts.getTrace()));
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
		
		return true;
	}
	
	

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		
		//Point2D p1 = hField.latLonToScreen(trace.getLatLon());
		//Point2D p2 = hField.latLonToScreen(trace2.getLatLon());
		//g2.setColor(Color.CYAN);
		//g2.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
		
		Rectangle rect = getRect(hField);
		
		//g2.drawImage(ResourceImageHolder.IMG_SHOVEL, rect.x , rect.y, null);
		
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x , rect.y+rect.height);
		
		
		g2.fill(ShapeHolder.flag);
		
		g2.setColor(Color.BLACK);
		g2.draw(ShapeHolder.flag);
		g2.translate(-rect.x , -(rect.y+rect.height));
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		
		Rectangle rect = getRect(vField);
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x , rect.y+rect.height);
		g2.fill(ShapeHolder.flag);
		
		if(isSelected()) {
			g2.setColor(Color.green);
			g2.setStroke(SELECTED_STROKE);
			g2.draw(ShapeHolder.flag);
			
			g2.setStroke(VERTICAL_STROKE);
			g2.setColor(Color.blue);
			g2.setXORMode(Color.gray);
			g2.drawLine(0 , 0, 0 , vField.sampleToScreen(offset.getMaxSamples()) - Model.TOP_MARGIN );
			g2.setPaintMode();
		}
		
		g2.translate(-rect.x , -(rect.y+rect.height));
		
	}
	
	public Rectangle getRect(ProfileField vField) {
		
		int x = vField.traceToScreen(offset.localToGlobal(traceInFile));
				
		Rectangle rect = new Rectangle(x, Model.TOP_MARGIN-R_VER_M*2, R_HOR_M*2, R_VER_M*2);
		return rect;
	}
	
	public Rectangle getRect(MapField hField) {
		
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
