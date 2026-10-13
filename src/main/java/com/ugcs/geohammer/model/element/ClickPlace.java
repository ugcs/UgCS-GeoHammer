package com.ugcs.geohammer.model.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;

import com.ugcs.geohammer.chart.Chart;
import com.ugcs.geohammer.chart.gpr.ProfileField;
import com.ugcs.geohammer.model.SelectedTrace;
import com.ugcs.geohammer.model.TraceKey;
import com.ugcs.geohammer.chart.gpr.GPRChart;
import com.ugcs.geohammer.chart.ScrollableData;
import com.ugcs.geohammer.util.Check;
import com.ugcs.geohammer.view.Views;
import javafx.geometry.Point2D;
import org.json.simple.JSONObject;

import com.ugcs.geohammer.format.csv.CsvFile;
import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.view.ResourceImageHolder;

public class ClickPlace extends PositionalObject {

	public static final Color AUTO_COLOR = new Color(0xFF6B6B);

	public static final Color USER_COLOR = new Color(0xC40000);

	public static final Image AUTO_IMAGE_BODY = Views.tintImage(ResourceImageHolder.IMG_GPS_BODY, AUTO_COLOR);

	public static final Image USER_IMAGE_BODY = Views.tintImage(ResourceImageHolder.IMG_GPS_BODY, USER_COLOR);

	private static final int ICON_WIDTH = ResourceImageHolder.IMG_GPS.getWidth(null);

	private static final int ICON_HALF_WIDTH = (int) Math.round(0.5 * ICON_WIDTH);

	private static final int ICON_HEIGHT = ResourceImageHolder.IMG_GPS.getHeight(null);

	private static final Stroke VERTICAL_STROKE = new BasicStroke(1.0f);

	private final SelectedTrace selectedTrace;

	public ClickPlace(SelectedTrace selectedTrace) {
		Check.notNull(selectedTrace);
		this.selectedTrace = selectedTrace;
	}

	public TraceKey getTrace() {
		return selectedTrace.trace();
	}

	@Override
	public int getTraceIndex() {
		return selectedTrace.trace().getIndex();
	}

	@Override
	public void offset(int traceOffset) {
		selectedTrace.trace().offset(traceOffset);
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField mapField) {
		return false;
	}

	@Override
	public BaseObject copy(int traceOffset) {
		TraceKey traceKey = selectedTrace.trace();
		TraceKey newTraceKey = new TraceKey(traceKey.getFile(), traceKey.getIndex() - traceOffset);
		SelectedTrace newSelectedTrace = new SelectedTrace(newTraceKey, selectedTrace.selectionType());
		return new ClickPlace(newSelectedTrace);
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField mapField) {

		Rectangle rect = getRect(mapField);

		g2.translate(rect.x, rect.y);
		g2.drawImage(getImageBodyBySelectionType(), 0, 0, null);
		g2.drawImage(ResourceImageHolder.IMG_GPS, 0, 0, null);
		g2.translate(-rect.x, -rect.y);
	}

	@Override
	public void drawOnCut(Graphics2D g2, ScrollableData scrollableData) {
		if (selectedTrace.trace().getFile() instanceof CsvFile) {
			return;
		}

		if (scrollableData instanceof GPRChart gprChart) {
			var profField = gprChart.getField();
			setClip(g2, profField.getClipTopMainRect());

			Rectangle rect = getRect(gprChart);

			g2.translate(rect.x, rect.y);
			g2.drawImage(getImageBodyBySelectionType(), 0, 0, null);
			g2.drawImage(ResourceImageHolder.IMG_GPS, 0, 0, null);

			Color markerColor = switch (selectedTrace.selectionType()) {
				case USER -> USER_COLOR;
				case AUTO -> AUTO_COLOR;
			};
			g2.setStroke(VERTICAL_STROKE);
			g2.setColor(markerColor);

			int maxSamples = profField.getMaxHeightInSamples();
			g2.drawLine(ICON_HALF_WIDTH, rect.height, ICON_HALF_WIDTH, gprChart.sampleToScreen(maxSamples) - rect.y);
			g2.translate(-rect.x, -rect.y);
		}
	}

	private Image getImageBodyBySelectionType() {
		return switch (selectedTrace.selectionType()) {
			case USER -> USER_IMAGE_BODY;
			case AUTO -> AUTO_IMAGE_BODY;
		};
	}

	private Rectangle getRect(GPRChart gprChart) {
		int x = gprChart.traceToScreen(selectedTrace.trace().getIndex());
		int y = ProfileField.TOP_MARGIN + Chart.MARKER_OFFSET;
		return new Rectangle(x - ICON_HALF_WIDTH, y - ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
	}
	
	private Rectangle getRect(MapField mapField) {
		Point2D p =  mapField.latLonToScreen(selectedTrace.trace().getLatLon());
		return new Rectangle((int) p.getX() - ICON_HALF_WIDTH, (int) p.getY() - ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
	}

	@Override
	public boolean saveTo(JSONObject json) {
		return false;
	}
}
