package com.ugcs.gprvisualizer.app;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.scene.control.skin.ComboBoxListViewSkin;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;


public class SensorLineChart {

    private final Model model;
    private final Broadcast broadcast;

    public SensorLineChart(Model model, Broadcast broadcast) {
        this.model = model;
        this.broadcast = broadcast;
    }

    public Map<SgyFile, List<PlotData>> generatePlotData(File csvFile) {
        //TODO: design it better
        var file = model.getFileManager().getFiles().stream().filter(f -> {
            return f.getFile() != null && Objects.equals(csvFile.getName(), f.getFile().getName());
        }).findAny().get();
        List<GeoData> geoData = file.getGeoData(csvFile);
        Map<String, List<SensorValue>> sensorValues = new HashMap<>();
        geoData.forEach(data -> {
            data.getSensorValues().forEach(value -> {
                sensorValues.compute(value.semantic() + "--" + (StringUtils.hasText(value.units()) ? value.units() : " "), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    if (value.data() != null) {
                        v.add(value);
                    }
                    return v;
                });
            });
        });
        var plotDataList = new ArrayList<PlotData>();
        for (String k: sensorValues.keySet()) {
            var pd = k.split("--");
            List<SensorValue> values = sensorValues.get(k);
            PlotData plotData = new PlotData(pd[0], pd[1], calculateAverages(values.stream()
                    .map(SensorValue::data)
                    .collect(Collectors.toList())));
            plotDataList.add(plotData);
        }
        //var plotData = List.of(
        //        new PlotData("Semantic 1", "Nm", List.of(1, 22, 33, 4, 54, 6, 7, 8, 9, 10)),
        //        new PlotData("Semantic 2", "mm", List.of(10, 1, 3, 7, 6, 4, 4, 3, 2, 1)),
        //        new PlotData("Semantic 3", "Sm", List.of(10, 93, 833, 73, 63, 53, 43, 33, 23, 13)),
        //        new PlotData("Semantic 4", "SS", List.of(20, 40, 66, 88, 100, 120, 777, 80, 90, 1023))
        //);
        return Map.of(file, plotDataList);
    }

    public static List<Number> calculateAverages(List<Number> sourceList) {
        if (sourceList.isEmpty()) return sourceList;
        var scale = sourceList.size() / Math.clamp(sourceList.size(), 1, 2000);
        return IntStream.range(0, sourceList.size() / scale) // Create an index stream from 0 to 999
                .mapToObj(i -> sourceList.subList(i * scale, (i + 1) * scale)) // Transform each index into a sublist of 100 elements
                .map(sublist -> sublist.stream() // Create a stream from sublist
                        .mapToDouble(n -> n.doubleValue())
                        //.mapToInt(Integer::intValue) // Convert Integer to int
                        .average() // Calculate average value
                        .orElseThrow(() -> new IllegalArgumentException("Cannot calculate average of an empty list."))) // Handle empty sublist
                //.map(d -> d.intValue())
                .collect(Collectors.toList()); // Collect results in a list
    }
    record PlotData(String semantic, String units, List<Number> data) {}
    record SeriesData(XYChart.Series<Number, Number> series) {
        @Override
        public final String toString() {
            return series.getName();
        }
    }

    public BooleanProperty getItemBooleanProperty(SeriesData item) {
        return itemBooleanMap.get(item);
    }

    private Map<SeriesData, BooleanProperty> itemBooleanMap = new HashMap<>();

    private LineChartWithMarkers<Number, Number> lastLineChart = null;

    public VBox createChartWithMultipleYAxes(SgyFile file, List<PlotData> plotData) {
        // Using StackPane to overlay charts
        StackPane stackPane = new StackPane();
        ObservableList<SeriesData> seriesList = FXCollections.observableArrayList();
        Set<LineChart<Number,Number>> charts = new HashSet<>();

        for (int i = 0; i < plotData.size(); i++) {
            // X-axis, common for all charts
            NumberAxis xAxis = getXAxis();

            // Y-axis
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(plotData.get(i).units());
            yAxis.setSide(javafx.geometry.Side.RIGHT); // Y-axis on the right
            yAxis.setMinorTickVisible(false);
            yAxis.setAutoRanging(false);
            var min = getFloorMin(plotData.get(i).data());
            var max = getCeilMax(plotData.get(i).data());
            yAxis.setLowerBound(min);
            yAxis.setUpperBound(max);
            yAxis.setTickUnit((max - min)/10);
            yAxis.setPrefWidth(70);
            if (i > 0) {
                yAxis.setOpacity(0);
            }

            // Creating chart
            LineChartWithMarkers<Number, Number> lineChart = new LineChartWithMarkers<>(xAxis, yAxis);
            charts.add(lineChart);
            lastLineChart = lineChart;
            lineChart.setLegendVisible(false); // Hide legend
            lineChart.setCreateSymbols(false); // Disable symbols
            if (i > 0) {
                lineChart.setLegendVisible(false); // Hide legend for second chart
                lineChart.setCreateSymbols(false); // Disable symbols
                lineChart.setVerticalGridLinesVisible(false);
                lineChart.setHorizontalGridLinesVisible(false);
                lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
            }
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(plotData.get(i).semantic());
            // Add data to chart
            for (int j = 0; j < plotData.get(i).data().size(); j++) {
                series.getData().add(new XYChart.Data<>(j, plotData.get(i).data().get(j)));
            }

            SeriesData item = new SeriesData(series);
            seriesList.add(item);

            itemBooleanMap.put(item, createBooleanProperty(item));

            // Set random color for series
            lineChart.getData().add(series);
            setColorForSeries(series, generateRandomColor());
            // Add chart to container
            stackPane.getChildren().add(lineChart);
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
            CheckBoxListCell<SeriesData> checkBoxListCell = new CheckBoxListCell<>(this::getItemBooleanProperty) {
                @Override
                public void updateItem(SeriesData item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setDisable(false); // it is required to fit default state
                    if (item != null && item.series().getData().isEmpty()) {
                        this.setDisable(true);
                        this.setStyle("-fx-text-fill: gray;");
                    }
                }
            };
            return checkBoxListCell;
        });

        if (lastLineChart != null) {
            double zoomFactor = 1.01;
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
        }

        Button close = new Button("X");
        HBox top = new HBox(close, new Label(file.getFile().getName()), comboBox);
        top.setSpacing(10);
        top.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(top, stackPane);
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(10));
        close.setOnMouseClicked(event -> {
            if (root.getParent() instanceof VBox) {
                // remove charts
                VBox parent = (VBox) root.getParent();
                parent.getChildren().remove(root);

                // remove files and traces from map
                model.getFileManager().getFiles().remove(file);
                model.initField();
                broadcast.notifyAll(new WhatChanged(Change.fileopened));
            }
        });
        return root;
    }

    private SeriesData getNonEmptySeries(ObservableList<SeriesData> seriesList) {
        return seriesList.stream().filter(s -> !s.series().getData().isEmpty()).findFirst().orElse(null);
    }

    private Data<Number, Number> currentVerticalMarker = null;

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

    private NumberAxis getXAxis() {
        NumberAxis xAxis = new NumberAxis();
        //xAxis.setLabel("Common X Axis");
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(2000);
        xAxis.setTickUnit(200);
        return xAxis;
    }

    private int getFloorMin(List<Number> data) {
        int min = data.stream().mapToInt(n -> n.intValue()).min().orElse(0);
        int baseMin = (int) Math.pow(10, (int) Math.clamp(Math.log10(min), 0, 3));
        return baseMin == 1 ? 0 : Math.floorDiv(min, baseMin) * baseMin;
    }

    private int getCeilMax(List<Number> data) {
        int max = data.stream().mapToInt(n -> n.intValue()).max().orElse(0);
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
    }
    static class CheckableItem {
        private final String name;
        private final CheckBox checkBox;
        public CheckableItem(String name) {
            this.name = name;
            this.checkBox = new CheckBox(name);
        }
        public CheckBox getCheckBox() {
            return checkBox;
        }
        public String getName() {
            return name;
        }
    }
    // Generate random color
    private Color generateRandomColor() {
        var brightColors = List.of(Color.web("#E6194B"), // Red
                Color.web("#3CB44B"), // Green
                Color.web("#4363D8"), // Dark Blue
                Color.web("#F58231"), // Orange
                Color.web("#911EB4"), // Purple
                Color.web("#F032E6"), // Magenta
                Color.web("#008080"), // Teal
                Color.web("#9A6324"), // Brown
                Color.web("#800000"), // Maroon
                Color.web("#808000"), // Olive
                Color.web("#000075"), // Navy Blue
                Color.web("#00FF00"), // Bright Green
                Color.web("#FF4500"), // Orange Red
                Color.web("#DA70D6") // Orchid
        );
        Random rand = new Random();
        return brightColors.get(rand.nextInt(brightColors.size()));
    }
    // Apply color to data series
    private void setColorForSeries(XYChart.Series<Number, Number> series, Color color) {
        // Convert color to string in HEX format
        String colorString = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        System.out.println("Color: " + colorString);
        // Apply style to series
        series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + colorString + ";" + "-fx-stroke-width: 0.6px;");
    }

    private class LineChartWithMarkers<X,Y> extends LineChart<X, Y> {

        private ObservableList<Data<X, Y>> horizontalMarkers;
        private ObservableList<Data<X, Y>> verticalMarkers;

        public LineChartWithMarkers(Axis<X> xAxis, Axis<Y> yAxis) {
            super(xAxis, yAxis);
            horizontalMarkers = FXCollections.observableArrayList(data -> new javafx.beans.Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new javafx.beans.Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        }

        public void addHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        public void removeHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            horizontalMarkers.remove(marker);
        }

        public void addVerticalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            verticalMarkers.add(marker);
        }

        public void removeVerticalValueMarker(Data<X, Y> marker) {
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
            for (Data<X, Y> horizontalMarker : horizontalMarkers) {
                Line line = (Line) horizontalMarker.getNode();
                line.setStartX(0);
                line.setEndX(getBoundsInLocal().getWidth());
                line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
                line.setEndY(line.getStartY());
                line.toFront();
            }
            for (Data<X, Y> verticalMarker : verticalMarkers) {
                Line line = (Line) verticalMarker.getNode();
                line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
                line.setEndX(line.getStartX());
                line.setStartY(0d);
                line.setEndY(getBoundsInLocal().getHeight());
                line.toFront();
            }      
        }

    }
}