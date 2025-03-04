package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

import com.ugcs.gprvisualizer.app.GPRChart;
import com.ugcs.gprvisualizer.app.ScrollableData;
import javafx.geometry.Point2D;
import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.gpr.Model;

public class ClickPlace extends BaseObjectImpl { //, MouseHandler {

	private final static int R_HOR = ResourceImageHolder.IMG_GPS.getWidth(null) / 2;
	private final static int R_VER = ResourceImageHolder.IMG_GPS.getHeight(null) / 2;

	public static Stroke SELECTED_STROKE = new BasicStroke(2.0f);
	
	private static final float[] dash1 = {7.0f, 2.0f};
	static Stroke VERTICAL_STROKE = 	
			new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, dash1, 0.0f);

	private Color flagColor = Color.getHSBColor((float) Math.random(), 1, 1f); 
	
	private final Trace trace;

	private final SgyFile file;

	public ClickPlace(SgyFile file, Trace trace) {
		this.file = file;
		this.trace = trace;
	}

	public Trace getTrace() {
		return trace;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField mapField) {
		return false;
	}

	@Override
	public BaseObject copy(int traceoffset, VerticalCutPart verticalCutPart) {
		ClickPlace result = new ClickPlace(file, trace); 	
		return result;
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField mapField) {
		
		Rectangle rect = getRect(mapField);
		
		g2.setColor(flagColor);
		
		g2.translate(rect.x, rect.y);
		
		g2.drawImage(ResourceImageHolder.IMG_GPS, 0, 0, null);
		g2.translate(-rect.x, -rect.y);
	}

	@Override
	public void drawOnCut(Graphics2D g2, ScrollableData scrollableData) {
		if (file instanceof CsvFile) {
			return;
		}

		if (scrollableData instanceof GPRChart gprChart) {
			var profField = gprChart.getField();
			setClip(g2, profField.getClipTopMainRect());

			Rectangle rect = getRect(gprChart);

			g2.setColor(flagColor);
			g2.translate(rect.x, rect.y);
			g2.drawImage(ResourceImageHolder.IMG_GPS, 0, 0, null);

			g2.setStroke(VERTICAL_STROKE);
			g2.setColor(Color.blue);
			g2.setXORMode(Color.gray);
			g2.drawLine(R_HOR, R_VER * 2 , R_HOR,
			gprChart.sampleToScreen(gprChart.getField().getMaxHeightInSamples()) - Model.TOP_MARGIN + R_VER * 2);
			g2.setPaintMode();
			g2.translate(-rect.x, -rect.y);
		}
	}
	
	private Rectangle getRect(GPRChart gprChart) {
		
		int x = gprChart.traceToScreen(trace.getIndexInSet());
				
		Rectangle rect = new Rectangle(
				x - R_HOR, Model.TOP_MARGIN - R_VER * 2,
				R_HOR * 2, R_VER * 2);
		return rect;
	}
	
	private Rectangle getRect(MapField mapField) {
		//Trace tr = getTrace();		
		Point2D p =  mapField.latLonToScreen(trace.getLatLon());		
		
		Rectangle rect = new Rectangle(
				(int) p.getX() - R_HOR, (int) p.getY() - R_VER * 2,
				R_HOR * 2, R_VER * 2);
		return rect;
	}

	//private Trace getTrace() {
	//	if (file != null) {
	//		return file.getTraces().get(traceInAll);
	//	}
	//	return AppContext.model.getGprTraces().get(traceInAll);
	//}



	@Override
	public List<BaseObject> getControls() {
		return null;
	}

	@Override
	public boolean saveTo(JSONObject json) {
		return false;
	}

	/*public int getGlobalTrace() {
		return traceInAll;
	}*/

}
