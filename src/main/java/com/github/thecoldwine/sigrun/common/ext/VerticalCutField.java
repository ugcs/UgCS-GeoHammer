package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Point;

import com.ugcs.gprvisualizer.gpr.Model;

public class VerticalCutField {

	
	private int topMargin = 0;
	private Model model;
	private int selectedTrace=0;
	private int startSample=0;
	private double vScale=1;
	private double hScale=2;

	public VerticalCutField(Model model, int topMargin) {
		this.model  = model;
		
		this.topMargin = topMargin;
	}

	public VerticalCutField(VerticalCutField copy){
		this.model  = copy.model;
		this.selectedTrace = copy.selectedTrace;
		this.startSample = copy.startSample;
		this.vScale = copy.vScale;
		this.hScale = copy.hScale;
		this.topMargin = copy.topMargin;
	}
	
	public TraceSample screenToTraceSample(Point point) {
	
		int trace = getSelectedTrace() + (int)(point.getX()/getHScale()); 
		int sample = getStartSample() + (int)((point.getY() - topMargin)/getVScale());
		
		return new TraceSample(trace, sample);
	}
	
	public Point traceSampleToScreen(TraceSample ts) {
		
		double x = (ts.getTrace()-getSelectedTrace()) * getHScale(); 
		double y = (ts.getSample()-getStartSample()) * getVScale() + topMargin;
		return new Point((int)x, (int)y);
	}

	public Point traceSampleToScreenCenter(TraceSample ts) {
		
		double x = (ts.getTrace()-getSelectedTrace()) * getHScale() + getHScale()/2; 
		double y = (ts.getSample()-getStartSample()) * getVScale() + topMargin + getVScale()/2;
		return new Point((int)x, (int)y);
	}
	
	public int getVisibleNumberOfTrace(int width) {
		
		Point p = traceSampleToScreen(new TraceSample(0,0));
		Point p2 = new Point(p.x + width, 0); 
		TraceSample t2 = screenToTraceSample(p2);		
		
		return t2.getTrace();
	}
	
	public int getFirstVisibleTrace(int width) {
		
		return 
			Math.max(
			Math.min(
				screenToTraceSample(new Point( -width/2, 0)).getTrace(),
				model.getFileManager().getTraces().size()-1), 0);
	}

	public int getLastVisibleTrace(int width) {
		return 
			Math.max(
			Math.min(
				screenToTraceSample(new Point( width/2, 0)).getTrace(),
				model.getFileManager().getTraces().size()-1), 0);					
	}

	public int getLastVisibleSample(int height) {

		return screenToTraceSample(new Point( 0, height)).getSample();
	}
	
	public int getHWnd() {
		return (int)hScale;
	}

	public int getSelectedTrace() {
		return selectedTrace;
	}

	public void setSelectedTrace(int selectedTrace) {
		this.selectedTrace = Math.max(0, selectedTrace);
	}

	public int getStartSample() {
		return startSample;
	}

	public void setStartSample(int startSample) {
		this.startSample = Math.max(0, startSample);
	}

	public double getVScale() {
		return vScale;
	}

	public void setVScale(double vscale) {
		this.vScale = vscale;
	}

	public double getHScale() {
		return hScale;
	}

	public void setHScale(double hScale) {
		this.hScale = hScale;
	}

}
