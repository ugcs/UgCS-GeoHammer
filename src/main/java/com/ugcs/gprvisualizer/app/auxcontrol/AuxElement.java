package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.Field;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;

public class AuxElement {

	Trace traceStart;
	//Trace traceFinish;
	Integer sampleStart;
	//Integer sampleFinish;
	
	boolean selected;	
	
	
	private static final int r = 5;		
	
	public AuxElement(Trace traceStart, Integer sampleStart) {
		this.traceStart = traceStart;
		this.sampleStart = sampleStart;
		
	}
	
	public void drawOnCut(Graphics2D g2, VerticalCutField field) {
		
		
		TraceSample ts = new TraceSample(traceStart.indexInSet, sampleStart != null ? sampleStart : 0);
		Point scr = field.traceSampleToScreen(ts);
		
		
		g2.setColor(Color.MAGENTA);
		g2.fillOval(scr.x-r, scr.y-r, r*2, r*2);
	}

	public void drawOnMap(Graphics2D g2, Field field) {
		
		Point2D scr = field.latLonToScreen(traceStart.getLatLon());
		
		g2.fillOval((int)scr.getX()-r, (int)scr.getY()-r, r*2, r*2);
		
	}

	public Trace getTraceStart() {
		return traceStart;
	}

	public void setTraceStart(Trace t) {
		traceStart = t;
	}
	
//	public Trace getTraceFinish() {
//		return traceFinish;
//	}
	
	public Integer getSampleStart() {
		return sampleStart;
	}

	public void setSampleStart(Integer s) {
		sampleStart = s;
	}
	
//	public Integer getSampleFinish() {
//		return sampleFinish;
//	}
	
}
