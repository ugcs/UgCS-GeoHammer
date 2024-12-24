package com.ugcs.gprvisualizer.app;

import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.thecoldwine.sigrun.common.ext.*;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.app.parcers.GeoCoordinates;
import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.event.FileSelectedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.utils.Nodes;
import com.ugcs.gprvisualizer.utils.Range;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

import com.ugcs.gprvisualizer.app.fir.FIRFilter;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.app.parcers.SensorValue;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PrefSettings;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

public class SensorLineChart extends ScrollableData implements FileDataContainer {

    private static final double ZOOM_STEP = 1.38;

    private static final String FILTERED_SERIES_SUFFIX = "_filtered";

    private static final Logger log = LoggerFactory.getLogger(SensorLineChart.class);

    private final Model model;
    private final ApplicationEventPublisher eventPublisher;
    private final PrefSettings settings;
    private final AuxElementEditHandler auxEditHandler;

    private Map<SeriesData, BooleanProperty> itemBooleanMap = new HashMap<>();
    private LineChartWithMarkers lastLineChart = null;
    private Set<LineChartWithMarkers> charts = new HashSet<>();
    private Rectangle selectionRect = new Rectangle();
    private final ProfileScroll profileScroll;

    //private int scale;
    private CsvFile file;
    //private Node rootNode;

    // data filters: allow to skip duplicate points in chart on add
    private Map<List<Number>, Set<Number>> dataFilters = new HashMap<>();

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> updateTask;

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(scheduler::shutdownNow));
    }

    public SensorLineChart(Model model, ApplicationEventPublisher eventPublisher, PrefSettings settings, AuxElementEditHandler auxEditHandler) {
        this.model = model;
        this.eventPublisher = eventPublisher;
        this.settings = settings;
        this.auxEditHandler = auxEditHandler;
        this.profileScroll = new ProfileScroll(model, this);
    }

    public void addFlag(FoundPlace fp) {
        if (!foundPlaces.containsKey(fp)) {
            putFoundPlace(fp);
            removeVerticalMarker();
            if (fp.isSelected()) {
                selectFlag(fp);
            }
        }
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
                var yAxis = (NumberAxis) chart.getYAxis();

                ZoomRect zoomRect = new ZoomRect(lowerIndex, upperIndex, yAxis.getLowerBound(), yAxis.getUpperBound());
                chart.setZoomRect(zoomRect);
                chart.updateLineChartData();
            }
        }
        model.selectAndScrollToChart(this);
        putVerticalMarker(selectedX);
    }

    private EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
        	if (event.getClickCount() == 2) {
                log.debug("Double click,  x: " + event.getX() + " y: " + event.getY() + " source: " + event.getSource());

                model.chartsClearSelection();

                if (event.getSource() instanceof LineChart) {
                    Axis<Number> xAxis = ((LineChart) event.getSource()).getXAxis();
                    Point2D point = xAxis.screenToLocal(event.getScreenX(), event.getScreenY());
                    Number xValue = xAxis.getValueForDisplay(point.getX());
                    System.out.println("xValue: " + xValue.intValue());

                    putVerticalMarker(xValue.intValue());

                    int traceIndex = xValue.intValue();
                    if (traceIndex >= 0 && traceIndex < file.getTraces().size()) {//model.getCsvTracesCount()) {
                        GeoData geoData = file.getGeoData().get(traceIndex);
                        model.getMapField().setSceneCenter(new LatLon(geoData.getLatitude(), geoData.getLongitude()));
                        model.createClickPlace(file, file.getTraces().get(traceIndex));
                        eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.mapscroll));
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
            //        .collect(Collectors.toList()));
            plotDataList.add(plotData);
        }
        return plotDataList;
    }

    private Color getColor(String semantic) {
        return model.getColorBySemantic(semantic);
    }

    @Override
    public Node getRootNode() {
        return root;
    }

    @Override
    public void selectFile() {
        eventPublisher.publishEvent(new FileSelectedEvent(this, file));
    }

    @Override
    public int getTracesCount() {
        return file.getTraces().size();
    }

    public record PlotData(String semantic, String units, Color color, List<Number> data, boolean rendered) {
        
        public PlotData(String semantic, String units, Color color, List<Number> data) {
            this(semantic, units, color, data, false);
        }

        public PlotData withData(List<Number> data) {
            return new PlotData(semantic, units, color, data);
        }

        /**
         * Set rendered flag to true
         * @return PlotData with rendered flag set to true
         */
        public PlotData render() {
            return new PlotData(semantic, units, color, data, true);
        }

        public String getPlotStyle() {
            return "-fx-stroke: " + getColorString(color) + ";" + "-fx-stroke-width: 0.6px;";
        }
    }

    private static String getColorString(Color color) {
        // Convert color to string in HEX format
        String colorString = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        return colorString;
    }

    record SeriesData(Series<Number, Number> series, Color color) {
        @Override
        public final String toString() {
            return series.getName();
        }
    }

    public BooleanProperty getItemBooleanProperty(SeriesData item) {
        return itemBooleanMap.get(item);
    }

    ComboBox<SeriesData> comboBox;

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
            yAxis.setSide(Side.RIGHT); // Y-axis on the right
            yAxis.setMinorTickVisible(false);
            var data = plotData.data();

            var min = getFloorMin(data, plotData.semantic);
            var max = getCeilMax(data, plotData.semantic);

            yAxis.setTickUnit((max - min) / 10);
            yAxis.setPrefWidth(70);
            if (i > 0) {
                yAxis.setOpacity(0);
            }

            // Creating chart
            LineChartWithMarkers lineChart = new LineChartWithMarkers(xAxis, yAxis,
                    new ZoomRect(0, plotData.data().size(), min, max), plotData);
            lineChart.resetZoomRect();

            lineChart.setLegendVisible(false); // Hide legend
            lineChart.setCreateSymbols(false); // Disable symbols
            if (i > 0) {
                lineChart.setVerticalGridLinesVisible(false);
                lineChart.setHorizontalGridLinesVisible(false);
                lineChart.setHorizontalZeroLineVisible(false);
                lineChart.setVerticalZeroLineVisible(false);
                lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
            }

            Series<Number, Number> series = new Series<>();
            series.setName(plotData.semantic());

            Series<Number, Number> filtered = new Series<>();
            filtered.setName(plotData.semantic() + FILTERED_SERIES_SUFFIX);

            // Add data to chart
            if (!data.isEmpty()) {
                addSeriesDataFiltered(series, data, 0, data.size());
            }

            SeriesData item = new SeriesData(series, plotData.color());
            seriesList.add(item);

            itemBooleanMap.put(item, createBooleanProperty(item));

            if (!item.series.getData().isEmpty()) {
                charts.add(lineChart);                
                lastLineChart = lineChart;
                // Set random color for series
                lineChart.getData().add(item.series());
                setStyleForSeries(item.series(), plotData.getPlotStyle());

                lineChart.getData().add(filtered);
                setStyleForSeries(filtered, plotData.getPlotStyle());

                lineChart.getData().forEach(s -> {
                    if (s.getNode() != null) {
                        s.getNode().setVisible(itemBooleanMap.get(item).get());
                    }
                });
                // Add chart to container
                stackPane.getChildren().add(lineChart);
            }
        }

        // ComboBox with checkboxes
        comboBox = new ComboBox<>(seriesList) {
            @Override
            protected Skin<?> createDefaultSkin() {
                var skin = super.createDefaultSkin();
                ((ComboBoxListViewSkin) skin).setHideOnClick(false);
                return skin;
            }
        };

        comboBox.setValue(seriesList.isEmpty() ? null : seriesList.get(0));

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectLineChart(newVal.series(), true);
            selectLineChart(oldVal.series(), false);
        });

        comboBox.setValue(seriesList.isEmpty() ? null : getNonEmptySeries(seriesList));

        comboBox.setCellFactory(listView -> {
            return new CheckBoxListCell<>(this::getItemBooleanProperty) {
                @Override
                public void updateItem(SeriesData item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setDisable(false); // it is required to fit the default state
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
            setSelectionHandlers(lastLineChart);
            setScrollHandlers(lastLineChart);

            lastLineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickHandler);

            lastLineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                log.debug("MouseClicked: " + event.getX() + ", " + event.getY());
            });

            charts.forEach(chart -> {
                if (GeoData.Semantic.LINE.getName().equals(chart.plotData.semantic)) {
                    int yValue = -1;
                    for (int i = 0; i < chart.plotData.data.size(); i++) {
                        if (chart.plotData.data.get(i) == null) {
                            continue;
                        }
                        var currentYValue = chart.plotData.data.get(i).intValue();
                        if (yValue != currentYValue) {
                            yValue = currentYValue;
                            Data<Number, Number> verticalMarker = new Data<>(i, 0);

                            Line line = new Line();
                            line.setStroke(chart.plotData.color);
                            line.setStrokeWidth(0.8);

                            Tooltip tooltip = new Tooltip("Remove Line " + currentYValue);
                            ImageView imageView = ResourceImageHolder.getImageView("closeFile.png");
                            Tooltip.install(imageView, tooltip);

                            imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                                imageView.setCursor(Cursor.HAND);
                            });
                            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                                System.out.println(event.getX() + ", " + event.getY() + ", " + currentYValue);

                                List<GeoData> geoDataLinesList = new ArrayList<>();
                                List<Trace> sublist = new ArrayList<>();
                                List<BaseObject> auxElements = new ArrayList<>();
                                for(GeoData geoData: file.getGeoData()) {
                                    if (geoData.getLine().data() != null && geoData.getLine().data().intValue() != currentYValue) {
                                        sublist.add(file.getTraces().get(geoData.getTraceNumber()));
                                        GeoData gd = new GeoData(geoData);
                                        if (geoData.getLine().data().intValue() > currentYValue) {
                                            gd.setLine(gd.getLine().data().intValue() - 1);
                                        }
                                        file.getAuxElements().stream().filter(FoundPlace.class::isInstance)
                                                .map(o -> ((FoundPlace) o))
                                                .filter(fp -> fp.getTraceInFile() == geoData.getTraceNumber())
                                                .forEach(fp -> auxElements.add(fp));
                                        geoDataLinesList.add(gd);
                                    }
                                }
                                CsvFile subfile = file.copy();
                                subfile.setUnsaved(true);

                                subfile.setTraces(sublist);
                                subfile.getGeoData().addAll(geoDataLinesList);
                                subfile.setAuxElements(auxElements);
                                subfile.updateInternalIndexes();

                                model.getFileManager().removeFile(file);
                                model.getFileManager().addFile(subfile);
                                model.updateChart((CsvFile) subfile);

                                model.init();
                                model.initField();
                                model.getProfileField().clear();
                                eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.traceCut));
                            });

                            imageView.setTranslateX(1);
                            imageView.setTranslateY(1);

                            Pane pane = new Pane();
                            pane.getChildren().add(imageView);

                            //lastLineChart.addVerticalValueMarkerWithButton(verticalMarker, line, imageView);
                            lastLineChart.addVerticalValueMarker(verticalMarker, line, null, pane);
                        }
                    }
                }
            });
        }

        file.getAuxElements().stream().map(o -> ((FoundPlace) o))
                .forEach(this::putFoundPlace);

        Button close = new Button("X");
        HBox top = new HBox(close, new Label(file.getFile().getName()), comboBox);
        top.setSpacing(10);
        top.setAlignment(Pos.CENTER_RIGHT);

        selectionRect.setManaged(false);
        selectionRect.setFill(null);
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setMouseTransparent(true);

        root = new VBox(top, stackPane, selectionRect);
        root.setFillWidth(true);
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(10));

        close.setOnMouseClicked(event -> {
            close();
        });

        root.setStyle("-fx-border-width: 2px; -fx-border-color: transparent;");
        root.setOnMouseClicked(event -> {
            if (model.selectAndScrollToChart(this)) {
                //eventPublisher.publishEvent(new FileSelectedEvent(file));
            }
        });

        //this.rootNode = root;

        //root.getChildren().add(0, profileScroll);

        profileScroll.widthProperty().bind(top.widthProperty());

        return root;
    }

    private Map<FoundPlace, Data<Number, Number>> foundPlaces = new HashMap<>();

    private void putFoundPlace(FoundPlace fp) {
        Data<Number, Number> verticalMarker = new Data<>(fp.getTraceInFile(), 0);

        var color = javafx.scene.paint.Color.rgb(
            fp.getFlagColor().getRed(),
            fp.getFlagColor().getGreen(),
            fp.getFlagColor().getBlue(),
            fp.getFlagColor().getAlpha() / 255.0
        );
        Line line = new Line();
        line.setStroke(color);
        line.setStrokeWidth(0.8);
        line.setTranslateY(46);

        var flagMarker = createFlagMarker(color);
        flagMarker.setTranslateX(0);
        flagMarker.setTranslateY(28);

        flagMarker.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            flagMarker.setCursor(Cursor.HAND);
        });

        flagMarker.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            selectFlag(fp);
            fp.mousePressHandle(new Point2D(event.getScreenX(), event.getScreenY()), this);
            event.consume();
        });

        flagMarker.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            event.consume();
        });

        foundPlaces.put(fp, verticalMarker);
        lastLineChart.addVerticalValueMarker(verticalMarker, line, null, flagMarker);
    }

    @Override
    public void setMiddleTrace(int trace) {
        super.setMiddleTrace(trace);
        foundPlaces.keySet().stream().filter(f -> f.getTraceInFile() == trace ).forEach(fp -> {
            selectFlag(fp);
        });
    }

    @Override
    public int getVisibleNumberOfTrace() {
        //TODO: for scroll
        return 0;
    }

    private void selectFlag(FoundPlace fp) {
        foundPlaces.keySet().forEach(f -> {
            var markerBox = (Pane) foundPlaces.get(f).getNode();
            Line l = (Line) markerBox.getChildren().get(1);
            l.setStrokeType(StrokeType.CENTERED);
            var fm = (Pane) ((Pane) markerBox.getChildren().get(0)).getChildren().get(0);
            fm.getChildren().stream().filter(ch -> ch instanceof Shape).forEach(
                    ch -> ((Shape) ch).setStrokeType(StrokeType.CENTERED)
            );
            f.setSelected(false);
        });

        var markerBox = (Pane) foundPlaces.get(fp).getNode();
        Line l = (Line) markerBox.getChildren().get(1);
        l.setStrokeType(StrokeType.OUTSIDE);
        var fm = (Pane) ((Pane) markerBox.getChildren().get(0)).getChildren().get(0);
        fm.getChildren().stream().filter(ch -> ch instanceof Shape).forEach(
                ch -> ((Shape) ch).setStrokeType(StrokeType.OUTSIDE)
        );
        fp.setSelected(true);
    }

    private List<Data<Number, Number>> getSubsampleInRange(List<Number> data, int lowerIndex, int upperIndex,
            Set<Number> filter) {
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

        List<Data<Number, Number>> subsample = new ArrayList<>(actualNumberOfPoints);

        if (actualNumberOfPoints > 0) {
            int step = Math.max(1, range / actualNumberOfPoints);
            for (int i = lowerIndex; i < upperIndex; i += step) {
                Number key = i;
                if (filter != null && filter.contains(key))
                    continue;
                subsample.add(new Data<>(key, data.get(i)));
            }
        }

        return subsample;
    }

    private void addSeriesDataFiltered(XYChart.Series<Number, Number> series,
                                       List<Number> data, int lowerIndex, int upperIndex) {
        Set<Number> dataFilter = dataFilters.computeIfAbsent(data, k -> new HashSet<>());
        List<Data<Number, Number>> subsample = getSubsampleInRange(data, lowerIndex, upperIndex, dataFilter);
        series.getData().addAll(subsample);
        subsample.forEach(v -> {
            dataFilter.add(v.getXValue());
        });
    }

    /**
     * Zoom to full range
     */
    public void zoomToFit() {
        for (LineChartWithMarkers chart : charts) {
            chart.resetZoomRect();
        }
        Platform.runLater(this::updateChartData);
    }

    public void zoomIn() {
        double scale = 1.0 / ZOOM_STEP;
        zoom(scale, 1.0, null);
        Platform.runLater(this::updateChartData);
    }

    public void zoomOut() {
        double scale = ZOOM_STEP;
        zoom(scale, 1.0, null);
        Platform.runLater(this::updateChartData);
    }

    private void zoomToArea(Point2D start, Point2D end) {
        for (LineChartWithMarkers chart : charts) {
            ZoomRect zoomRect = chart.createZoomRectForArea(start, end);
            chart.setZoomRect(zoomRect);
        }
    }

    private void zoom(double scaleX, double scaleY, Point2D scaleCenter) {
        for (LineChartWithMarkers chart : charts) {
            ZoomRect zoomRect = chart.scaleZoomRect(chart.zoomRect, scaleX, scaleY, scaleCenter);
            chart.setZoomRect(zoomRect);
        }
    }

    private void updateChartData() {
        for (LineChartWithMarkers chart : charts) {
            chart.updateLineChartData();
        }
    }

    /**
     * Close chart
     */
    public void close() {
        close(true);
    }

    /**
     * Close chart
     * @param removeFromModel
     */
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
                eventPublisher.publishEvent(new FileOpenedEvent(this));
            }
        }
    }

    private void setSelectionRectFromDragBounds(DragBounds bounds) {
        selectionRect.setX(bounds.getMinX());
        selectionRect.setY(bounds.getMinY());
        selectionRect.setWidth(bounds.getWidth());
        selectionRect.setHeight(bounds.getHeight());
    }

    private void setSelectionHandlers(LineChartWithMarkers chart) {
        // selection coordinates in a root coordinate space
        DragBounds dragBounds = new DragBounds(Point2D.ZERO, Point2D.ZERO);

        chart.setOnMousePressed(event -> {
            Bounds chartBounds = Nodes.getBoundsInParent(chart, root);
            Point2D pointInScene = new Point2D(
                    event.getX() + chartBounds.getMinX(),
                    event.getY() + chartBounds.getMinY());
            dragBounds.setStart(pointInScene);
            dragBounds.setStop(pointInScene);
            setSelectionRectFromDragBounds(dragBounds);
            selectionRect.setVisible(true);
        });

        chart.setOnMouseDragged(event -> {
            Bounds chartBounds = Nodes.getBoundsInParent(chart, root);
            Point2D pointInScene = new Point2D(
                    event.getX() + chartBounds.getMinX(),
                    event.getY() + chartBounds.getMinY());
            dragBounds.setStop(pointInScene);
            setSelectionRectFromDragBounds(dragBounds);
        });

        chart.setOnMouseReleased(event -> {
            selectionRect.setVisible(false);
            if (selectionRect.getWidth() < 10 || selectionRect.getHeight() < 10) {
                return;
            }

            // convert selection coordinates to chart coordinates
            Bounds chartBounds = Nodes.getBoundsInParent(chart, root);
            double startX = selectionRect.getX() - chartBounds.getMinX();
            double startY = selectionRect.getY() - chartBounds.getMinY();
            double endX = startX + selectionRect.getWidth();
            double endY = startY + selectionRect.getHeight();

            zoomToArea(new Point2D(startX, startY), new Point2D(endX, endY));
            updateChartData();
        });
    }

    private void setScrollHandlers(LineChartWithMarkers chart) {
        chart.setOnScroll((ScrollEvent event) -> {
            Point2D scaleCenter = new Point2D(event.getX(), event.getY());
            boolean scaleY = !event.isControlDown();
            double scaleFactor = 1.1;
            if (event.getDeltaY() > 0) {
                scaleFactor = 1 / scaleFactor;
            }

            zoom(scaleFactor, scaleY ? scaleFactor : 1, scaleCenter);
            scheduleUpdate(() -> {
                Platform.runLater(this::updateChartData);
            }, 300);

            event.consume(); // don't scroll parent pane
        });
    }

    private void scheduleUpdate(Runnable update, long delayMillis) {
        if (updateTask != null) {
            updateTask.cancel(false);
        }
        updateTask = scheduler.schedule(update, delayMillis, TimeUnit.MILLISECONDS);
    }

    private SeriesData getNonEmptySeries(ObservableList<SeriesData> seriesList) {
        return seriesList.stream().filter(s -> !s.series().getData().isEmpty()).findFirst().orElse(null);
    }

    private Data<Number, Number> currentVerticalMarker = null;
    private VBox root;

    /** 
     * Remove vertical marker 
     */
    public void removeVerticalMarker() {
        if (lastLineChart != null && currentVerticalMarker != null) {
            lastLineChart.removeVerticalValueMarker(currentVerticalMarker);
            currentVerticalMarker = null;
        }
    }

    /**
     * Add vertical marker
     * @param x
     */
    public void putVerticalMarker(int x) {
        if (lastLineChart != null) {
            if (currentVerticalMarker != null) {
                lastLineChart.removeVerticalValueMarker(currentVerticalMarker);
            }
            currentVerticalMarker = new Data<>(x, 0);
            lastLineChart.addVerticalValueMarker(currentVerticalMarker);
        }
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

    private Map<String, Integer> semanticMinValues = new HashMap<>();
    private Map<String, Integer> semanticMaxValues = new HashMap<>();

    public Integer getSemanticMinValue() {
        return semanticMinValues.getOrDefault(getSelectedSeriesName(), 0);
    }

    public Integer getSemanticMaxValue() {
        return semanticMaxValues.getOrDefault(getSelectedSeriesName(), 0);
    }

    private int getFloorMin(List<Number> data, String semantic) {
        int min = data.stream().filter(Objects::nonNull).mapToInt(n -> n.intValue()).sorted().skip((int) (data.size() * 0.01)).min().orElse(0);
        semanticMinValues.put(semantic, min);
        int baseMin = (int) Math.pow(10, (int) Math.clamp(Math.log10(Math.abs(min)), 0, 3));
        return baseMin == 1 ? (min >= 0 ? 0 : -10) : Math.floorDiv(min, baseMin) * baseMin;
    }

    private int getCeilMax(List<Number> data, String semantic) {
        int max = data.stream().filter(Objects::nonNull).mapToInt(n -> n.intValue()).sorted().limit((int) (data.size() * 0.99)).max().orElse(0);
        semanticMaxValues.put(semantic, max);
        int baseMax = (int) Math.pow(10, (int) Math.clamp(Math.log10(max), 0, 3));
        return baseMax == 1 ? 10 : Math.ceilDiv(max, baseMax) * baseMax;
    }

    private BooleanProperty createBooleanProperty(SeriesData item) {
        var settingsValue = settings.getSetting(file.getParser().getTemplate().getName() + "." + item.series, "visible");
        boolean initialValue = !item.series().getData().isEmpty() && (settingsValue == null || settingsValue.equals("true"));
        final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "visible", initialValue);
        booleanProperty.addListener((observable, oldValue, newValue) -> {
            item.series().getChart().getData().forEach(series -> {
                if (series.getNode() != null) {
                    settings.saveSetting(file.getParser().getTemplate().getName() + "." + item.series, Map.of("visible", newValue));
                    series.getNode().setVisible(newValue);
                }
            });
        });
        return booleanProperty;
    }

    private void selectLineChart(Series<Number, Number> series, boolean isVisible) {
        LineChart<Number, Number> lineChart = (LineChart) series.getChart();

        lineChart.setVerticalGridLinesVisible(isVisible);
        lineChart.setHorizontalGridLinesVisible(isVisible);
        lineChart.setHorizontalZeroLineVisible(isVisible);
        lineChart.setVerticalZeroLineVisible(isVisible);

        lineChart.getYAxis().setOpacity(isVisible ? 1: 0);
    }

    // Apply color to data series
    private void setStyleForSeries(Series<Number, Number> series, String plotStyle) {
        //String colorString = getColorString(color);
        //System.out.println("Color: " + colorString);
        // Apply style to series
        series.getNode().lookup(".chart-series-line").setStyle(plotStyle);
        //.setStyle("-fx-stroke: " + colorString + ";" + "-fx-stroke-width: 0.6px;");
    }

    private record ZoomRect(Number xMin, Number xMax, Number yMin, Number yMax) {}

    /**
     * Line chart with markers
     */
    private class LineChartWithMarkers extends LineChart<Number, Number> {

        private final ObservableList<Data<Number, Number>> horizontalMarkers;
        private final ObservableList<Data<Number, Number>> verticalMarkers;
        //private final List<Data<Number, Number>> buttonMarkers;
        //private final List<Data<Number, Number>> flagMarkers;

        private final ZoomRect outZoomRect;
        private ZoomRect zoomRect;

        private PlotData plotData;
        private PlotData filteredData;

        //private final List<Data<Number, Number>> subsampleInFullRange;
        
        public LineChartWithMarkers(Axis<Number> xAxis, Axis<Number> yAxis, ZoomRect outZoomRect, PlotData plotData) {
            super(xAxis, yAxis);

            this.outZoomRect = outZoomRect;
            
            horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            //buttonMarkers = new ArrayList<>();// FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            //buttonMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
            //flagMarkers = new ArrayList<>();

            this.plotData = plotData;
        }

        public ZoomRect createZoomRectForArea(Point2D start, Point2D end) {
            // start and end are in chart coordinates
            Node plot = lookup(".chart-plot-background");
            Bounds plotBounds = Nodes.getBoundsInParent(plot, this);
            // translate coordinates to a plot space
            Point2D plotStart = new Point2D(
                    Math.clamp(start.getX() - plotBounds.getMinX(), 0, plotBounds.getWidth()),
                    Math.clamp(start.getY() - plotBounds.getMinY(), 0, plotBounds.getHeight()));
            Point2D plotEnd = new Point2D(
                    Math.clamp(end.getX() - plotBounds.getMinX(), 0, plotBounds.getWidth()),
                    Math.clamp(end.getY() - plotBounds.getMinY(), 0, plotBounds.getHeight()));

            NumberAxis xAxis = (NumberAxis)getXAxis();
            Number xMin = xAxis.getValueForDisplay(plotStart.getX());
            Number xMax = xAxis.getValueForDisplay(plotEnd.getX());

            NumberAxis yAxis = (NumberAxis)getYAxis();
            Number yMax = yAxis.getValueForDisplay(plotStart.getY());
            Number yMin = yAxis.getValueForDisplay(plotEnd.getY());

            return new ZoomRect(xMin, xMax, yMin, yMax);
        }

        public ZoomRect scaleZoomRect(ZoomRect zoomRect, double xScale, double yScale, Point2D scaleCenter) {
            if (zoomRect == null)
                return null;

            // scale center is in chart coordinates
            // translate chart coordinates to plot coordinates
            Node plot = lookup(".chart-plot-background");
            Bounds plotBounds = Nodes.getBoundsInParent(plot, this);
            Point2D plotScaleCenter = null;
            if (scaleCenter != null ) {
                plotScaleCenter = new Point2D(
                        scaleCenter.getX() - plotBounds.getMinX(),
                        scaleCenter.getY() - plotBounds.getMinY());
            }

            // convert center coordinates to x and y ratios [0, 1]
            // when scale center is null zoom at plot center
            double xCenterRatio = plotScaleCenter != null && plotBounds.getWidth() > 1e-9
                    ? Math.clamp(plotScaleCenter.getX() / plotBounds.getWidth(), 0, 1)
                    : 0.5;
            double yCenterRatio = plotScaleCenter != null && plotBounds.getHeight() > 1e-9
                    ? Math.clamp(1 - plotScaleCenter.getY() / plotBounds.getHeight(), 0, 1)
                    : 0.5;

            Range xRange = new Range(zoomRect.xMin, zoomRect.xMax)
                    .scale(xScale, xCenterRatio);
            Range yRange = new Range(zoomRect.yMin, zoomRect.yMax)
                    .scale(yScale, yCenterRatio);

            return new ZoomRect(
                    xRange.getMin(), xRange.getMax(),
                    yRange.getMin(), yRange.getMax());
        }

        public ZoomRect cropZoomRect(ZoomRect zoomRect, ZoomRect cropRect) {
            if (zoomRect == null || cropRect == null)
                return zoomRect;

            return new ZoomRect(
                    Math.max(zoomRect.xMin.doubleValue(), cropRect.xMin.doubleValue()),
                    Math.min(zoomRect.xMax.doubleValue(), cropRect.xMax.doubleValue()),
                    Math.max(zoomRect.yMin.doubleValue(), cropRect.yMin.doubleValue()),
                    Math.min(zoomRect.yMax.doubleValue(), cropRect.yMax.doubleValue()));
        }

        public void setZoomRect(ZoomRect zoomRect) {
            this.zoomRect = zoomRect != outZoomRect
                    ? cropZoomRect(zoomRect, outZoomRect)
                    : zoomRect;
            applyZoom();
        }

        public void resetZoomRect() {
            setZoomRect(outZoomRect);
        }

        private void applyZoom() {
            NumberAxis xAxis = (NumberAxis)getXAxis();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(zoomRect.xMin.doubleValue());
            xAxis.setUpperBound(zoomRect.xMax.doubleValue());

            NumberAxis yAxis = (NumberAxis) getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(zoomRect.yMin.doubleValue());
            yAxis.setUpperBound(zoomRect.yMax.doubleValue());
        }

        private void updateLineChartData() {
            int lowerIndex = Math.clamp(zoomRect.xMin.intValue(), 0, plotData.data().size() - 1);
            int upperIndex = Math.clamp(zoomRect.xMax.intValue(), lowerIndex, plotData.data().size());
                        
            getData().forEach(series -> {
                if (series.getName().contains(FILTERED_SERIES_SUFFIX)) {
                    if (filteredData == null || !filteredData.rendered) {
                        series.getData().clear();
                        if (filteredData == null) {
                            series.getData().add(new Data<Number,Number>(0, 0));
                        }
                    } 
                    if (filteredData != null) {
                        addSeriesDataFiltered(series, filteredData.data, lowerIndex, upperIndex);
                        if (!filteredData.rendered) {
                            filteredData = filteredData.render();
                        }
                    }
                } else {
                    var node = series.getNode().lookup(".chart-series-line");
                    node.setStyle(plotData.getPlotStyle());
                    if (filteredData != null) {
                            node.setStyle(node.getStyle() + "-fx-stroke-dash-array: 1 5 1 5;");
                    }
                    addSeriesDataFiltered(series, plotData.data, lowerIndex, upperIndex);
                }
            });
        }

        private void clearLineChartData() {
            // TODO: better to recreate series because clear is too slow
            if (!plotData.data.isEmpty()) {
                getData().forEach(series -> {
                    //System.out.println("start clear: " + System.currentTimeMillis());
                    series.getData().clear();
                    //System.out.println("complete clear: " + System.currentTimeMillis());
                });
            }
        }

        /**
         * Add horizontal value marker
         * @param marker
         */
        public void addHorizontalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        /**
         * Remove horizontal value marker
         * @param marker
         */
        public void removeHorizontalValueMarker(Data<Number, Number> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            horizontalMarkers.remove(marker);
        }

        public void addVerticalValueMarker(Data<Number, Number> marker) {
            Line line = new Line();
            line.setStroke(Color.RED); // Bright color for white background
            line.setStrokeWidth(1);
            line.setTranslateY(15);

            ImageView imageView = ResourceImageHolder.getImageView("gps32.png");
            imageView.setTranslateY(17);

            addVerticalValueMarker(marker, line, imageView, null);
        }

        /**
         * Add a vertical value marker to the chart.
         *
         * @param marker    the data point to be marked
         * @param line      the line to be used for the marker
         * @param imageView the image to be displayed at the marker
         */
        public void addVerticalValueMarker(Data<Number, Number> marker, Line line, ImageView imageView, Pane flag) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalMarkers.contains(marker)) return;

            VBox markerBox = new VBox();
            markerBox.setAlignment(Pos.TOP_CENTER);
            
            // Создаем контейнер для изображений/флагов
            VBox imageContainer = new VBox();
            imageContainer.setAlignment(Pos.TOP_CENTER);
            
            if (imageView != null) {
                imageContainer.getChildren().add(imageView);
            }

            if (flag != null) {
                imageContainer.getChildren().add(flag);
            }
            
            // Добавляем контейнер с изображениями
            markerBox.getChildren().add(imageContainer);
            
            // Добавляем линию
            markerBox.getChildren().add(line);

            marker.setNode(markerBox);
            getPlotChildren().add(markerBox);

            verticalMarkers.add(marker);
        }

        /**
         * Remove vertical value marker
         * @param marker
         */
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
                VBox markerBox = (VBox) verticalMarker.getNode();
                markerBox.setLayoutX(getXAxis().getDisplayPosition(verticalMarker.getXValue()));
                
                // Получаем контейнер с изображениями (первый элемент)
                VBox imageContainer = (VBox) markerBox.getChildren().get(0);
                double imageHeight = imageContainer.getChildren().stream()
                    .mapToDouble(node -> node.getBoundsInLocal().getHeight())
                    .sum();
                    
                // Получаем линию (второй элемент)
                Line line = (Line) markerBox.getChildren().get(1);
                line.setStartY(imageHeight);
                line.setEndY(getBoundsInLocal().getHeight());
                
                markerBox.setLayoutY(0d);
                markerBox.setMinHeight(getBoundsInLocal().getHeight());
                markerBox.toFront();
            }
        }
    }

    // Create flag marker
    private Pane createFlagMarker(Color color) {
        Line flagPole = new Line(0, 0, 0, 18);
        flagPole.setStroke(Color.BLACK);
        flagPole.setStrokeWidth(0.8);

        Polygon flag = new Polygon();
        flag.getPoints().addAll(0.0, 0.0, 
        15.0, 0.0, 
        10.0, 5.0, 
        15.0, 10.0, 
        0.0, 10.0);

        flag.setFill(color);
        flag.setStroke(Color.BLACK);
        flag.setStrokeWidth(0.8);

        Pane flagMarker = new Pane();
        flagMarker.getChildren().addAll(flagPole, flag);

        flag.setTranslateX(0);
        flag.setTranslateY(-10);

        return flagMarker;
    }

    public void gnssTimeLag(String seriesName, int shift) {
        charts.forEach(chart -> {
            if ((seriesName.equals(chart.plotData.semantic)) && !GeoData.Semantic.LINE.getName().equals(chart.plotData.semantic)) {

                var shiftedList = new ArrayList<Number>();
                var nulls = new ArrayList<Number>(Collections.nCopies(Math.abs(shift), null));
                if (shift > 0) {
                    shiftedList.addAll(chart.plotData.data.subList(shift, chart.plotData.data.size()));
                    shiftedList.addAll(nulls);
                } else {
                    shiftedList.addAll(nulls);
                    shiftedList.addAll(chart.plotData.data.subList(0, chart.plotData.data.size() + shift));
                }

                chart.filteredData = chart.plotData.withData(shiftedList);

                assert shiftedList.size() == file.getTraces().size();

                for (int i = 0; i < file.getGeoData().size(); i++) {
                    file.getGeoData().get(i).setSensorValue(chart.plotData.semantic, shiftedList.get(i));
                }
            }
        });
        Platform.runLater(() -> {
            updateChartData();
        });
    }

    public void lowPassFilter(String seriesName, int value) {
        
        List<Long> timestampList = file.getGeoData().stream()
			.limit(2000)
			.map(GeoCoordinates::getDateTime)
			.map(dt -> dt.toInstant(ZoneOffset.UTC).toEpochMilli())
			.collect(Collectors.toList());

		double samplingRate = FIRFilter.calculateSamplingRate(timestampList);

        //var selectedSeriesName = comboBox.getValue().series.getName();
            
        charts.forEach(chart -> {
            
            if ((seriesName.equals(chart.plotData.semantic)) && !GeoData.Semantic.LINE.getName().equals(chart.plotData.semantic)) {
                //double cutoffFrequency = FIRFilter.findCutoffFrequency(chart.plotData.data, samplingRate);
                double cutoffFrequency = samplingRate / value;

                //System.out.println("Series: " + chart.plotData.semantic);
                //System.out.println("Cutoff frequency: " + cutoffFrequency);
                //System.out.println("Sampling rate: " + samplingRate);

                var filterOrder = value; //21;

                FIRFilter filter = new FIRFilter(filterOrder, cutoffFrequency, samplingRate);

                var shift = filterOrder / 2;    
                var filteredList = filter.filterList(chart.plotData.data).subList(shift, chart.plotData.data.size() + shift);
                //for (int i = 0; i < shift; i++) {
                //    filteredList.add( null);
                //}

                double rmsOriginal = calculateRMS(chart.plotData.data.stream().filter(Objects::nonNull).mapToDouble(v -> v.doubleValue()).toArray());
                double rmsFiltered = calculateRMS(filteredList.stream().filter(Objects::nonNull).mapToDouble(v -> v.doubleValue()).toArray());

                double scale = rmsOriginal / rmsFiltered;

                for (int i = 0; i < filteredList.size(); i++) {
                    if(filteredList.get(i) != null) {
                        filteredList.set(i, (filteredList.get(i).doubleValue() * scale));
                    }
                }

                chart.filteredData = chart.plotData.withData(filteredList);

                assert filteredList.size() == file.getTraces().size();

                for (int i = 0; i < file.getGeoData().size(); i++) {
                    file.getGeoData().get(i).setSensorValue(chart.plotData.semantic, filteredList.get(i));
                }
            }
        });
        Platform.runLater(() -> {
            updateChartData();
        });
    }

    private static double calculateRMS(double[] signal) {
        double sum = 0.0;
        for (double v : signal) {
            sum += v * v;
        }
        return Math.sqrt(sum / signal.length);
    }

    private Map<String, LineChartWithMarkers> getCharts() {
        return charts.stream().collect(Collectors.toMap(c -> c.plotData.semantic, c -> c));
    }

    public void undoFilter(String seriesName) {
        //var selectedSeriesName = comboBox.getValue().series.getName();
        if (GeoData.Semantic.LINE.getName().equals(seriesName)) {
            return;
        }

        log.debug("Undo filter for series: {}", seriesName);

        var chart = getCharts().get(seriesName);

        chart.filteredData = null;
        for (int i = 0; i < file.getGeoData().size(); i++) {
            file.getGeoData().get(i).undoSensorValue(seriesName);
        }

        updateChartData();
    }

    public boolean isSameTemplate(CsvFile selectedFile) {
        return file.isSameTemplate(selectedFile);
    }

    public String getSelectedSeriesName() {
        return comboBox.getValue().series.getName();
    }

    public void clearFlags() {
        for (FoundPlace fp: foundPlaces.keySet()) {
            removeFlag(fp);
        }
        foundPlaces.clear();
    }

    public void removeFlag(FoundPlace fp) {
        if (lastLineChart != null) {
            lastLineChart.removeVerticalValueMarker(foundPlaces.get(fp));
        }
    }
}