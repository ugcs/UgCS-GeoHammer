package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;

import com.github.thecoldwine.sigrun.common.ext.*;
import com.ugcs.gprvisualizer.app.*;
import edu.mines.jtk.interp.SplinesGridder2;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.CoordinatesMath;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

@Component
public class GridLayer extends BaseLayer implements InitializingBean {

	@Autowired
	private Model model;
	
	@Autowired
	private Dimension wndSize;

	private EventHandler<ActionEvent> showMapListener = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			System.out.println("showMapListener: " + event);
			setActive(optionPane.getGridding().isSelected());
			getRepaintListener().repaint();				
		}
	};

	@Autowired
	private OptionPane optionPane;

	ThrQueue q;

	private CsvFile file;
	private double cellSize;
	private double blankingDistance;
	private boolean recalcGrid;
	private boolean toAll;

	@Override
	public void afterPropertiesSet() throws Exception {
		setActive(optionPane.getGridding().isSelected());

		q = new ThrQueue(model) {
			protected void draw(BufferedImage backImg, MapField field) {
				optionPane.griddingProgress(true);

				Graphics2D g2 = (Graphics2D) backImg.getGraphics();
				g2.translate(backImg.getWidth() / 2, backImg.getHeight() / 2);
				drawOnMapField(g2, field);
			}
			
			public void ready() {
				optionPane.griddingProgress(false);
				getRepaintListener().repaint();
			}
		};

		q.setWindowSize(wndSize);

		optionPane.getGridding().addEventHandler(ActionEvent.ACTION, showMapListener);
	}
		
	@Override
	public void draw(Graphics2D g2, MapField currentField) {
		if (currentField.getSceneCenter() == null || !isActive()) {
			return;
		}
		q.drawImgOnChangedField(g2, currentField, q.getFront());
	}


	public void drawOnMapField(Graphics2D g2, MapField field) {
		if (isActive()) {
			if (recalcGrid) {
				setActive(false);
				getRepaintListener().repaint();
			}

			if (file != null && cellSize != 0 && blankingDistance != 0) {
				drawFileOnMapField(g2, field, file);
			}
			setActive(optionPane.getGridding().isSelected());
		}
	}

	record DataPoint(double latitude, double longitude, double value) implements Comparable<DataPoint> {

		DataPoint {
			if (latitude < -90 || latitude > 90) {
				throw new IllegalArgumentException("Latitude must be in range [-90, 90]");
			}
			if (longitude < -180 || longitude > 180) {
				throw new IllegalArgumentException("Longitude must be in range [-180, 180]");
			}
		}

		@Override
		public int compareTo(@NotNull DataPoint o) {
			return latitude == o.latitude ? Double.compare(longitude, o.longitude) : Double.compare(latitude, o.latitude);
		}
	}

	private static double calculateMedian(List<Double> values) {
		Collections.sort(values);
		int size = values.size();
		if (size % 2 == 0) {
			return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
		} else {
			return values.get(size / 2);
		}
	}

	private static Color getColorForValue(double value, double min, double max) {
		value = Math.clamp(value, min, max);
		double normalized = (value - min) / (max - min);

		javafx.scene.paint.Color color = javafx.scene.paint.Color.hsb(normalized * 280, 0.8f, 0.8f);
		return new Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
	}

	private List<DataPoint> dataPoints;
	private LatLon minLatLon;
	private LatLon maxLatLon;
	private float[][] gridData;
	private float minValue;
	private float maxValue;

	private void drawFileOnMapField(Graphics2D g2, MapField field, CsvFile csvFile) {

		if (recalcGrid) {
			var chart = model.getChart(csvFile);
			String sensor = chart.get().getSelectedSeriesName();

			//if (chart.isPresent()) {
			//	sensor = chart.get().getSelectedSeriesName();
			//} else {
			//	sensor = GeoData.Semantic.TMI.getName();
			//}

			if (toAll) {
				dataPoints = new ArrayList<>();
				for(CsvFile csvFile1: model.getFileManager().getCsvFiles().stream().map(f -> (CsvFile)f).filter(f -> f.isSameTemplate(csvFile)).toList()) {
					dataPoints.addAll(getDataPoints(csvFile1, sensor));
				}
			} else {
				dataPoints = getDataPoints(csvFile, sensor);
			}
			dataPoints = getMedianValues(dataPoints);

			double minLon = dataPoints.stream().mapToDouble(DataPoint::longitude).min().orElseThrow();
			double maxLon = dataPoints.stream().mapToDouble(DataPoint::longitude).max().orElseThrow();
			double minLat = dataPoints.stream().mapToDouble(DataPoint::latitude).min().orElseThrow();
			double maxLat = dataPoints.stream().mapToDouble(DataPoint::latitude).max().orElseThrow();

			minLatLon = new LatLon(minLat, minLon);
			maxLatLon = new LatLon(maxLat, maxLon);

			int gridSizeX = (int) Math.max(new LatLon(minLat, minLon).getDistance(new LatLon(minLat, maxLon)),
					new LatLon(maxLat, minLon).getDistance(new LatLon(maxLat, maxLon)));

			gridSizeX = (int) (gridSizeX / cellSize);

			int gridSizeY = (int) Math.max(new LatLon(minLat, minLon).getDistance(new LatLon(maxLat, minLon)),
					new LatLon(minLat, maxLon).getDistance(new LatLon(maxLat, maxLon)));

			gridSizeY = (int) (gridSizeY / cellSize);

			double lonStep = (maxLon - minLon) / gridSizeX;
			double latStep = (maxLat - minLat) / gridSizeY;

			gridData = new float[gridSizeX][gridSizeY];

			//var average = dataPoints.stream().mapToDouble(p -> p.value).average().getAsDouble();
			var average = 0.0f;

			for (DataPoint point : dataPoints) {
				int xIndex = (int) ((point.longitude - minLon) / lonStep);
				int yIndex = (int) ((point.latitude - minLat) / latStep);
				try {
					gridData[xIndex][yIndex] = (float) point.value;
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("xIndex = " + xIndex + " yIndex = " + yIndex);
				}

				//for (int i = 0; i < gridData.length; i++) {
				//	for (int j = 0; j < gridData[i].length; j++) {
				//		if (gridData[i][j] == 0.0f) {
				//			gridData[i][j] = (float) average;
				//		}
				//	}
				//}
			}

			var gridder = new SplinesGridder2();
			gridder.setTension(0.999f);
			gridder.gridMissing((float) average, gridData);

			//var minValue = ArrayMath.min(gridData);
			//var maxValue = ArrayMath.max(gridData);
			//System.out.println("minValue = " + minValue + " maxValue = " + maxValue);

			KdTree kdTree = buildKdTree(dataPoints);

			double lonStepBD = (maxLatLon.getLonDgr() - minLatLon.getLonDgr()) / (gridSizeX * cellSize / blankingDistance);
			double latStepBD = (maxLatLon.getLatDgr() - minLatLon.getLatDgr()) / (gridSizeY * cellSize / blankingDistance);

			Set<Float> values = new HashSet<>();
			for (int i = 0; i < gridData.length; i++) {
				for (int j = 0; j < gridData[0].length; j++) {

					double lat = minLatLon.getLatDgr() + j * latStep;
					double lon = minLatLon.getLonDgr() + i * lonStep;

					//TODO: convert to use nearest neighbors
					//if (!isInBlankingDistanceToKnownPoints(lat, lon, dataPoints, blankingDistance)) {
					//	gridData[i][j] = Float.NaN;
					//	continue;
					//}

					var delta = 1;
					List<KdNode> neighbors = kdTree.query(new Envelope(lon - delta*lonStepBD, lon + delta*lonStepBD, lat - delta * latStepBD, lat + delta * latStepBD)); // maxNeighbors);
					if (neighbors.isEmpty()) {
						gridData[i][j] = Float.NaN;
						continue;
					} else {
						values.add(gridData[i][j]);
					}

				}
			}

			System.out.println("values.size() = " + values.size());

			double min = values.stream().filter(v -> !Float.isNaN(v)).mapToDouble(p -> p).min().orElse(Double.MIN_VALUE);
			//minValue = (float) values.stream().filter(v -> !Float.isNaN(v)).mapToDouble(p -> p).sorted().skip((long)(values.size() * 0.05)).min().orElse(Double.MIN_VALUE);
			//System.out.println("min = " + min + ", lowValue = " + minValue);
			this.minValue = (float) min;

			double max = values.stream().mapToDouble(p -> p).max().orElse(Double.MAX_VALUE);
			//maxValue = (float) values.stream().mapToDouble(p -> p).sorted().limit((long)(values.size() * 0.95)).max().orElse(Double.MAX_VALUE);
			//System.out.println("max = " + max + ", highValue = " + maxValue);
			this.maxValue = (float) max;

			Platform.runLater(() -> optionPane.setGriddingMinMax(min, max, minValue, maxValue));

			recalcGrid = false;
		}

		int gridSizeX = gridData.length;
		int gridSizeY = gridData[0].length;

		double lonStep = (maxLatLon.getLonDgr() - minLatLon.getLonDgr()) / gridSizeX;
		double latStep = (maxLatLon.getLatDgr() - minLatLon.getLatDgr()) / gridSizeY;

		var minLatLonPoint = field.latLonToScreen(minLatLon);
		var nextLatLonPoint = field.latLonToScreen(new LatLon(minLatLon.getLatDgr() + latStep, minLatLon.getLonDgr() + lonStep));

		double cellWidth = Math.abs(minLatLonPoint.getX() - nextLatLonPoint.getX()) + 1; //3; //width / gridSizeX;
		double cellHeight = Math.abs(minLatLonPoint.getY() - nextLatLonPoint.getY()) + 1; //3; //height / gridSizeY;

		System.out.println("cellWidth = " + cellWidth + " cellHeight = " + cellHeight);

		var minValue = optionPane.getGriddingRangeSlider().isDisabled() ? this.minValue : (float) optionPane.getGriddingRangeSlider().getLowValue();
		var maxValue = optionPane.getGriddingRangeSlider().isDisabled() ? this.maxValue : (float) optionPane.getGriddingRangeSlider().getHighValue();

			for (int i = 0; i < gridData.length; i++) {
				for (int j = 0; j < gridData[0].length; j++) {
					try {
						var value = gridData[i][j];
						if (value <= 0 || Double.isNaN(value) || Float.isNaN(value)) {
							continue;
						}

						Color color = getColorForValue(value, minValue, maxValue);
						g2.setColor(color);

						double lat = minLatLon.getLatDgr() + j * latStep;
						double lon = minLatLon.getLonDgr() + i * lonStep;

						var point = field.latLonToScreen(new LatLon(lat, lon));
						g2.fillRect((int) point.getX(), (int) point.getY(), (int) cellWidth, (int) cellHeight);
					} catch (Exception e) {
						//System.out.println(e.getMessage());
						//System.err.println(e);
					}
				}
			}
	}

	private List<DataPoint> getDataPoints(CsvFile csvFile, String sensor) {
		return csvFile.getGeoData().stream().filter(gd -> gd.getSensorValue(sensor).data() != null)
				.map(gd -> new DataPoint(gd.getLatitude(), gd.getLongitude(), gd.getSensorValue(sensor).data().doubleValue())).toList();
	}

	private static KdTree buildKdTree(List<DataPoint> dataPoints) {
		KdTree kdTree = new KdTree(0.0000);
		for (DataPoint point : dataPoints) {
			Coordinate coord = new Coordinate(point.longitude, point.latitude);
			kdTree.insert(coord, point);
		}
		return kdTree;
	}

	private static boolean isInBlankingDistanceToKnownPoints(double lat, double lon, List<DataPoint> knownPoints, double blankingDistance) {
        for (DataPoint point : knownPoints) {
            double distance = CoordinatesMath.measure(lat, lon, point.latitude, point.longitude);
            if (distance <= blankingDistance) {
                return true;
            }
        }
        return false;
    }    

	private static List<DataPoint> getMedianValues(List<DataPoint> dataPoints) {
		Map<String, List<Double>> dataMap = new HashMap<>();
		for (DataPoint point : dataPoints) {
			String key = point.latitude + "," + point.longitude;
			dataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(point.value);
		}

		List<DataPoint> medianDataPoints = new ArrayList<>();
		for (Map.Entry<String, List<Double>> entry : dataMap.entrySet()) {
			String[] coords = entry.getKey().split(",");
			double latitude = Double.parseDouble(coords[0]);
			double longitude = Double.parseDouble(coords[1]);
			//double averageValue = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
			double medianValue = calculateMedian(entry.getValue());
			//System.out.println("latitude = " + latitude + " longitude = " + longitude + " averageValue = " + averageValue + " calculateValue = " + calculateValue);
			medianDataPoints.add(new DataPoint(latitude, longitude, medianValue));
		}

		return medianDataPoints;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if (
				changed.isTraceCut()
		//		|| changed.isTraceValues()
		//		|| changed.isFileopened()
				|| changed.isZoom() 
				|| changed.isAdjusting() 
				|| changed.isMapscroll() 
				|| changed.isWindowresized()
				|| changed.isJustdraw()) {
			q.add();
		} else if (changed.isTimeLagFixed()) {
			recalcGrid = true;
			q.add();
		} else if (changed.isFileSelected()) {
			var file = ((FileSelected) changed).getSelectedFile();
			if (file instanceof CsvFile) {
				this.file = (CsvFile) file;
			}
			recalcGrid = true;
			q.add();
		} else if (changed.isGriddingParams()) {
			cellSize = ((GriddingParamsSetted) changed).getCellSize();
			blankingDistance = ((GriddingParamsSetted) changed).getBlankingDistance();
			toAll = ((GriddingParamsSetted) changed).isToAll();
			recalcGrid = true;
			q.add();
		}
	}

	@Override
	public boolean mousePressed(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseRelease(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMove(Point2D point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Node> getToolNodes() {
		return List.of();
		//return Arrays.asList(showLayerCheckbox);
	}
}
