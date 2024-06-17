package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import com.ugcs.gprvisualizer.app.Navigator;
import com.ugcs.gprvisualizer.gpr.Model;

public class ProfileField {
	
	public static final double ZOOM_A = 1.2;
	public static final double ASPECT_A = 1.14;

	private Model model;
	
	private int selectedTrace = 0;
	private int startSample = 0;
	private int zoom = 1;
	
	private double aspect = -15;
	
	// screen coordinates
	private Dimension viewDimension = new Dimension();
	private Rectangle topRuleRect = new Rectangle();
	private Rectangle leftRuleRect = new Rectangle();
	private Rectangle mainRectRect = new Rectangle();
	private Rectangle infoRect = new Rectangle();

	//draw coordinates
	private Rectangle clipMainRect = new Rectangle();
	private Rectangle clipLeftMainRect = new Rectangle();
	private Rectangle clipTopMainRect = new Rectangle();
	private Rectangle clipInfoRect = new Rectangle();
	
	//
	public int visibleStart;
	public int visibleFinish;
	

	public ProfileField(Model model) {
		this.model  = model;		
	}

	public ProfileField(ProfileField copy) {
		this.model  = copy.model;
		this.selectedTrace = copy.selectedTrace;
		this.startSample = copy.startSample;
		this.zoom = copy.zoom;
		this.vertScale = copy.vertScale;
		this.aspect = copy.aspect;
		this.realAspect = copy.realAspect;
		
		this.visibleStart = copy.visibleStart;
		this.visibleFinish = copy.visibleFinish;
		
		this.topRuleRect = copy.topRuleRect;
		this.leftRuleRect = copy.leftRuleRect;
		this.mainRectRect = copy.mainRectRect;
		this.infoRect = copy.infoRect;
		
		this.clipMainRect = copy.clipMainRect ;
		this.clipLeftMainRect = copy.clipLeftMainRect; 
		this.clipTopMainRect = copy.clipTopMainRect; 
		this.clipInfoRect = copy.clipInfoRect; 	
	}

	public void clear() {
		zoom = 1;
		aspect = -15;		
		startSample = 0;
		if (model.isActive() && model.getGprTracesCount() > 0) {
			fitFull();
		}
	}

	private void fitFull() {		
		setSelectedTrace(model.getGprTracesCount() / 2);
		int maxSamples = model.getMaxHeightInSamples();
		fit(maxSamples * 2, model.getGprTracesCount());
	}

	public void fit(int maxSamples, int tracesCount) {
		double vertScale = (double) getViewDimension().height 
				/ (double) maxSamples;
		double zoom = Math.log(vertScale) / Math.log(ProfileField.ZOOM_A);
		
		setZoom((int) zoom);
		setStartSample(0);
		
		double h = (double) (getViewDimension().width 
				- getLeftRuleRect().width - 20) 
				/ ((double) tracesCount);
		
		double realAspect = h / getVScale();

		setAspectReal(realAspect);		
	}
	
	public TraceSample screenToTraceSample(Point point, VerticalCutPart vcp) {
		int trace = vcp.globalToLocal(getSelectedTrace() 
				+ (int)((point.getX()) / getHScale()));
		
		int sample = getStartSample() 
				+ (int) ((point.getY() - getTopMargin()) / getVScale());
		
		return new TraceSample(trace, sample);
		
	}
	
	public TraceSample screenToTraceSample(Point point) {
	
		int trace = getSelectedTrace() + (int) (-1 + (point.getX()) / getHScale()); 
		int sample = getStartSample() + (int) ((point.getY() - getTopMargin()) / getVScale());
		
		return new TraceSample(trace, sample);
	}
	
	public int traceToScreen(int trace) {
		return (int) ((trace - getSelectedTrace()) * getHScale());
	}

	public int sampleToScreen(int sample) {
		return (int) ((sample - getStartSample()) * getVScale() + getTopMargin());
	}
	
	public Point traceSampleToScreen(TraceSample ts) {
		return new Point(traceToScreen(ts.getTrace()), sampleToScreen(ts.getSample()));
	}

	public Point traceSampleToScreenCenter(TraceSample ts) {
		
		return new Point(
			traceToScreen(ts.getTrace()) + (int) (getHScale() / 2), 
			sampleToScreen(ts.getSample()) + (int) (getVScale() / 2));
	}
	
	public int getVisibleNumberOfTrace() {
		
		Point p = traceSampleToScreen(new TraceSample(0,0));
		Point p2 = new Point(p.x + getMainRect().width, 0); 
		TraceSample t2 = screenToTraceSample(p2);		
		
		return t2.getTrace();
	}
	
	public int getFirstVisibleTrace() {		
		return Math.clamp(screenToTraceSample(new Point(-getMainRect().width / 2, 0)).getTrace(),
				0, model.getGprTracesCount() - 1);
	}

	public int getLastVisibleTrace() {
		return Math.clamp(screenToTraceSample(new Point(getMainRect().width / 2, 0)).getTrace(),
				0, model.getGprTracesCount() - 1);
	}

	public int getLastVisibleSample(int height) {
		return screenToTraceSample(new Point( 0, height)).getSample();
	}
	
	public int getSelectedTrace() {
		return selectedTrace;
	}

	public void setSelectedTrace(int selectedTrace) {
		this.selectedTrace =
			Math.clamp(selectedTrace, 0, 
			Math.max(0, model.getGprTracesCount() - 1));
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

	double vertScale = 1;

	public double getVScale() {	
		return vertScale;
	}

	public double getHScale() {
		return getVScale()*getAspectReal();
	}

	double realAspect = 0.5;

	public double getAspectReal() {
		return realAspect;
	}
	
	public void setAspectReal(double realAspect) {
		this.realAspect = realAspect;
	}
	
	public Dimension getScreenImageSize() {
		
		return new Dimension(
				(int) (model.getGprTracesCount() * getHScale()), 
				(int) (model.getMaxHeightInSamples() * getVScale()));
	}

	public Dimension getViewDimension() {
		return viewDimension;
	}

	public void setViewDimension(Dimension viewDimension) {
		this.viewDimension = viewDimension;
		
		infoRect = new Rectangle(0, 0, Model.TOP_MARGIN - 1, Model.TOP_MARGIN - 1);
		topRuleRect = new Rectangle(Model.TOP_MARGIN, 0, viewDimension.width - Model.TOP_MARGIN, Model.TOP_MARGIN - 1);
		leftRuleRect = new Rectangle(0, Model.TOP_MARGIN - 1, Model.TOP_MARGIN-1, viewDimension.height - Model.TOP_MARGIN);
		mainRectRect = new Rectangle(Model.TOP_MARGIN, Model.TOP_MARGIN, viewDimension.width - Model.TOP_MARGIN, viewDimension.height - Model.TOP_MARGIN);
		
		visibleStart = -mainRectRect.x -mainRectRect.width / 2;
		
		initClipRects();
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
		vertScale = Math.pow(ZOOM_A, getZoom());
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
	
	public Rectangle getInfoRect() {
		return infoRect;
	}

	public Rectangle getClipMainRect() {
		return clipMainRect;		
	}
	
	public Rectangle getClipLeftMainRect() {
		return clipLeftMainRect;
	}
	
	public Rectangle getClipTopMainRect() {
		return clipTopMainRect;
	}
	
	public Rectangle getClipInfoRect() {
		return clipInfoRect;
	}
	
	public void initClipRects() {
		clipMainRect = new Rectangle(
				-getMainRect().width / 2, getMainRect().y, 
				getMainRect().width, getMainRect().height);
		
		clipTopMainRect = new Rectangle(
				-getMainRect().width / 2, 0, 
				getMainRect().width, getMainRect().y + getMainRect().height);

		clipLeftMainRect = new Rectangle(
				-getMainRect().x - getMainRect().width / 2, getMainRect().y, 
				getMainRect().x + getMainRect().width, getMainRect().height);

		clipInfoRect = new Rectangle(
				-getMainRect().x - getMainRect().width / 2, 0, getInfoRect().width, getInfoRect().height);
				
	}
}