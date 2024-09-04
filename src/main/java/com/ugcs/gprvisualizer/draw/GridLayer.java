package com.ugcs.gprvisualizer.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;

import com.github.thecoldwine.sigrun.common.ext.*;
import com.ugcs.gprvisualizer.app.FileSelected;
import com.ugcs.gprvisualizer.app.OptionPane;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import edu.mines.jtk.interp.SplinesGridder2;
import edu.mines.jtk.util.ArrayMath;
import org.jetbrains.annotations.NotNull;
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

	@Override
	public void afterPropertiesSet() throws Exception {
		setActive(optionPane.getGridding().isSelected());

		q = new ThrQueue(model) {
			protected void draw(BufferedImage backImg, MapField field) {
				
				Graphics2D g2 = (Graphics2D) backImg.getGraphics();
				g2.translate(backImg.getWidth() / 2, backImg.getHeight() / 2);
				drawOnMapField(g2, field);
			}
			
			public void ready() {
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
		if (file != null && cellSize != 0 && blankingDistance != 0) {
			drawFileOnMapField(g2, field, file);
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

			dataPoints = csvFile.getGeoData().stream().filter(gd -> gd.getSensorValue(sensor).data() != null)
					.map(gd -> new DataPoint(gd.getLatitude(), gd.getLongitude(), gd.getSensorValue(sensor).data().doubleValue())).toList();
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

			var average = dataPoints.stream().mapToDouble(p -> p.value).average().getAsDouble();

			average = 0.0f;

			for (DataPoint point : dataPoints) {
				int xIndex = (int) ((point.longitude - minLon) / lonStep);
				int yIndex = (int) ((point.latitude - minLat) / latStep);
				try {
					gridData[xIndex][yIndex] = (float) point.value;
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("xIndex = " + xIndex + " yIndex = " + yIndex);
				}

				for (int i = 0; i < gridData.length; i++) {
					for (int j = 0; j < gridData[i].length; j++) {
						if (gridData[i][j] == 0.0f) {
							gridData[i][j] = (float) average;
						}
					}
				}
			}

			var gridder = new SplinesGridder2();
			gridder.setTension(0.999f);
			gridder.gridMissing((float) average, gridData);

			//var minValue = ArrayMath.min(gridData);
			//var maxValue = ArrayMath.max(gridData);
			//System.out.println("minValue = " + minValue + " maxValue = " + maxValue);

			Set<Float> values = new HashSet<>();
			for (int i = 0; i < gridData.length; i++) {
				for (int j = 0; j < gridData[0].length; j++) {
					values.add(gridData[i][j]);
				}
			}

			System.out.println("values.size() = " + values.size());

			minValue = (float) values.stream().filter(v -> !Float.isNaN(v)).mapToDouble(p -> p).sorted().skip((long)(values.size() * 0.1)).min().orElse(Double.MIN_VALUE);
			System.out.println("minValue = " + minValue);

			maxValue = (float) values.stream().mapToDouble(p -> p).sorted().limit((long)(values.size() * 0.9)).max().orElse(Double.MAX_VALUE);
			System.out.println("maxValue = " + maxValue);

			optionPane.setGriddingMinMax(minValue, maxValue);

			recalcGrid = false;
		}

		double cellWidth = 3; //width / gridSizeX;
		double cellHeight = 3; //height / gridSizeY;

		int gridSizeX = gridData.length;
		int gridSizeY = gridData[0].length;

		double lonStep = (maxLatLon.getLonDgr() - minLatLon.getLonDgr()) / gridSizeX;
		double latStep = (maxLatLon.getLatDgr() - minLatLon.getLatDgr()) / gridSizeY;

		var minValue = this.minValue;
		minValue = optionPane.getMinValue().getText().isEmpty() ? minValue : Float.parseFloat(optionPane.getMinValue().getText());
		var maxValue = this.maxValue;
		maxValue = optionPane.getMaxValue().getText().isEmpty() ? maxValue : Float.parseFloat(optionPane.getMaxValue().getText());

			for (int i = 0; i < gridData.length; i++) {
				for (int j = 0; j < gridData[0].length; j++) {
					try {
						var value = gridData[i][j];
						if (value <= 0 || Double.isNaN(value) || Float.isNaN(value)) {
							continue;
						}

						double lat = minLatLon.getLatDgr() + j * latStep;
						double lon = minLatLon.getLonDgr() + i * lonStep;

                		//TODO: convert to use nearest neighbors
                		if (!isInBlankingDistanceToKnownPoints(lat, lon, dataPoints, blankingDistance)) {
							gridData[i][j] = Float.NaN;
                    		continue;
                		}

						Color color = getColorForValue(value, minValue, maxValue);
						g2.setColor(color);

						var point = field.latLonToScreen(new LatLon(lat, lon));
						g2.fillRect((int) point.getX(), (int) point.getY(), (int) cellWidth, (int) cellHeight);
					} catch (Exception e) {
						//System.out.println(e.getMessage());
						//System.err.println(e);
					}
				}
			}
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
