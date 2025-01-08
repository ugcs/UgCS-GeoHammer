package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.Settings;

public class ProfileField {

	//private final Model model;
	private final SgyFile sgyFile;

	// screen coordinates
	private Dimension viewDimension = new Dimension();
	private Rectangle topRuleRect = new Rectangle();
	private Rectangle leftRuleRect = new Rectangle();
	private Rectangle infoRect = new Rectangle();
	private Rectangle mainRectRect = new Rectangle();

	//draw coordinates
	private Rectangle clipMainRect = new Rectangle();
	private Rectangle clipLeftMainRect = new Rectangle();
	private Rectangle clipTopMainRect = new Rectangle();
	private Rectangle clipInfoRect = new Rectangle();
	
	//
	private int visibleStart;
	//private int visibleFinish;

	private int maxHeightInSamples = 0;
	private final Settings profileSettings = new Settings();


	public int getMaxHeightInSamples() {
		return maxHeightInSamples;
	}

	private void updateMaxHeightInSamples() {

		//set index of traces
		int maxHeight = 0;
		for (Trace tr: getGprTraces()) {
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

	List<Trace> gprTraces = new java.util.ArrayList<>();

	public List<Trace> getGprTraces() {
		if (gprTraces.isEmpty()) {
			int traceIndex = 0;
			for (SgyFile file : List.of(sgyFile)) {
				if (file instanceof CsvFile) {
					continue;
				}
				for (Trace trace : file.getTraces()) {
					gprTraces.add(trace);
					trace.setIndexInSet(traceIndex++);
				}
			}
		}
		return gprTraces;
	}

	public int getGprTracesCount() {
		return getGprTraces().size();
	}

	public ProfileField(SgyFile sgyFile) {
		this.sgyFile  = sgyFile;
		updateMaxHeightInSamples();
	}

	public int getVisibleStart() {
		return visibleStart;
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

	public int getTopMargin() {
		return mainRectRect.y;
	}
}