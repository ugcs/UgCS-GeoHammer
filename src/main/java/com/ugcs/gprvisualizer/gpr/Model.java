package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.ugcs.gprvisualizer.app.*;
import com.ugcs.gprvisualizer.app.auxcontrol.*;
import com.ugcs.gprvisualizer.event.BaseEvent;
import javafx.scene.layout.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.ext.FileManager;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;

@Component
public class Model implements InitializingBean {

	public static final int TOP_MARGIN = 50;
	public static final int CHART_MIN_HEIGHT = 400;

	private boolean loading = false; 
	
	private MapField field = new MapField();
	private ProfileField profField = new ProfileField(this);

	private final FileManager fileManager;

	private final Settings settings = new Settings();

	private final LeftRulerController leftRulerController = new LeftRulerController(this);
	
	private final Set<FileChangeType> changes = new HashSet<>();
	
	private final List<BaseObject> auxElements = new ArrayList<>();

	private List<BaseObject> controls = null;
	
	private Rectangle2D.Double bounds;

	private boolean kmlToFlagAvailable = false;

	private final PrefSettings prefSettings;

	private final AuxElementEditHandler auxEditHandler;

	private final VBox chartsContainer = new VBox(); // Charts container

	Map<CsvFile, SensorLineChart> csvFiles = new HashMap<>();

	private final ApplicationEventPublisher eventPublisher;

	public Model(FileManager fileManager, PrefSettings prefSettings, AuxElementEditHandler auxEditHandler, ApplicationEventPublisher eventPublisher) {
		this.prefSettings = prefSettings;
		this.fileManager = fileManager;
		this.auxEditHandler = auxEditHandler;
		this.eventPublisher = eventPublisher;
	}
	
	public Settings getSettings() {
		return settings;
	}
		
	public void setBounds(Rectangle2D.Double bounds) {
		this.bounds = bounds;		
	}
	
	public Rectangle2D.Double getBounds() {
		return bounds;
	}

	public MapField getMapField() {
		return field;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public Set<FileChangeType> getChanges() {
		return changes;
	}

	public List<BaseObject> getAuxElements() {
		return auxElements;
	}

	public List<BaseObject> getControls() {
		return controls;
	}

	public void setControls(List<BaseObject> controls) {
		this.controls = controls;
	}
	
	public void updateAuxElements() {
		auxElements.clear();
		for (SgyFile sf : getFileManager().getGprFiles()) {
			auxElements.addAll(sf.getAuxElements());
			
			Trace lastTrace = sf.getTraces().get(sf.getTraces().size() - 1);
			
			// add remove button
			RemoveFileButton rfb = new RemoveFileButton(
					lastTrace.getIndexInFile(), sf.getOffset(), sf);
			
			auxElements.add(rfb);
			
		}

        getFileManager().getCsvFiles().forEach(sf -> {
            auxElements.addAll(sf.getAuxElements());
            sf.getAuxElements().forEach(element -> {
                getChart((CsvFile) sf).get().addFlag((FoundPlace) element);
            });
        });
		
		auxElements.add(new DepthStart(ShapeHolder.topSelection, getProfileField()));
		auxElements.add(new DepthHeight(ShapeHolder.botSelection, getProfileField()));
		auxElements.add(getLeftRulerController().tb);
	}
	
	public SgyFile getSgyFileByTrace(int i) {
		for (SgyFile fl : getFileManager().getGprFiles()) {
			Trace lastTrace = fl.getTraces().get(fl.getTraces().size() - 1);
			if (i <= lastTrace.getIndexInSet()) {
				return fl;
			}		
		}
		return null;
	}

	public int getSgyFileIndexByTrace(int i) {
		for (int index = 0;
				index < getFileManager().getGprFiles().size(); index++) {
			SgyFile fl =  getFileManager().getGprFiles().get(index);
			
			if (i <= fl.getTraces().get(fl.getTraces().size() - 1).getIndexInSet()) {
				return index;
			}		
		}
		return 0;
	}

	public VBox getChartsContainer() {
		return chartsContainer;
	}

	public ProfileField getProfileField() {
		return profField;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public void updateSgyFileOffsets() {
		int startTraceNum = 0;
		for (SgyFile sgyFile : this.getFileManager().getGprFiles()) {			
			sgyFile.getOffset().setStartTrace(startTraceNum);
			startTraceNum += sgyFile.getTraces().size();
			sgyFile.getOffset().setFinishTrace(startTraceNum);
			sgyFile.getOffset().setMaxSamples(getProfileField().getMaxHeightInSamples());
		}
	}

	public void initField() {
		// center
		MinMaxAvg lonMid = new MinMaxAvg();
		MinMaxAvg latMid = new MinMaxAvg();
		for (Trace trace : getTraces()) {
			if (trace == null) {
				System.out.println("null trace or ot latlon");
				continue;
			}
			
			if (trace.getLatLon() != null) {
				latMid.put(trace.getLatLon().getLatDgr());
				lonMid.put(trace.getLatLon().getLonDgr());
			}
		}
		
		if (latMid.isNotEmpty()) {
			this.getMapField().setPathCenter(
					new LatLon(latMid.getMid(), lonMid.getMid()));
			this.getMapField().setSceneCenter(
					new LatLon(latMid.getMid(), lonMid.getMid()));
			
			
			LatLon lt = new LatLon(latMid.getMin(), lonMid.getMin());
			LatLon rb = new LatLon(latMid.getMax(), lonMid.getMax());
			
			this.getMapField().setPathEdgeLL(lt, rb);
			
			this.getMapField().adjustZoom(CHART_MIN_HEIGHT, 700);
			
		} else {
			//Sout.p("GPS coordinates not found");
			this.getMapField().setPathCenter(null);
			this.getMapField().setSceneCenter(null);
		}
	}

	public void init() {
		
		this.profField.updateMaxHeightInSamples();
		
		this.updateSgyFileOffsets();
		
		//
		
		
		//
		
		this.updateAuxElements();
	}

	public boolean isActive() {
		return getFileManager().isActive();
	}

	public LeftRulerController getLeftRulerController() {
		return leftRulerController;
	}

	public boolean stopUnsaved() {
    	if (getFileManager().isUnsavedExists()) {
    		
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Warning");
			alert.setContentText("Current files are not saved. Continue?");
			 
			Optional<ButtonType> result = alert.showAndWait();
			 
			if (!result.isPresent() || result.get() != ButtonType.OK) {
				return true;    			
			}
    	}
		return false;
	}

	public boolean isSpreadCoordinatesNecessary() {
		for (SgyFile file : getFileManager().getGprFiles()) {
			if (file.isSpreadCoordinatesNecessary()) {
				return true;
			}
		}
		return false;
	}

	public boolean isKmlToFlagAvailable() {
		return kmlToFlagAvailable;
	}

	public void setKmlToFlagAvailable(boolean kmlToFlagAvailable) {
		this.kmlToFlagAvailable = kmlToFlagAvailable;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		AppContext.model = this;
		loadColorSettings(semanticColors);
	}

	/** 
	 * Initialize chart for the given CSV file
	 * @param csvFile CSV file to initialize chart for
	 * @return void
	 */
	public void initChart(CsvFile csvFile) {
		if (getChart(csvFile).isPresent()) {
			return;
		}
		var sensorLineChart = createSensorLineChart(csvFile);
		saveColorSettings(semanticColors);

		Platform.runLater(() -> {
			selectAndScrollToChart(sensorLineChart);
		});
	}

	public void updateChart(CsvFile csvFile) {
		Optional<SensorLineChart> currentChart = getChart(csvFile);
		if (currentChart.isEmpty()) {
			return;
		} 
		
		currentChart.get().close(false);
		csvFiles.remove(csvFile);

		createSensorLineChart(csvFile);
	}

	private SensorLineChart createSensorLineChart(CsvFile csvFile) {
		var sensorLineChart = new SensorLineChart(this, eventPublisher, prefSettings, auxEditHandler);
		var plotData = sensorLineChart.generatePlotData(csvFile);
		var sensorLineChartBox = sensorLineChart.createChartWithMultipleYAxes(csvFile, plotData);
		chartsContainer.getChildren().add(sensorLineChartBox);
		sensorLineChartBox.getChildren().forEach(node -> {
			if (node instanceof StackPane) {
				((StackPane) node).setPrefHeight(Math.max(CHART_MIN_HEIGHT, node.getScene().getHeight()));
				((StackPane) node).setMinHeight(Math.max(CHART_MIN_HEIGHT, node.getScene().getHeight() / 2));
			}
		});
		csvFiles.put(csvFile, sensorLineChart);
		return sensorLineChart;
	}

	private void saveColorSettings(Map<String, Color> semanticColors) {
		String group = "colors";
		prefSettings.saveSetting(group, semanticColors);
	}

	/**
	 * Get chart for the given file if it exists in the model
	 * @param csvFile CSV file to get chart for
	 * @return Optional of SensorLineChart
	 */
    public Optional<SensorLineChart> getChart(CsvFile csvFile) {
		return Optional.ofNullable(csvFiles.get(csvFile));
    }

	public void chartsClearSelection() {
		csvFiles.forEach((file, chart) -> {
			if (Platform.isFxApplicationThread()) {
				chart.removeVerticalMarker();
			}
		});
	}

	public void chartsZoomOut() {
		csvFiles.forEach((file, chart) -> {
			chart.zoomToFit();
		});
	}

	public void closeAllCharts() {
		csvFiles.forEach((file, chart) -> {
			chart.close(false);
		});
		csvFiles.clear();
	}

	public void removeChart(CsvFile csvFile) {
		csvFiles.remove(csvFile);
    }

	Map<String, Color> semanticColors = new HashMap<>();

	public void loadColorSettings(Map<String, Color> semanticColors) {
		prefSettings.getAllSettings().get("colors")
			.forEach((key, value) -> {
			//if (key.startsWith("colors")) {
				semanticColors.put(key, Color.web(value));
			//}
		});
	}

	public Color getColorBySemantic(String semantic) {
		return semanticColors.computeIfAbsent(semantic, k -> generateRandomColor());
	}

	private List<Color> brightColors = List.of(
		Color.web("#fbd101"),
		//Color.web("#fdff0d"),
		Color.web("#b0903a"),
		Color.web("#f99e01"),
		Color.web("#c56a04"),
		Color.web("#df818e"),
		Color.web("#ff6455"),
		Color.web("#d80b01"),
		Color.web("#b40a13"),
		Color.web("#690d08"),
		Color.web("#989d9b"),
		Color.web("#738768"),
		Color.web("#6bb7e6"),
		Color.web("#496a8b"),
		Color.web("#2b52a3"),
		Color.web("#272f73"),
		Color.web("#5b9a95"),
		Color.web("#add6aa"),
		Color.web("#2b960a"),
		Color.web("#0e5f1e"),
		Color.web("#bfc1c3"),
		Color.web("#cbac7a"),
		Color.web("#80674e"),
		Color.web("#cabf95"),
		Color.web("#7b7b7b"),
		Color.web("#354a32"),
		Color.web("#8c2a07"),
		Color.web("#545a4c"),
		Color.web("#242d29"),
		Color.web("#7b7b7b"));

	Random rand = new Random();

	private Node selectedDataNode;

	// Generate random color
    private Color generateRandomColor() {
        return brightColors.get(rand.nextInt(brightColors.size()));
    }

	/*public boolean isOnlyCsvLoaded() {
		return fileManager.getFiles().stream()
				.allMatch(SgyFile::isCsvFile);
	}*/

	public List<Trace> getGprTraces() {
		return profField.getGprTraces();
	}

	public List<Trace> getCsvTraces() {
		return getFileManager().getCsvTraces();
	}

	public int getCsvTracesCount() {
		return getCsvTraces().size();
	}

	public int getGprTracesCount() {
		return getGprTraces().size();
	}

	public List<Trace> getTraces() {
		List<Trace> result = new ArrayList<>();
		result.addAll(getGprTraces());
		result.addAll(getCsvTraces());
		return result;
	}

	private void setSelectedData(Node node) {
		this.selectedDataNode = node;
	}

    private Node getSelectedData() {
		return selectedDataNode;   
	}

	public boolean selectAndScrollToChart(FileDataContainer fileDataContainer) {
		
		Node node = fileDataContainer.getRootNode();

        if (getSelectedData() != null) {
			if (getSelectedData() == node) {
				return false;
			}
			getChart(null); // clear selection
            getSelectedData().setStyle("-fx-border-width: 2px; -fx-border-color: transparent;");
        }

        node.setStyle("-fx-border-width: 2px; -fx-border-color: lightblue;");
        setSelectedData(node);

		fileDataContainer.selectFile();

		ScrollPane scrollPane = findScrollPane(node);
		if (scrollPane != null) {
			//TODO: implement scroll to chart
        	scrollToChart(scrollPane, node);
		}

		return true;
    }

	private ScrollPane findScrollPane(Node node) {
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                return (ScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

	private void scrollToChart(ScrollPane scrollPane, Node chart) {
        Bounds viewportBounds = scrollPane.getViewportBounds();
        Bounds chartBounds = chart.getBoundsInParent();

        double heightDifference = chartsContainer.getBoundsInParent().getHeight() - viewportBounds.getHeight();

        double vValue = chartBounds.getMinY() / heightDifference;

        scrollPane.setVvalue(vValue < 0 ? Double.POSITIVE_INFINITY : vValue);
    }

	public void createClickPlace(SgyFile file, Trace trace) {
		ClickPlace fp = new ClickPlace(file, trace);
		fp.setSelected(true);
		setControls(Arrays.asList(fp));
	}

    public Collection<SensorLineChart> getCharts() {
		return csvFiles.values();
    }

	public void publishEvent(BaseEvent event) {
		eventPublisher.publishEvent(event);
	}
}