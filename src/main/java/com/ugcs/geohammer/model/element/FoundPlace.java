package com.ugcs.geohammer.model.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

import com.ugcs.geohammer.chart.Chart;
import com.ugcs.geohammer.chart.gpr.ProfileField;
import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.format.gpr.Trace;
import com.ugcs.geohammer.format.TraceFile;
import com.ugcs.geohammer.model.TraceKey;
import com.ugcs.geohammer.model.TraceSample;
import com.ugcs.geohammer.chart.gpr.GPRChart;
import com.ugcs.geohammer.chart.ScrollableData;
import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.model.event.WhatChanged;
import com.ugcs.geohammer.util.Check;
import javafx.geometry.Point2D;

import com.ugcs.geohammer.AppContext;
import com.ugcs.geohammer.model.Model;

public class FoundPlace extends PositionalObject {

	private static final int ICON_WIDTH = ShapeHolder.FLAG.getBounds().width;

	private static final int ICON_HEIGHT = ShapeHolder.FLAG.getBounds().height;

	public static final Color SELECTED_COLOR = Color.green;

	public static final Stroke SELECTED_STROKE = new BasicStroke(2.0f);

	private static final Stroke VERTICAL_STROKE = new BasicStroke(1.0f);

	public static final int FLAG_POLE_HEIGHT = 18;

	private Color flagColor = Color.getHSBColor((float) Math.random(), 0.9f, 0.97f);

	private final Model model;

	private TraceKey trace;

	public FoundPlace(TraceKey trace, Model model) {
		Check.notNull(trace);

		this.model = model;
		this.trace = trace;
	}

	public TraceKey getTrace() {
		return trace;
	}

	@Override
	public int getTraceIndex() {
		return trace.getIndex();
	}

	@Override
	public void offset(int traceOffset) {
		trace.offset(traceOffset);
	}

	public Color getFlagColor() {
		return flagColor;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		Rectangle r = getRect(field);
		if (r.contains(point.getX(), point.getY())) {
			ScrollableData scrollable = model.getChart(trace.getFile());
			int traceIndex = getTraceIndex();
			if (scrollable != null) {
				scrollable.scrollToTrace(traceIndex);
			}

			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));

			coordinatesToStatus();
			return true;
		}
		return false;
	}

	public void coordinatesToStatus() {
		if (trace == null) {
			return;
		}
		LatLon latlon = trace.getLatLon();
		if (latlon != null) {
			AppContext.status.showProgressText(latlon.toString());
		}
	}
	
	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
		if (profField instanceof SensorLineChart || isPointInside(localPoint, profField)) {
			model.getMapField().setSceneCenter(getLatLon());
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapscroll));
			coordinatesToStatus();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point2D point, ScrollableData profField) {
		if (trace.getFile() instanceof TraceFile traceFile) {
			GPRChart chart = model.getGprChart(traceFile);
			if (chart != null) {
				TraceSample sample = profField.screenToTraceSample(point);
				List<Trace> traces = traceFile.getTraces();
				int traceIndex = Math.clamp(sample.trace(), 0, traces.size() - 1);
				trace = new TraceKey(traceFile, traceIndex);
				model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
			}
		}
		coordinatesToStatus();
		return true;
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField mapField) {
		Rectangle rect = getRect(mapField);
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x, rect.y + rect.height);
		
		g2.fill(ShapeHolder.FLAG);
		
		g2.setColor(Color.BLACK);
		g2.draw(ShapeHolder.FLAG);
		g2.translate(-rect.x, -(rect.y + rect.height));
	}

	@Override
	public void drawOnCut(Graphics2D g2, ScrollableData scrollableData) {
		if (scrollableData instanceof GPRChart gprChart) {
			var profField = gprChart.getField();

			g2.setClip(profField.getClipTopMainRect().x,
					profField.getClipTopMainRect().y,
					profField.getClipTopMainRect().width,
					profField.getClipTopMainRect().height);

			Rectangle rect = getRect(gprChart);
			g2.translate(rect.x, rect.y + rect.height);

			g2.setColor(flagColor);
			g2.setStroke(VERTICAL_STROKE);
			if (isSelected()) {
				g2.setColor(SELECTED_COLOR);
			}
			g2.fill(ShapeHolder.FLAG);

			if (isSelected()) {
				g2.setColor(flagColor);
				g2.setStroke(SELECTED_STROKE);
				g2.draw(ShapeHolder.FLAG);
			}

			int maxSamples = profField.getMaxHeightInSamples();
			g2.drawLine(1, 0, 1, gprChart.sampleToScreen(maxSamples) - (rect.y + rect.height));

			g2.translate(-rect.x, -(rect.y + rect.height));
		}
	}
	
	private Rectangle getRect(ScrollableData scrollableData) {
		int x = scrollableData.traceToScreen(getTraceIndex());
		int y = ProfileField.TOP_MARGIN + Chart.MARKER_OFFSET;
		return new Rectangle(x, y - ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
	}
	
	private Rectangle getRect(MapField mapField) {
		Point2D p = mapField.latLonToScreen(trace.getLatLon());
		return new Rectangle((int) p.getX(), (int) p.getY() - ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
	}

	@Override
	public boolean isPointInside(Point2D localPoint, ScrollableData profField) {
		Rectangle rect = getRect(profField);
		return rect.contains(localPoint.getX(), localPoint.getY());
	}

	@Override
	public BaseObject copy(int traceOffset) {
		TraceKey newTrace = new TraceKey(trace.getFile(), trace.getIndex() - traceOffset);
		FoundPlace copy = new FoundPlace(newTrace, model);
		// keep flag color
		copy.flagColor = flagColor;
		return copy;
	}

	@Override
	public boolean isFit(int begin, int end) {
		int traceIndex = getTraceIndex();
		return traceIndex >= begin && traceIndex <= end;
	}

	public LatLon getLatLon() {
		return trace.getLatLon();
	}
}