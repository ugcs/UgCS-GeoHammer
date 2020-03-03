package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import com.ugcs.gprvisualizer.gpr.Model;

public class ProfileField {

	
	public static final double ZOOM_A = 1.2;
	public static final double ASPECT_A = 1.14;
	

	private Model model;
	private int selectedTrace=0;
	private int startSample=0;
	private int zoom = 1;
	
	private double aspect = -15;
	
	private Dimension viewDimension = new Dimension();
	private Rectangle topRuleRect = new Rectangle();
	private Rectangle leftRuleRect = new Rectangle();
	private Rectangle mainRectRect = new Rectangle();
	
	
	//
	public int visibleStart;
	public int visibleFinish;
	

	public ProfileField(Model model) {
		this.model  = model;		
		
	}

	public ProfileField(ProfileField copy){
		this.model  = copy.model;
		this.selectedTrace = copy.selectedTrace;
		this.startSample = copy.startSample;
		this.zoom = copy.zoom;
		this.aspect = copy.aspect;
		//this.topMargin = copy.getTopMargin();
		this.realAspect = copy.realAspect;
		
		this.visibleStart = copy.visibleStart;
		this.visibleFinish = copy.visibleFinish;
		
		this.topRuleRect = copy.topRuleRect;
		this.leftRuleRect = copy.leftRuleRect;
		this.mainRectRect = copy.mainRectRect;
	}

	public void clear() {
		System.out.println("VerticalCutField clear");
		zoom = 1;
		aspect = -15;		
		startSample = 0;
		if(model.isActive()) {
			selectedTrace = model.getFileManager().getFiles().get(0).getTraces().size()/3;
		}

	}
	
	public TraceSample screenToTraceSample(Point point, VerticalCutPart vcp) {
		int trace = vcp.globalToLocal(getSelectedTrace() + (int)((point.getX())/getHScale())); 
		int sample = getStartSample() + (int)((point.getY() - getTopMargin())/getVScale());
		
		return new TraceSample(trace, sample);
		
	}
	
	public TraceSample screenToTraceSample(Point point) {
	
		int trace = getSelectedTrace() + (int)((point.getX() )/getHScale()); 
		int sample = getStartSample() + (int)((point.getY() - getTopMargin())/getVScale());
		
		return new TraceSample(trace, sample);
	}
	
	public int traceToScreen(int trace) {
		return (int)((trace-getSelectedTrace()) * getHScale());
	}

	public int sampleToScreen(int sample) {
		return (int)((sample-getStartSample()) * getVScale() + getTopMargin());
	}
	
	public Point traceSampleToScreen(TraceSample ts) {
		return new Point(traceToScreen(ts.getTrace()), sampleToScreen(ts.getSample()));
	}

	public Point traceSampleToScreenCenter(TraceSample ts) {
		
		//double x = (ts.getTrace()-getSelectedTrace()) * getHScale() + getHScale()/2; 
		//double y = (ts.getSample()-getStartSample()) * getVScale() + topMargin + getVScale()/2;
		return new Point(
			traceToScreen(ts.getTrace()) + (int)(getHScale()/2), 
			sampleToScreen(ts.getSample()) + (int)(getVScale()/2));
	}
	
	public int getVisibleNumberOfTrace() {
		
		Point p = traceSampleToScreen(new TraceSample(0,0));
		Point p2 = new Point(p.x + getMainRect().width, 0); 
		TraceSample t2 = screenToTraceSample(p2);		
		
		return t2.getTrace();
	}
	
	public int getFirstVisibleTrace() {
		
		return 
			Math.max(
			Math.min(
				screenToTraceSample(new Point(-getMainRect().width/2, 0)).getTrace(),
				model.getFileManager().getTraces().size()-1), 0);
	}

	public int getLastVisibleTrace() {
		return 
			Math.max(
			Math.min(
				screenToTraceSample(new Point(getMainRect().width/2, 0)).getTrace(),
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

//	private double getAspectReal() {
//		return Math.pow(ASPECT_A, getAspect());
//	}

	double realAspect = 0.5;
	public double getAspectReal() {
		return realAspect;
	}
	public void setAspectReal(double realAspect) {
		this.realAspect = realAspect;
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
		
		topRuleRect = new Rectangle(Model.TOP_MARGIN, 0, viewDimension.width-Model.TOP_MARGIN, Model.TOP_MARGIN-1);
		leftRuleRect = new Rectangle(0, Model.TOP_MARGIN-1, Model.TOP_MARGIN-1, viewDimension.height - Model.TOP_MARGIN);
		mainRectRect = new Rectangle(Model.TOP_MARGIN, Model.TOP_MARGIN, viewDimension.width-Model.TOP_MARGIN, viewDimension.height - Model.TOP_MARGIN);
		
		visibleStart = -mainRectRect.x -mainRectRect.width/2;
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

	private int getTopMargin() {
		return mainRectRect.y;
	}
	
	public Rectangle getTopRuleRect() {
		return topRuleRect;
	}
	
	public Rectangle getLeftRuleRect() {
		return leftRuleRect;
	}
	
	public Rectangle getMainRect() {
		return mainRectRect;
	}
	
}

