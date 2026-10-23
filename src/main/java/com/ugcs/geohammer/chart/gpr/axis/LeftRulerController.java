package com.ugcs.geohammer.chart.gpr.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.ugcs.geohammer.AppContext;
import com.ugcs.geohammer.chart.gpr.GPRChart;
import com.ugcs.geohammer.chart.gpr.ProfileField;
import com.ugcs.geohammer.format.TraceFile;
import com.ugcs.geohammer.chart.ScrollableData;
import com.ugcs.geohammer.view.Colors;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.tuple.Pair;

import com.ugcs.geohammer.model.element.BaseObject;
import com.ugcs.geohammer.model.element.BaseObjectImpl;

public class LeftRulerController {

	// toggle box along the text (vertical on screen) and across it
	private static final int TOGGLE_WIDTH = 55;

	private static final int TOGGLE_HEIGHT = 20;

	// from the right edge of the ruler to the toggle center
	private static final int TOGGLE_RIGHT_OFFSET = 20;

	public static Stroke STROKE = new BasicStroke(1.0f);

	private final Converter[] converters;

	private int index = 0;

	private final BaseObject toggle = new BaseObjectImpl() {

		@Override
		public void drawOnCut(Graphics2D g2, ScrollableData scrollableData) {
			if (!(scrollableData instanceof GPRChart gprChart)) {
				return;
			}

			Color strokeColor = Colors.awtColor(AppContext.getTheme().strokeColor());
			g2.setClip(null);

			Rectangle r = getRect(gprChart.getField());
			AffineTransform transform = g2.getTransform();
			g2.translate(r.getCenterX(), r.getCenterY());
			g2.rotate(-0.5 * Math.PI); // 90 degrees CCW, text reads bottom to top

			g2.setStroke(STROKE);
			g2.setColor(Colors.opaque(strokeColor, 0.25f));
			g2.drawRoundRect(-TOGGLE_WIDTH / 2, -TOGGLE_HEIGHT / 2, TOGGLE_WIDTH, TOGGLE_HEIGHT, 7, 7);

			g2.setColor(strokeColor);
			String text = getConverter().getUnit();
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(text, -fm.stringWidth(text) / 2, (fm.getAscent() - fm.getDescent()) / 2);

			g2.setTransform(transform);
		}

		private Rectangle getRect(ProfileField profField) {
			Rectangle ruleRect = profField.getLeftRuleRect();
			Rectangle mainRect = profField.getMainRect();
			int centerX = profField.getVisibleStart() + ruleRect.x + ruleRect.width - TOGGLE_RIGHT_OFFSET;
			int centerY = mainRect.y + mainRect.height / 2;
			return new Rectangle(
					centerX - TOGGLE_HEIGHT / 2, centerY - TOGGLE_WIDTH / 2,
					TOGGLE_HEIGHT, TOGGLE_WIDTH);
		}

		@Override
		public boolean isPointInside(Point2D localPoint, ScrollableData scrollableData) {
			if (scrollableData instanceof GPRChart gprChart) {
				Rectangle rect = getRect(gprChart.getField());
				return rect.contains(localPoint.getX(), localPoint.getY());
			}
			return false;
		}

		@Override
		public boolean mousePressHandle(Point2D localPoint, ScrollableData scrollableData) {
			if (isPointInside(localPoint, scrollableData)) {
				nextConverter();
				return true;
			}
			return false;
		}
	};

	public LeftRulerController(ProfileField profileField) {
		converters = new Converter[] {
                new SampleConverter(), new TimeConverter(profileField.getFile())
        };
	}
	
	public Converter getConverter() {
		return converters[index];
	}
	
	public void nextConverter() {
		index = (index + 1) % converters.length;
	}

	public BaseObject getToggle() {
		return toggle;
	}

	public interface Converter {

		Pair<Integer, Integer> convert(int s, int f);

		int back(int unt);

		String getUnit();
	}

	static class SampleConverter implements Converter {

		@Override
		public Pair<Integer, Integer> convert(int s, int f) {
			return Pair.of(s, f);
		}

		public int back(int unt) {
			return unt;
		}

		@Override
		public String getUnit() {
			return "samples";
		}
	}

	static class TimeConverter implements Converter {

		// tolerance for rounding sample times to whole ns
		private static final double EPS = 1e-9;

		private final TraceFile sgyFile;

		public TimeConverter(TraceFile sgyFile) {
			this.sgyFile = sgyFile;
		}

		@Override
		public Pair<Integer, Integer> convert(int s, int f) {
			double sampleInterval = sgyFile.getSampleIntervalNanos();

			return Pair.of(
					(int)Math.ceil(sampleInterval * s - EPS),
					(int)Math.floor(sampleInterval * f + EPS));
		}

		@Override
		public int back(int unt) {
			double sampleInterval = sgyFile.getSampleIntervalNanos();
			if (sampleInterval <= 0) {
				return 0;
			}
			return (int)Math.round(unt / sampleInterval);
		}

		@Override
		public String getUnit() {
			return "ns";
		}
	}
}
