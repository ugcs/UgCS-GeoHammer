package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import com.ugcs.gprvisualizer.app.ScrollableData;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;
import javafx.geometry.Point2D;

public class ProfileField extends ScrollableData {
	
	public static final double ASPECT_A = 1.14;

	private final Model model;

	private double aspect = -15;
	
	// screen coordinates
	private Dimension viewDimension = new Dimension();
	private Rectangle topRuleRect = new Rectangle();
	private Rectangle leftRuleRect = new Rectangle();
	private Rectangle infoRect = new Rectangle();

	//draw coordinates
	private Rectangle clipMainRect = new Rectangle();
	private Rectangle clipLeftMainRect = new Rectangle();
	private Rectangle clipTopMainRect = new Rectangle();
	private Rectangle clipInfoRect = new Rectangle();
	
	//
	private int visibleStart;
	//private int visibleFinish;

	private int maxHeightInSamples = 0;
	private Settings profileSettings = new Settings();


	public int getMaxHeightInSamples() {
		return maxHeightInSamples;
	}

	public void updateMaxHeightInSamples() {

		//set index of traces
		int maxHeight = 0;
		for (int i = 0; i < getGprTracesCount(); i++) {
			Trace tr = getGprTraces().get(i);
			maxHeight = Math.max(maxHeight, tr.getNormValues().length);
		}

		this.maxHeightInSamples = maxHeight;
		getProfileSettings().maxsamples = maxHeightInSamples;


		if (getProfileSettings().getLayer() + getProfileSettings().hpage > maxHeightInSamples) {
			getProfileSettings().setLayer(maxHeightInSamples / 4);
			getProfileSettings().hpage = maxHeightInSamples / 4;
		}

	}

	public Settings getProfileSettings() {
		return profileSettings;
	}

	public List<Trace> getGprTraces() {
		return model.getFileManager().getGprTraces();
	}

	public int getGprTracesCount() {
		return getGprTraces().size();
	}

	public ProfileField(Model model) {
		this.model  = model;		
	}

	public int getVisibleStart() {
		return visibleStart;
	}

	/*	public ProfileField(ProfileField copy) {

		this.model  = copy.model;

		setMiddleTrace(copy.getMiddleTrace());
		setRealAspect(copy.getRealAspect());

		this.startSample = copy.startSample;
		this.zoom = copy.zoom;
		this.vertScale = copy.vertScale;
		this.aspect = copy.aspect;

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
	}*/

	@Override
	public void clear() {
		super.clear();
		aspect = -15;
		startSample = 0;
		if (model.isActive() && model.getGprTracesCount() > 0) {
			fitFull();
		}
	}

	private void fitFull() {		
		setMiddleTrace(getGprTracesCount() / 2);
		fit(maxHeightInSamples * 2, getGprTracesCount());
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

		setRealAspect(realAspect);
	}
	
	public TraceSample screenToTraceSample(Point2D point, VerticalCutPart vcp) {
		int trace = vcp.globalToLocal(getMiddleTrace()
				+ (int)((point.getX()) / getHScale()));
		
		int sample = getStartSample() 
				+ (int) ((point.getY() - getTopMargin()) / getVScale());
		
		return new TraceSample(trace, sample);
	}

	public int getVisibleNumberOfTrace() {
		Point2D p = traceSampleToScreen(new TraceSample(0,0));
		Point2D p2 = new Point2D(p.getX() + getMainRect().width, 0);
		TraceSample t2 = screenToTraceSample(p2);		
		
		return t2.getTrace();
	}

	@Override
	public int getTracesCount() {
		return model.getGprTracesCount();
	}

	public int getFirstVisibleTrace() {		
		return Math.clamp(screenToTraceSample(new Point2D(-getMainRect().width / 2, 0)).getTrace(),
				0, model.getGprTracesCount() - 1);
	}

	public int getLastVisibleTrace() {
		return Math.clamp(screenToTraceSample(new Point2D(getMainRect().width / 2, 0)).getTrace(),
				0, model.getGprTracesCount() - 1);
	}

	public int getLastVisibleSample(int height) {
		return screenToTraceSample(new Point2D( 0, height)).getSample();
	}

	public void setStartSample(int startSample) {
		if(getScreenImageSize().height < getViewDimension().height){
			startSample = 0;
		}
		this.startSample = Math.max(0, startSample);
	}

	public Dimension getScreenImageSize() {
		return new Dimension(
				(int) (getGprTracesCount() * getHScale()),
				(int) (getMaxHeightInSamples() * getVScale()));
	}

	public Dimension getViewDimension() {
		return viewDimension;
	}

	public void setViewDimension(Dimension viewDimension) {
		this.viewDimension = viewDimension;

		int leftMargin = 30;
		int ruleWidth = 90;
		
		topRuleRect = new Rectangle(leftMargin, 0, viewDimension.width - leftMargin - ruleWidth, Model.TOP_MARGIN - 1);
		infoRect = new Rectangle(leftMargin + topRuleRect.width, 0, Model.TOP_MARGIN - 1, Model.TOP_MARGIN - 1);
		leftRuleRect = new Rectangle(leftMargin + topRuleRect.width, Model.TOP_MARGIN, ruleWidth, viewDimension.height - leftMargin);
		mainRectRect = new Rectangle(leftMargin, Model.TOP_MARGIN, viewDimension.width - leftMargin - ruleWidth, viewDimension.height - leftMargin);
		
		visibleStart = -mainRectRect.x -mainRectRect.width / 2;
		
		initClipRects();
 	}

	public double getAspect() {
		return aspect;
	}

	public void setAspect(double aspect) {
		this.aspect = aspect;
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