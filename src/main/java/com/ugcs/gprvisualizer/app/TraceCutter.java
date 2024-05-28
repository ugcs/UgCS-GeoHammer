package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceCutInitializer;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.Layer;
import com.ugcs.gprvisualizer.draw.RepaintListener;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.dzt.DztFile;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

@Component
public class TraceCutter implements Layer, SmthChangeListener, InitializingBean {

	private static final int RADIUS = 5;
	
	private MapField field;
	private List<LatLon> points;	
	private Integer active = null;

	Map<Integer, Boolean> activePoints = new HashMap<>();
	
	private final Model model;
	private final Broadcast broadcast;
	
	private RepaintListener listener;
	
	private ToggleButton buttonCutMode = new ToggleButton("Select", 
			ResourceImageHolder.getImageView("select_rect20.png"));
	private Button buttonSet = new Button("Crop", 
			ResourceImageHolder.getImageView("scisors3-20.png"));	
	private Button buttonUndo = new Button("", 
			ResourceImageHolder.getImageView("undo.png"));
	
	{
		buttonUndo.setTooltip(new Tooltip("Undo Crop")); 
	}

	public TraceCutter(Model model, Broadcast broadcast) {
		this.model = model;
		this.broadcast = broadcast;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.field = model.getField();
	}

	public void clear() {
		points = null;
		active = null;
	}

	public void init() {		
		points = new TraceCutInitializer()
			.initialRect(model, model.getFileManager().getTraces());
	}
	
	@Override
	public boolean mousePressed(Point2D point) {
		if (points == null) {
			return false;
		}
		
		List<Point2D> border = getScreenPoligon(field);
		for (int i = 0; i < border.size(); i++) {
			Point2D p = border.get(i);
			if (point.distance(p) < RADIUS) {
				active = i;
				getListener().repaint();
				return true;
			}			
		}
		active = null;
		return false;
	}
	
	@Override
	public boolean mouseRelease(Point2D point) {
		if (points == null) {
			return false;
		}
		
		if (active != null) {
			getListener().repaint();
			active = null;
		
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseMove(Point2D point) {
		if (points == null) {
			return false;
		}
		
		if (active == null) {
			return false;
		}
		
		points.get(active).from(field.screenTolatLon(point));
		if (active % 2 == 0) {
			if (!isActive(active + 1)) {
				points.get(active + 1).from(TraceCutInitializer.getMiddleOf((active + 2) < points.size() ? points.get(active + 2) : points.get(0), field.screenTolatLon(point)));
			}
			if (!isActive(active == 0 ? points.size() - 1 : active - 1)) {
				(active == 0 ? points.get(points.size() - 1) : points.get(active - 1))
				.from(TraceCutInitializer.getMiddleOf(active == 0 ? points.get(points.size() - 2) : points.get(active - 2), field.screenTolatLon(point)));
			}
		} else {
			activePoints.put(active, !isInTheMiddle(active));
		}

		getListener().repaint();		
		return true;
	}
	
	private boolean isActive(int pointIndex) {
		return activePoints.computeIfAbsent(pointIndex, i -> false);
	}

	@Override
	public void draw(Graphics2D g2, MapField fixedField) {
		if (points == null) {
			return;
		}
		
		List<Point2D> border = getScreenPoligon(field);
		
		for (int i = 0; i < border.size(); i++) {
			
			Point2D p1 = border.get(i);
			Point2D p2 = border.get((i + 1) % border.size());
			
			g2.setColor(Color.YELLOW);
			g2.drawLine((int) p1.getX(), (int) p1.getY(), 
					(int) p2.getX(), (int) p2.getY());
		}
		
		for (int i = 0; i < border.size(); i++) {
			Point2D p1 = border.get(i);			
			
			if ((i+1) % 2 == 0 && !isActive(i)) {
				g2.setColor(Color.GRAY);
			} else {
				g2.setColor(Color.WHITE);
			}
			 
			g2.fillOval((int) p1.getX() - RADIUS, 
					(int) p1.getY() - RADIUS,
					2 * RADIUS, 2 * RADIUS);
			if (active != null && active == i) {
				g2.setColor(Color.BLUE);
				g2.drawOval((int) p1.getX() - RADIUS,
						(int) p1.getY() - RADIUS,
						2 * RADIUS, 2 * RADIUS);
			}			
		}		
	}

	private boolean isInTheMiddle(int pointIndex) {
		List<Point2D> border = getScreenPoligon(field);
		return isInTheMiddle(
			border.get(pointIndex - 1), 
			border.get(pointIndex), 
			(pointIndex + 1 < border.size()) ? border.get(pointIndex + 1) : border.get(0));
	}
	
	private boolean isInTheMiddle(Point2D before, Point2D current, Point2D after) {
		double dist = before.distance(after);
		double dist1 = before.distance(current);
		double dist2 = current.distance(after);
		return Math.abs(dist1 + dist2 - dist) < 0.05;
	}

	private void apply() {
		
		model.setControls(null);
		
		MapField fld = new MapField(field);
		fld.setZoom(28);
		List<Point2D> border = getScreenPoligon(fld);
		
		List<SgyFile> slicedSgyFiles = new ArrayList<>();
		for (SgyFile file : model.getFileManager().getFiles()) {
			slicedSgyFiles.addAll(splitFile(file, fld, border));
		}
		
		model.setUndoFiles(model.getFileManager().getFiles());
		
		model.getFileManager().setFiles(slicedSgyFiles);

		for (SgyFile sf : model.getFileManager().getFiles()) {
			model.updateChart(sf.getFile(), broadcast);
		}
		
		model.getFileManager().clearTraces();
		
		model.init();
		
		model.getVField().clear();
	}

	private void undo() {
		model.setControls(null);

		if (model.getUndoFiles() != null) {
			model.getFileManager().setFiles(model.getUndoFiles());
			model.setUndoFiles(null);
			
			for (SgyFile sf : model.getFileManager().getFiles()) {
				sf.updateInternalIndexes();
			}

			model.getFileManager().clearTraces();
			
			model.init();
			
			//model.initField();
			model.getVField().clear();
		}
		
	}
	
	public boolean isTraceInsideSelection(MapField fld, List<Point2D> border, Trace trace) {
		Point2D p = fld.latLonToScreen(trace.getLatLon());
		boolean ins = inside(p, border);
		return ins;
	}

	public boolean isGeoDataInsideSelection(MapField fld, List<Point2D> border, GeoData geoData) {
		Point2D p = fld.latLonToScreen(new LatLon(geoData.getLatitude(), geoData.getLongitude()));
		boolean ins = inside(p, border);
		return ins;
	}

	private SgyFile generateSgyFileFrom(SgyFile sourceFile, List<Trace> traces, int part) {
		return generateSgyFileFrom(sourceFile, traces, new ArrayList<>(), part);
	}

	private SgyFile generateSgyFileFrom(SgyFile sourceFile, List<Trace> traces, List<GeoData> geoDataList, int part) {
		
		SgyFile sgyFile = sourceFile.copyHeader(); 
		
		sgyFile.setUnsaved(true);
		
		sgyFile.setTraces(traces);
		sgyFile.setFile(getPartFile(sourceFile, part, sourceFile.getFile().getParentFile()));

		sgyFile.setFile(sourceFile.getFile());
		sgyFile.setParser(sourceFile.getParser());
		sgyFile.getGeoData().addAll(geoDataList);
		
		int begin = traces.get(0).indexInFile;
		int end = traces.get(traces.size() - 1).indexInFile;
		sgyFile.setAuxElements(copyAuxObjects(sourceFile, sgyFile, begin, end));

		
		/// TODO:
		if (sgyFile instanceof DztFile) {
			DztFile dztfile = (DztFile) sgyFile;
			dztfile.dzg = dztfile.dzg.cut(begin, end);
		}
		
		///
		sgyFile.updateInternalIndexes();
		return sgyFile;
	}

	private File getPartFile(SgyFile file, int part, File nfolder) {
		File nfile;
		String name = file.getFile().getName();
		int pos = name.lastIndexOf(".");
		String onlyname = name.substring(0, pos);
		String spart = String.format("%03d", part);
		nfile = new File(nfolder, onlyname + "_" + spart + name.substring(pos));
		return nfile;
	}
	
	private List<SgyFile> splitFile(SgyFile file, MapField field, List<Point2D> border) {
		
		List<SgyFile> splitList = new ArrayList<>();
		List<Trace> sublist = new ArrayList<>();
		
		int part = 1;

		if(file.isCsvFile()) {
			for(Trace trace: file.getTraces()) {
				boolean inside = isTraceInsideSelection(field, border, trace);
				if (inside) {
					sublist.add(trace);
				} 
			}	
			List<GeoData> geoDataList = new ArrayList<>();
			List<GeoData> geoDataLineList = new ArrayList<>();
			int lineNumber = 0;
			for(GeoData geoData: file.getGeoData()) {
				boolean inside = isGeoDataInsideSelection(field, border, geoData);
				if (inside) {
					geoDataLineList.add(geoData);
				} else {
					if (!geoDataLineList.isEmpty()) {
						if (isGoodForFile(geoDataLineList)) { //filter too small lines
							for(GeoData gd: geoDataLineList) {
								gd.setLine(lineNumber);
							}
							lineNumber++;
							geoDataList.addAll(geoDataLineList);
						}
						geoDataLineList = new ArrayList<>();
					}
				}
	
			}
			SgyFile subfile = generateSgyFileFrom(file, sublist, geoDataList, part++);
			subfile.setFile(file.getFile());
			splitList.add(subfile);
			return splitList;
		} 
		
		for (Trace trace : file.getTraces()) {
			boolean inside = isTraceInsideSelection(field, border, trace);
			if (inside) {
				sublist.add(trace);
			} else {
				if (!sublist.isEmpty()) {
					if (isGoodForFile(sublist)) { //filter too small files
						SgyFile subfile = generateSgyFileFrom(
								file, sublist, part++);
						splitList.add(subfile);
					}
					sublist = new ArrayList<>();
				}
			}
		}

		//for last
		if (isGoodForFile(sublist)) {					
			SgyFile subfile = generateSgyFileFrom(file, sublist, part++);
			splitList.add(subfile);
		}
		
		if (splitList.size() == 1) {
			splitList.get(0).setFile(file.getFile());
		}
		
		return splitList;
	}

	public List<BaseObject> copyAuxObjects(SgyFile file, SgyFile sgyFile, int begin, int end) {
		List<BaseObject> auxObjects = new ArrayList<>();				
		for (BaseObject au : file.getAuxElements()) {
			if (au.isFit(begin, end)) {
				
				BaseObject copy = au.copy(begin, sgyFile.getOffset());
				if (copy != null) {
					auxObjects.add(copy);
				}
			}
		}
		return auxObjects;
	}

	public boolean isGoodForFile(List<?> sublist) {
		return sublist.size() > 3;
	}
	
	private List<Point2D> getScreenPoligon(MapField fld) {

		List<Point2D> border = new ArrayList<>();
		for (LatLon ll : points) {
			border.add(fld.latLonToScreen(ll));
		}
		return border;
	}

	private boolean inside(Point2D p, List<Point2D> border) {
		
		boolean result = false;
		for (int i = 0; i < border.size(); i++) {
			Point2D pt1 = border.get(i);
			Point2D pt2 = border.get((i + 1) % border.size());
		
			if ((pt1.getY() > p.getY()) != (pt2.getY() > p.getY()) 
				&& (p.getX() 
					< (pt2.getX() - pt1.getX()) 
					* (p.getY() - pt1.getY()) 
					/ (pt2.getY() - pt1.getY()) 
					+ pt1.getX())) {
		         result = !result;
		    }		
		}
		
		return result;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		
		if (changed.isFileopened()) {
			clear();
			model.setUndoFiles(null);
			initButtons();
		}
	}

	@Override
	public List<Node> getToolNodes() {
		return Arrays.asList();
	}
	
	public List<Node> getToolNodes2() {
		
		initButtons();
		
		buttonCutMode.setOnAction(e -> updateCutMode());
		
		buttonSet.setOnAction(e -> {
	    	apply();
	    	
	    	buttonCutMode.setSelected(false);
	    	updateCutMode();
	    	buttonUndo.setDisable(false);

	    	
	    	broadcast.notifyAll(new WhatChanged(Change.traceCut));
	    	
		});
		
		
		buttonUndo.setOnAction(e -> {
	    	undo();

	    	buttonCutMode.setSelected(false);
	    	updateCutMode();
	    	buttonUndo.setDisable(true);

	    	broadcast.notifyAll(new WhatChanged(Change.traceCut));
	    	
		});
		
		return Arrays.asList(buttonCutMode, buttonSet, buttonUndo);
	}

	public void initButtons() {
		buttonCutMode.setSelected(false);
		buttonSet.setDisable(true);
		buttonUndo.setDisable(model.getUndoFiles() == null);
	}
	
	private void updateCutMode() {
		if (buttonCutMode.isSelected()) {
    		init();
    		buttonSet.setDisable(false);
    	} else {
    		clear();
    		buttonSet.setDisable(true);
    	}		
		
    	getListener().repaint();
	}

	public RepaintListener getListener() {
		return listener;
	}

	public void setListener(RepaintListener listener) {
		this.listener = listener;
	}	
}

