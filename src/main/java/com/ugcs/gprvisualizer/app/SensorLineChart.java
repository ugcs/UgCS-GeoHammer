package com.ugcs.gprvisualizer.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.ImageView;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.app.parcers.SensorValue;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;


public class SensorLineChart {

    private static final Logger log = LoggerFactory.getLogger(SensorLineChart.class);

    private final Model model;
    private final Broadcast broadcast;

    private Map<SeriesData, BooleanProperty> itemBooleanMap = new HashMap<>();
    private LineChartWithMarkers lastLineChart = null;
    private Set<LineChartWithMarkers> charts = new HashSet<>();
    private Rectangle zoomRect = new Rectangle();

    //private int scale;
    private CsvFile file;

    public SensorLineChart(Model model, Broadcast broadcast) {
        this.model = model;
        this.broadcast = broadcast;
    }

    public void setSelectedTrace(int traceNumber) {

        int selectedX = traceNumber; /// scale;
        NumberAxis xAxis = (NumberAxis) lastLineChart.getXAxis();
        var dataSize = lastLineChart.plotData.data().size();
        
        if (selectedX < 0 || selectedX > dataSize) {
            log.error("Selected trace number: {} is out of range: {}", traceNumber, dataSize);
            return;
        }

        log.debug("Selected trace number: {}", traceNumber);

        if (xAxis.getLowerBound() > selectedX || xAxis.getUpperBound() < selectedX) {

            int delta = (int)(xAxis.getUpperBound() - xAxis.getLowerBound());

            int lowerIndex = Math.clamp(selectedX - delta / 2, 0, dataSize - delta);
            int upperIndex = Math.clamp(selectedX + delta / 2, delta, dataSize);

            log.debug("Shifted charts, lowerIndex: {} upperIndex: {} size: {}", lowerIndex, upperIndex, dataSize);

            for (LineChartWithMarkers chart: charts) {
                xAxis = (NumberAxis) chart.getXAxis();
                xAxis.setLowerBound(lowerIndex);
                xAxis.setUpperBound(upperIndex);

                chart.getData().forEach(series -> {
                    //series.getData().addAll(getSubsample(calculateAverages(chart.plotData.data().subList(lowerIndex, upperIndex)), lowerIndex, upperIndex));
                    series.getData().addAll(getSubsampleInRange(chart.plotData.data()//.subList(lowerIndex, upperIndex)
                    , lowerIndex, upperIndex));
                });
            }        
        }
        model.selectAndScrollToChart(root);
        putVerticalMarker(selectedX);
    }


    private EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	
        	if (event.getClickCount() == 2) {

                log.debug("Double click " + event.getX() + " " + event.getY() + " " + event.getSource());
                model.getChart(null); // clear selection
                                
                if (event.getSource() instanceof LineChart) {
                    Axis<Number> xAxis = ((LineChart) event.getSource()).getXAxis();
                    Number xValue = xAxis.getValueForDisplay(event.getX() - xAxis.getLayoutX());
                    System.out.println("xValue: " + xValue.intValue()); //* scale);

                    putVerticalMarker(xValue.intValue());

                    int traceIndex = xValue.intValue(); //* scale;
                    if (traceIndex >= 0 && traceIndex < file.getTraces().size()) {//model.getCsvTracesCount()) {
                        GeoData geoData = file.getGeoData().get(traceIndex);
                        model.getMapField().setSceneCenter(new LatLon(geoData.getLatitude(), geoData.getLongitude()));
                        model.createClickPlace(file, file.getTraces().get(traceIndex));
                        broadcast.notifyAll(new WhatChanged(Change.mapscroll));
                    }
                }
                event.consume();
        	}
        }
	};

    public List<PlotData> generatePlotData(CsvFile csvFile) {
    
        List<GeoData> geoData = csvFile.getGeoData();

        Map<String, List<SensorValue>> sensorValues = new LinkedHashMap<>();
        geoData.forEach(data -> {
            data.getSensorValues().forEach(value -> {
                sensorValues.compute(value.semantic() + "--" + (StringUtils.hasText(value.units()) ? value.units() : " "), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(value);
                    return v;
                });
            });
        });
        
        List<PlotData> plotDataList = new ArrayList<>();
        for (Map.Entry<String, List<SensorValue>> e: sensorValues.entrySet()) {
            var pd = e.getKey().split("--");
            var data = e.getValue().stream()
                    .map(SensorValue::data)
                    .collect(Collectors.toList());
            PlotData plotData = new PlotData(pd[0], pd[1], getColor(pd[0]), data);
            
            //calculateAverages(e.getValue().stream()
            //        .map(SensorValue::data)
            //        .collect(Collectors.toList())));
            plotDataList.add(plotData);
        }
        return plotDataList;
    }

    private Color getColor(String semantic) {
        return model.getColorBySemantic(semantic);
    }

    /* private List<Number> calculateAverages(List<Number> sourceList) {
        if (sourceList.isEmpty()) return sourceList;
        //check if sourceList have only nulls
        if (sourceList.stream().allMatch(Objects::isNull)) {
            return new ArrayList<>();
        }

        var scale = sourceList.size() / Math.clamp(sourceList.size(), 1, 2000);
        return IntStream.range(0, sourceList.size() / scale) // Create an index stream from 0 to 999
                .mapToObj(i -> sourceList.subList(i * scale, (i + 1) * scale) // Transform each index into a sublist of 100 elements
                    .stream()    
                    .filter(Objects::nonNull)
                    .mapToDouble(Number::doubleValue)
                    .average())
                .map(optAvg -> optAvg.isPresent() ? optAvg.getAsDouble() : null) // Calculate average value
                .collect(Collectors.toList()); // Collect results in a list
    } */

    public record PlotData(String semantic, String units, Color color, List<Number> data) {}
    record SeriesData(XYChart.Series<Number, Number> series, Color color) {
        @Override
        public final String toString() {
            return series.getName();
        }
    }

    public BooleanProperty getItemBooleanProperty(SeriesData item) {
        return itemBooleanMap.get(item);
    }

    public VBox createChartWithMultipleYAxes(CsvFile file, List<PlotData> plotDataList) {

        this.file = file;        

        // Using StackPane to overlay charts
        StackPane stackPane = new StackPane();
        ObservableList<SeriesData> seriesList = FXCollections.observableArrayList();

        for (int i = 0; i < plotDataList.size(); i++) {

            var plotData = plotDataList.get(i);
            
            // X-axis, common for all charts
            NumberAxis xAxis = getXAxis(plotData.data().size() / 10);

            // Y-axis
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(plotData.units());
            yAxis.setSide(javafx.geometry.Side.RIGHT); // Y-axis on the right
            yAxis.setMinorTickVisible(false);
            //yAxis.setAutoRanging(false);
            var data = plotData.data();//calculateAverages(plotData.data());

            var min = getFloorMin(data);
            var max = getCeilMax(data);
            //yAxis.setLowerBound(min);
            //yAxis.setUpperBound(max);

            yAxis.setTickUnit((max - min) / 10);
            yAxis.setPrefWidth(70);
            if (i > 0) {
                yAxis.setOpacity(0);
            }

            // Creating chart
            LineChartWithMarkers lineChart = new LineChartWithMarkers(xAxis, yAxis, 0, plotData.data().size(), min, max, plotData);
            lineChart.zoomOut();

            lineChart.setLegendVisible(false); // Hide legend
            lineChart.setCreateSymbols(false); // Disable symbols
            if (i > 0) {
                lineChart.setVerticalGridLinesVisible(false);
                lineChart.setHorizontalGridLinesVisible(false);
                lineChart.setHorizontalZeroLineVisible(false);
                lineChart.setVerticalZeroLineVisible(false);
                lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
            }

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(plotData.semantic());
            // Add data to chart
            if (!data.isEmpty()) {
                series.getData().setAll(getSubsampleInRange(data, 0, plotData.data().size()));
            }

            SeriesData item = new SeriesData(series, plotData.color());
            seriesList.add(item);

            itemBooleanMap.put(item, createBooleanProperty(item));

            if (!item.series.getData().isEmpty()) {
                charts.add(lineChart);
                lastLineChart = lineChart;
                // Set random color for series
                lineChart.getData().add(item.series());
                setColorForSeries(item.series(), item.color());
                // Add chart to container
                stackPane.getChildren().add(lineChart);
            }
        }

        // ComboBox with checkboxes
        ComboBox<SeriesData> comboBox = new ComboBox<>(seriesList) {
            @Override
            protected javafx.scene.control.Skin<?> createDefaultSkin() {
                var skin = super.createDefaultSkin();
                ((ComboBoxListViewSkin) skin).setHideOnClick(false);
                return skin;
            }
        };

        comboBox.setValue(seriesList.isEmpty() ? null : seriesList.get(0));

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            oldVal.series().getChart().getYAxis().setOpacity(0);
            newVal.series().getChart().getYAxis().setOpacity(1);
            customizeLineChart(newVal.series(), true);
            customizeLineChart(oldVal.series(), false);
        });

        comboBox.setValue(seriesList.isEmpty() ? null : getNonEmptySeries(seriesList));

        comboBox.setCellFactory(listView -> {
            return new CheckBoxListCell<>(this::getItemBooleanProperty) {
                @Override
                public void updateItem(SeriesData item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setDisable(false); // it is required to fit default state
                    if (item != null) {
                        if (item.series().getData().isEmpty()) {
                            this.setDisable(true);
                            this.setStyle("-fx-text-fill: gray;");
                        } else {
                            this.setStyle("-fx-text-fill: " + getColorString(item.color()) + ";");
                        }
                    } 
                }
            };
        });

        comboBox.setButtonCell(new ListCell<SeriesData>() {
            @Override
            protected void updateItem(SeriesData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.series().getName());
                    setStyle("-fx-text-fill: " + getColorString(item.color()) + ";");
                }
            }
        });

        if (lastLineChart != null) {
            double zoomFactor = 1.1;
            lastLineChart.setOnScroll((ScrollEvent event) -> {
                // scroll amount
                double translateY = event.getDeltaY() / 40;
                for(LineChart<Number, Number> chart: charts) {
                    NumberAxis axis = (NumberAxis) chart.getXAxis();
                    double mous = axis.getValueForDisplay(event.getX() - axis.getLayoutX()).doubleValue();
                    zoom(axis, mous, translateY, zoomFactor);

                    if(!event.isControlDown()) {
                        axis = (NumberAxis) chart.getYAxis();
                        mous = axis.getValueForDisplay(event.getY() - axis.getLayoutY()).doubleValue();
                        zoom(axis, mous, translateY, zoomFactor);
                    }
                }
                event.consume(); // for prevent to scroll parent pane
            });
            setZoomHandlers(lastLineChart, charts);

            lastLineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickHandler);

            lastLineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                log.debug("MouseClicked: " + event.getX() + ", " + event.getY());
            });

        }

        Button close = new Button("X");
        HBox top = new HBox(close, new Label(file.getFile().getName()), comboBox);
        top.setSpacing(10);
        top.setAlignment(Pos.CENTER_RIGHT);

        zoomRect.setManaged(false);
        zoomRect.setFill(null);
        zoomRect.setStroke(javafx.scene.paint.Color.BLUE);

        root = new VBox(top, stackPane, zoomRect);
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(10));
        close.setOnMouseClicked(event -> {
            close();
        });

        root.setStyle("-fx-border-width: 2px; -fx-border-color: transparent;");
        root.setOnMouseClicked(event -> {
            model.selectAndScrollToChart(root);
            broadcast.fileSelected(file);
        });

        return root;
    }

    private List<XYChart.Data<Number, Number>> getSubsampleInRange(List<Number> data, int lowerIndex, int upperIndex) {
        // Validate indices
        if (lowerIndex < 0 || upperIndex > data.size() || lowerIndex >= upperIndex) {
            throw new IllegalArgumentException("Invalid range specified.");
        }

        if (data.stream().allMatch(Objects::isNull)) {
            return new ArrayList<>();
        }

        // Calculate the number of points to sample within the specified range
        int range = upperIndex - lowerIndex;
        int desiredNumberOfPoints = 2000;
        int actualNumberOfPoints = Math.min(desiredNumberOfPoints, range);

        List<XYChart.Data<Number, Number>> subsample = new ArrayList<>(actualNumberOfPoints);

        if (actualNumberOfPoints > 0) {
            int step = Math.max(1, range / actualNumberOfPoints);
            //if (step == 1) {
            //    actualNumberOfPoints = range;
            //}
            for (int i = lowerIndex; i < upperIndex; i += step) {
                subsample.add(new XYChart.Data<>(i, data.get(i)));
            }
        }

        return subsample;
    }

    public void zoomOut() {
        charts.forEach(LineChartWithMarkers::zoomOut);
    }

    public void close() {
        close(true);
    }

    public void close(boolean removeFromModel) {
        if (root.getParent() instanceof VBox) {
            // remove charts
            VBox parent = (VBox) root.getParent();
            parent.getChildren().remove(root);

            if (removeFromModel) {
                // remove files and traces from map
                model.getFileManager().removeFile(file);
                model.removeChart(file);
                model.initField();
                broadcast.notifyAll(new WhatChanged(Change.fileopened));
            }
        }
    }

    private void setZoomHandlers(LineChartWithMarkers chart, Set<LineChartWithMarkers> charts) {
        
        chart.setOnMousePressed(event -> {
            javafx.geometry.Point2D pointInScene = chart.parentToLocal(event.getX()+10, event.getY()+45);
            System.out.println(pointInScene);
            zoomRect.setX(pointInScene.getX());
            zoomRect.setY(pointInScene.getY());
            zoomRect.setWidth(0);
            zoomRect.setHeight(0);
            zoomRect.setVisible(true);
        });

        chart.setOnMouseDragged(event -> {
            javafx.geometry.Point2D pointInScene = chart.parentToLocal(event.getX()+10, event.getY()+45);
            zoomRect.setWidth(Math.abs(pointInScene.getX() - zoomRect.getX()));
            zoomRect.setHeight(Math.abs(pointInScene.getY() - zoomRect.getY()));
            zoomRect.setX(Math.min(pointInScene.getX(), zoomRect.getX()));
            zoomRect.setY(Math.min(pointInScene.getY(), zoomRect.getY()));
        });

        chart.setOnMouseReleased(event -> {
            double startX = zoomRect.getX();
            double endX = startX + zoomRect.getWidth();
            System.out.println("w: " + zoomRect.getWidth() + " h:" + zoomRect.getHeight());

            double startY = zoomRect.getY();
            double endY = startY + zoomRect.getHeight();

            if (zoomRect.getWidth() < 10 || zoomRect.getHeight() < 10) {
                zoomRect.setVisible(false);
                return;
            }
            
            for(LineChartWithMarkers c: charts) {
                NumberAxis xAxis = (NumberAxis) c.getXAxis();
                Number xMin = xAxis.getValueForDisplay(startX - 25);
                Number xMax = xAxis.getValueForDisplay(endX - 25);
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(xMin.doubleValue());
                xAxis.setUpperBound(xMax.doubleValue());

                System.out.println("xMin: " + xMin + " xMax: " + xMax);
                c.getData().forEach(series -> {
                    int lowerIndex = Math.clamp(xMin.intValue(), 0, c.plotData.data().size()-1);
                    int upperIndex = Math.clamp(xMax.intValue(), 0, c.plotData.data().size());
                    System.out.println("lowerIndex: " + lowerIndex + " upperIndex: " + upperIndex + " size: " + c.plotData.data().size());
                    //series.getData().addAll(getSubsample(calculateAverages(c.plotData.data().subList(lowerIndex, upperIndex)), lowerIndex, upperIndex));
                    series.getData().addAll(getSubsampleInRange(c.plotData.data() //.subList(lowerIndex, upperIndex)
                    , lowerIndex, upperIndex));

                });
                
                NumberAxis yAxis = (NumberAxis) c.getYAxis();
                Number yMax = yAxis.getValueForDisplay(startY - 60);
                Number yMin = yAxis.getValueForDisplay(endY - 60);
                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(yMin.doubleValue());
                yAxis.setUpperBound(yMax.doubleValue());
            }

            zoomRect.setVisible(false); 
        });
    }

    private SeriesData getNonEmptySeries(ObservableList<SeriesData> seriesList) {
        return seriesList.stream().filter(s -> !s.series().getData().isEmpty()).findFirst().orElse(null);
    }

    private Data<Number, Number> currentVerticalMarker = null;
    private VBox root;

    public void removeVerticalMarker() {
        if (lastLineChart != null && currentVerticalMarker != null) {
            lastLineChart.removeVerticalValueMarker(currentVerticalMarker);
            currentVerticalMarker = null;
        }
    }

    public void putVerticalMarker(int x) {
        if (lastLineChart != null) {
            if (currentVerticalMarker != null) {
                lastLineChart.removeVerticalValueMarker(currentVerticalMarker);
            }
            currentVerticalMarker = new Data<>(x, 0);
            lastLineChart.addVerticalValueMarker(currentVerticalMarker);
        }
    }

    private static void zoom(NumberAxis axis, double mous, double translateY, double zoomFactor) {
        ZoomDeltas zoom = getZoomDeltas(axis, mous, translateY, zoomFactor);
        axis.setAutoRanging(false);
        axis.setLowerBound(mous - zoom.deltaLower);
        axis.setUpperBound(mous + zoom.deltaUpper);
    }

    private static @NotNull ZoomDeltas getZoomDeltas(NumberAxis axis, double mous, double translateY, double zoomFactor) {
        double deltaLower  = (mous - axis.getLowerBound());
        double deltaUpper = (axis.getUpperBound() - mous);
        if (translateY > 0) {
            deltaLower = deltaLower / zoomFactor;
            deltaUpper = deltaUpper / zoomFactor;
        } else {
            deltaLower = deltaLower * zoomFactor;
            deltaUpper = deltaUpper * zoomFactor;
        }
        ZoomDeltas zoom = new ZoomDeltas(deltaLower, deltaUpper);
        return zoom;
    }

    private record ZoomDeltas(double deltaLower, double deltaUpper) {
    }

    private NumberAxis getXAxis(int tickUnit) {
        NumberAxis xAxis = new NumberAxis();
        //xAxis.setLabel("Common X Axis");
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        //xAxis.setAutoRanging(false);
        //xAxis.setUpperBound(2000);
        xAxis.setTickUnit(tickUnit);
        return xAxis;
    }

    private int getFloorMin(List<Number> data) {
        int min = data.stream().filter(Objects::nonNull).mapToInt(n -> n.intValue()).min().orElse(0);
        int baseMin = (int) Math.pow(10, (int) Math.clamp(Math.log10(Math.abs(min)), 0, 3));
        return baseMin == 1 ? (min >= 0 ? 0 : -10) : Math.floorDiv(min, baseMin) * baseMin;
    }

    private int getCeilMax(List<Number> data) {
        int max = data.stream().filter(Objects::nonNull).mapToInt(n -> n.intValue()).max().orElse(0);
        int baseMax = (int) Math.pow(10, (int) Math.clamp(Math.log10(max), 0, 3));
        return baseMax == 1 ? 10 : Math.ceilDiv(max, baseMax) * baseMax;
    }

    private BooleanProperty createBooleanProperty(SeriesData item) {
        final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "visible", !item.series().getData().isEmpty());
        booleanProperty.addListener((observable, oldValue, newValue) -> {
            if (item.series().getNode() != null) {
                item.series().getNode().setVisible(newValue);
                //if (isSelected && series.getChart().getYAxis().getOpacity() > 0) {
                    //((LineChart) series.getChart()).setCreateSymbols(true);
                //}
            }
            item.series().getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().setVisible(newValue);
                }
            });
        });

        return booleanProperty;
    }

    private void customizeLineChart(Series<Number, Number> series, boolean isVisible) {
        LineChart<Number, Number> lineChart = (LineChart) series.getChart();
        //lineChart.setCreateSymbols(series.getNode().isVisible() && isVisible);
        lineChart.setVerticalGridLinesVisible(isVisible);
        lineChart.setHorizontalGridLinesVisible(isVisible);
        lineChart.setHorizontalZeroLineVisible(isVisible);
        lineChart.setVerticalZeroLineVisible(isVisible);
    }

    // Apply color to data series
    private void setColorForSeries(XYChart.Series<Number, Number> series, Color color) {
        String colorString = getColorString(color);
        System.out.println("Color: " + colorString);
        // Apply style to series
        series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + colorString + ";" + "-fx-stroke-width: 0.6px;");
    }

    private String getColorString(Color color) {
        // Convert color to string in HEX format
        String colorString = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        return colorString;
    }

    private class LineChartWithMarkers extends LineChart<Number, Number> {

        private final ObservableList<Data<Number, Number>> horizontalMarkers;
        private final ObservableList<Data<Number, Number>> verticalMarkers;

        private final int xLowerBound;
        private final int xUpperBound;
        private final int yLowerBound;
        private final int yUpperBound;

        private PlotData plotData;
        //private final List<Number> data;

        //private final List<Data<Number, Number>> subsampleInFullRange;
        
        public LineChartWithMarkers(Axis<Number> xAxis, Axis<Number> yAxis, int xLowerBound, int xUpperBound, int yLowerBound, int yUpperBound, PlotData plotData) {
            super(xAxis, yAxis);
            this.xLowerBound = xLowerBound;
            this.xUpperBound = xUpperBound;
            this.yLowerBound = yLowerBound;
            this.yUpperBound = yUpperBound;
            horizontalMarkers = FXCollections.observableArrayList(data -> new javafx.beans.Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new javafx.beans.Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            
            this.plotData = plotData;

            //System.out.println("start filter " + System.currentTimeMillis());
            //var plotFilteredData = filterList(plotData.data());
            //System.out.println("complete filter " + System.currentTimeMillis());
            //this.plotData = new PlotData(plotData.semantic, plotData.units, plotData.color, plotFilteredData);

            //subsampleInFullRange = getSubsampleInRange(this.plotData.data, xLowerBound, xUpperBound);
        }

        public void zoomOut() {
            NumberAxis xAxis = (NumberAxis) getXAxis();
            NumberAxis yAxis = (NumberAxis) getYAxis();
            //xAxis.setAutoRanging(true);
            //yAxis.setAutoRanging(true);
            xAxis.setAutoRanging(false);
            yAxis.setAutoRanging(false);
            xAxis.setLowerBound(xLowerBound);
            xAxis.setUpperBound(xUpperBound);
            yAxis.setLowerBound(yLowerBound);
            yAxis.setUpperBound(yUpperBound);

            // Add data to chart
            // TODO: better to recreate series because clear is too slow
            if (!plotData.data.isEmpty()) {
                getData().forEach(series -> {
                    System.out.println("start clear: " + System.currentTimeMillis());
                    series.getData().clear();
                    System.out.println("complete clear: " + System.currentTimeMillis());
                    series.getData().setAll(getSubsampleInRange(this.plotData.data, xLowerBound, xUpperBound));
                });
            }
        }

        public void addHorizontalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        public void removeHorizontalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            horizontalMarkers.remove(marker);
        }

        public void addVerticalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalMarkers.contains(marker)) return;
            Line line = new Line();
            
            line.setStroke(Color.RED); // Bright color for white background
            line.setStrokeWidth(2);


            ImageView imageView = ResourceImageHolder.getImageView("gps32.png");

            VBox markerBox = new VBox();
            markerBox.setAlignment(Pos.TOP_CENTER);
            markerBox.getChildren().addAll(imageView, line);

            marker.setNode(markerBox);

            getPlotChildren().add(markerBox);
            verticalMarkers.add(marker);
        }

        public void removeVerticalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            verticalMarkers.remove(marker);
        }


        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            for (Data<Number, Number> horizontalMarker : horizontalMarkers) {
                Line line = (Line) horizontalMarker.getNode();
                line.setStartX(0);
                line.setEndX(getBoundsInLocal().getWidth());
                line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
                line.setEndY(line.getStartY());
                line.toFront();
            }
            for (Data<Number, Number> verticalMarker : verticalMarkers) {
                VBox line = (VBox) verticalMarker.getNode();
                line.setLayoutX(getXAxis().getDisplayPosition(verticalMarker.getXValue()));// + 0.5);  // 0.5 for crispness
                for(Node node: line.getChildren()) {
                    if (node instanceof Line) {
                        Line l = (Line) node;
                        l.setStartY(0d);
                        l.setEndY(getBoundsInLocal().getHeight());
                        l.toFront();
                    }
                }
                line.setLayoutY(0d);
                line.setMinHeight(getBoundsInLocal().getHeight());
                line.toFront();
            }      
        }

    }
}