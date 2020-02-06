package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Dimension;
import java.awt.Point;

import com.ugcs.gprvisualizer.gpr.Model;

public class ProfileField {

	
	public static final double ZOOM_A = 1.2;
	public static final double ASPECT_A = 1.14;
	
	private int topMargin = 0;
	private Model model;
	private int selectedTrace=0;
	private int startSample=0;
	//private double vScale=1;
	private int zoom = 1;
	
	private double aspect = -15;
	
	//private double hScale=2;
	
	private Dimension viewDimension = new Dimension();

	public ProfileField(Model model, int topMargin) {
		this.model  = model;
		
		this.topMargin = topMargin;
	}

	public ProfileField(ProfileField copy){
		this.model  = copy.model;
		this.selectedTrace = copy.selectedTrace;
		this.startSample = copy.startSample;
		this.zoom = copy.zoom;
		this.aspect = copy.aspect;
		this.topMargin = copy.topMargin;
	}

	public void clear() {
		System.out.println("VerticalCutField clear");
		//vScale=1;
		//hScale=2;
		zoom = 1;
		aspect = -15;		
		startSample = 0;
		selectedTrace = model.getFileManager().getFiles().get(0).getTraces().size()/3;

	}
	
	public TraceSample screenToTraceSample(Point point, VerticalCutPart vcp) {
		int trace = vcp.globalToLocal(getSelectedTrace() + (int)(point.getX()/getHScale())); 
		int sample = getStartSample() + (int)((point.getY() - topMargin)/getVScale());
		
		return new TraceSample(trace, sample);
		
	}
	
	public TraceSample screenToTraceSample(Point point) {
	
		int trace = getSelectedTrace() + (int)(point.getX()/getHScale()); 
		int sample = getStartSample() + (int)((point.getY() - topMargin)/getVScale());
		
		return new TraceSample(trace, sample);
	}
	
	public int traceToScreen(int trace) {
		return (int)((trace-getSelectedTrace()) * getHScale());
	}

	public int sampleToScreen(int sample) {
		return (int)((sample-getStartSample()) * getVScale() + topMargin);
	}
	
	public Point traceSampleToScreen(TraceSample ts) {
		return new Point(traceToScreen(ts.getTrace()), sampleToScreen(ts.getSample()));
	}

	public Point traceSampleToScreenCenter(TraceSample ts) {
		
		double x = (ts.getTrace()-getSelectedTrace()) * getHScale() + getHScale()/2; 
		double y = (ts.getSample()-getStartSample()) * getVScale() + topMargin + getVScale()/2;
		return new Point(
			traceToScreen(ts.getTrace()) + (int)(getHScale()/2), 
			sampleToScreen(ts.getSample()) + (int)(getVScale()/2));
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
	
//	public int getHWnd() {
//		return (int)hScale;
//	}

	public int getSelectedTrace() {
		return selectedTrace;
	}

	public void setSelectedTrace(int selectedTrace) {
		this.selectedTrace =
			Math.min(
				Math.max(0, selectedTrace),
				model.getFileManager().getTraces().size()-1);
	}

	public int getStartSample() {
		return startSample;
	}

	public void setStartSample(int startSample) {
		
		if(getScreenImageSize().height < getViewDimension().height){
			startSample = 0;
		}		
		
		this.startSample = Math.max(0, startSample);
	}

	public double getVScale() {
		
		double s = Math.pow(ZOOM_A, getZoom());
		
		return s;
	}

//	public void setVScale(double vscale) {
//		this.vScale = vscale;
//	}

	public double getHScale() {
		return getVScale()*getAspectReal();
	}

	private double getAspectReal() {
		return Math.pow(ASPECT_A, getAspect());
	}

//	public void setHScale(double hScale) {
//		this.hScale = hScale;
//	}

	public Dimension getScreenImageSize() {
		
		return new Dimension(
				(int)(model.getFileManager().getTraces().size() * getHScale()), 
				(int)(model.getMaxHeightInSamples() * getVScale()));
	}

	public Dimension getViewDimension() {
		return viewDimension;
	}

	public void setViewDimension(Dimension viewDimension) {
		this.viewDimension = viewDimension;
	}

	public double getAspect() {
		return aspect;
	}

	public void setAspect(double aspect) {
		this.aspect = aspect;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}
}
