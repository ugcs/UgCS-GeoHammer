package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.math.NumberUtils;

public class Hyperbola implements BaseObject {

	
	//MutableInt tracePinacle = new MutableInt();
	//MutableInt samplePinacle = new MutableInt();
	//MutableInt sampleThick = new MutableInt();
	//MutableInt hyperkfcVal = new MutableInt();
	
	int leftWidth = 30;
	int rightWidth = 30;
	int thickness = 30;
	int hyperkfcInt = 60;
	
	//MutableInt traceStart = new MutableInt();
	//MutableInt traceFinish = new MutableInt();
	//MutableInt sampleStart = new MutableInt();
	//MutableInt sampleFinish = new MutableInt();

	private VerticalCutPart offset;
	DragAnchor pinnacle;
	DragAnchor left;
	DragAnchor right;
	DragAnchor thick;
	DragAnchor hyperkfc;
	
	public Hyperbola(int trace, int sample, VerticalCutPart offset) {
		
		//offset.globalToLocal(traceStart)
		tracePinacle.setValue(offset.globalToLocal(trace));
		traceStart.setValue(offset.globalToLocal(trace - leftWidth));
		traceFinish.setValue(offset.globalToLocal(trace + rightWidth));
		sampleThick.setValue(sample + thickness);
		samplePinacle.setValue(sample);
		hyperkfcVal.setValue(sample + hyperkfcInt);
		
		this.offset = offset;
		
		initDragAnchors();
	}
	
	public Hyperbola(JSONObject json, VerticalCutPart offset) {
		this.offset = offset;
		
		tracePinacle.setValue((int)(long)(Long)json.get("tracePinacle"));
		samplePinacle.setValue((int)(long)(Long)json.get("samplePinacle"));
		thickness = ((int)(long)(Long)json.get("thickness"));
		leftWidth = ((int)(long)(Long)json.get("leftWidth"));
		rightWidth = ((int)(long)(Long)json.get("rightWidth"));
		
		//
		traceStart.setValue(offset.globalToLocal(tracePinacle.intValue() - leftWidth));
		traceFinish.setValue(offset.globalToLocal(tracePinacle.intValue() + rightWidth));
		sampleThick.setValue(samplePinacle.intValue() + thickness);
		
		initDragAnchors();
	}

	private void initDragAnchors() {
		
		hyperkfc = new DragAnchor(ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				hyperkfcInt = hyperkfcVal.intValue() - samplePinacle.intValue();
			}
			
			public int getTrace() {
				return tracePinacle.intValue()+rightWidth;
			}
		};
		
		pinnacle = new DragAnchor(tracePinacle, samplePinacle, ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				traceStart.setValue(tracePinacle.intValue() - leftWidth);
				traceFinish.setValue(tracePinacle.intValue() + rightWidth);
				sampleThick.setValue(samplePinacle.intValue() + thickness);
				hyperkfcVal.setValue(samplePinacle.intValue() + hyperkfcInt);
			}
		};
		
		left = new DragAnchor(traceStart, new MutableInt(), ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				leftWidth = tracePinacle.intValue()-traceStart.intValue();				
			}
			
			public int getSample() {
				return samplePinacle.intValue();
			}
		};
		right = new DragAnchor(traceFinish, new MutableInt(), ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				rightWidth = traceFinish.intValue() - tracePinacle.intValue();
			}
			
			public int getSample() {
				return samplePinacle.intValue();
			}
		};
		thick = new DragAnchor(new MutableInt(), sampleThick, ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				thickness = sampleThick.intValue() - samplePinacle.intValue();
			}
			
			public int getTrace() {
				return tracePinacle.intValue();
			}
		};

	}
	
	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {		
		return isPointInside(localPoint, vField);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, VerticalCutField vField) {
		drawHyperbola(g2, vField);
		
	}

	@Override
	public Rectangle getRect(VerticalCutField vField) {
		Point lt = vField.traceSampleToScreen(new TraceSample(getTraceStartGlobal(), samplePinacle.intValue()-10));
		Point rb = vField.traceSampleToScreen(new TraceSample(getTraceFinishGlobal(), samplePinacle.intValue()+10));
		return new Rectangle(lt.x, lt.y, rb.x - lt.x, rb.y - lt.y);
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BaseObject> getControls() {

			return
				Arrays.asList(pinnacle, left, right, thick, hyperkfc); 

	}

	@Override
	public void saveTo(JSONObject json) {
		json.put("tracePinacle", tracePinacle);
		json.put("samplePinacle", samplePinacle);
		json.put("leftWidth", leftWidth);
		json.put("rightWidth", rightWidth);
		json.put("thickness", thickness);
		
		int y = samplePinacle.intValue();
		
		int[] topCut = getCutArray(y, y);
		
		JSONArray arr = new JSONArray();
		for(int i : topCut) {
			arr.add(i);
		}		
		json.put("topCut", arr);
		
		int[] botCut = getCutArray(y+thickness, y);
		JSONArray arr2 = new JSONArray();
		for(int i : botCut) {
			arr2.add(i);
		}		
		json.put("botCut", arr2);
		
	}

	private int[] getCutArray(int y, int top) {
		int[] topCut = new int[leftWidth+rightWidth];		
		double kf = AppContext.model.getSettings().hyperkfc / 100.0;
		for(int i=0; i<topCut.length; i++) {
			double x=(i-leftWidth) * kf;
			double c = Math.sqrt(x*x+y*y);
			topCut[i] = (int)c - top;
		}
		return topCut;
	}

	@Override
	public boolean mousePressHandle(Point2D point, Field field) {
		return false;
	}

	@Override
	public boolean isPointInside(Point localPoint, VerticalCutField vField) {
		
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	private void drawHyperbola(Graphics2D g2, VerticalCutField vField) {
		
		//Rectangle rect = getRect(vField); 
		//g2.setColor(Color.RED);
		//g2.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		
		g2.setColor(Color.YELLOW);
		//double kf = AppContext.model.getSettings().hyperkfc / 100.0;
		double kf = hyperkfcInt / 100.0;
		int tr = getTracePinacleGlobal();//tracePinacle.intValue();
		double y = samplePinacle.intValue();
		drawHyperbolaLine(g2, vField, kf, tr, y);
		drawHyperbolaLine(g2, vField, kf, tr, y+thickness);
	}

	private void drawHyperbolaLine(Graphics2D g2, VerticalCutField vField, double kf, int tr, double y) {
		Point prev = null;
		
		int s = getTraceStartGlobal(); //tracePinacle.intValue() - leftWidth;
		int f = getTraceFinishGlobal();//tracePinacle.intValue() + rightWidth;
		
		
		for(int i=s; i<= f; i++) {
			
			double x=(i-tr) * kf;
			double c = Math.sqrt(x*x+y*y);
			
			Point lt = vField.traceSampleToScreen(new TraceSample(i, (int)c));
			if(prev != null) {
				g2.drawLine(prev.x, prev.y, lt.x, lt.y);				
			}
			
			prev = lt;
		}
	}

	public int getTracePinacleGlobal() {
		return offset.localToGlobal(this.tracePinacle.getValue());
	}
	
	public int getTraceStartGlobal() {
		return offset.localToGlobal(this.traceStart.getValue());
	}
	
	public int getTraceFinishGlobal() {
		return offset.localToGlobal(this.traceFinish.getValue());
	}
	
	
}
