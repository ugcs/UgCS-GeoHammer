package com.ugcs.gprvisualizer.gpr;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.FileChangeType;
import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.Broadcast;
import com.ugcs.gprvisualizer.app.SensorLineChart;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.DepthHeight;
import com.ugcs.gprvisualizer.app.auxcontrol.DepthStart;
import com.ugcs.gprvisualizer.app.auxcontrol.RemoveFileButton;
import com.ugcs.gprvisualizer.app.ext.FileManager;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.math.MinMaxAvg;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@Component
public class Model implements InitializingBean {

	public static final int TOP_MARGIN = 50;
	
	private boolean loading = false; 
	
	private MapField field = new MapField();
	private ProfileField profField = new ProfileField(this);
	
	private FileManager fileManager; // = new FileManager();
	private List<SgyFile> undoFiles = null;
	
	
	private Settings settings = new Settings();
	private LeftRulerController leftRulerController = new LeftRulerController(this);
	
	private Set<FileChangeType> changes = new HashSet<>();
	
	private List<BaseObject> auxElements = new ArrayList<>();
	private List<BaseObject> controls = null;
	
	private Rectangle2D.Double bounds;
	private int maxHeightInSamples = 0;
	
	private boolean kmlToFlagAvailable = false;
	
	public Model(FileManager fileManager) {
		Sout.p("create model");
		this.fileManager = fileManager;
	}
	
	public int getTracesCount() {
		return getFileManager().getTraces().size();
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public void setBounds(Rectangle2D.Double bounds) {
		this.bounds = bounds;		
	}
	
	public Rectangle2D.Double getBounds() {
		return bounds;
	}

	public MapField getField() {
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
		for (SgyFile sf : getFileManager().getFiles()) {
			auxElements.addAll(sf.getAuxElements());
			
			Trace lastTrace = sf.getTraces().get(sf.getTraces().size() - 1);
			
			// add remove button
			RemoveFileButton rfb = new RemoveFileButton(
					lastTrace.indexInFile, sf.getOffset(), sf);
			
			auxElements.add(rfb);
			
		}
		
		auxElements.add(new DepthStart(ShapeHolder.topSelection));
		auxElements.add(new DepthHeight(ShapeHolder.botSelection));
		auxElements.add(getLeftRulerController().tb);
	}
	
	public SgyFile getSgyFileByTrace(int i) {
		for (SgyFile fl : getFileManager().getFiles()) {
			Trace lastTrace = fl.getTraces().get(fl.getTraces().size() - 1);
			if (i <= lastTrace.indexInSet) {
				return fl;
			}		
		}
		return null;
	}

	public int getSgyFileIndexByTrace(int i) {
		
		for (int index = 0;
				index < getFileManager().getFiles().size(); index++) {
			SgyFile fl =  getFileManager().getFiles().get(index);
			
			if (i <= fl.getTraces().get(fl.getTraces().size() - 1).indexInSet) {
				
				return index;
			}		
		}
		
		return 0;
	}

	private final VBox chartsContainer = new VBox(); // Charts container

	public VBox getChartsContainer() {
		return chartsContainer;
	}

	
	public ProfileField getVField() {
		return profField;
	}

	public int getMaxHeightInSamples() {
		return maxHeightInSamples;
	}

	public void updateMaxHeightInSamples() {
		
		//set index of traces
		int maxHeight = 0;
		for (int i = 0;
				i < this.getFileManager().getTraces().size(); 
				i++) {
			Trace tr = this.getFileManager().getTraces().get(i);
			maxHeight = Math.max(maxHeight, tr.getNormValues().length);
		}
		
		this.maxHeightInSamples = maxHeight;
		getSettings().maxsamples = maxHeightInSamples;
		
		
		if (getSettings().layer + getSettings().hpage > maxHeightInSamples) {
			
			getSettings().layer = maxHeightInSamples / 4;
			getSettings().hpage = maxHeightInSamples / 4;			
		}
		
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public void updateSgyFileOffsets() {
		int startTraceNum = 0;
		for (SgyFile sgyFile : this.getFileManager().getFiles()) {
			
			sgyFile.getOffset().setStartTrace(startTraceNum);
			startTraceNum += sgyFile.getTraces().size();
			sgyFile.getOffset().setFinishTrace(startTraceNum);
			sgyFile.getOffset().setMaxSamples(maxHeightInSamples);
		}
	}

	public void initField() {
		// center
		MinMaxAvg lonMid = new MinMaxAvg();
		MinMaxAvg latMid = new MinMaxAvg();
		for (Trace trace : this.getFileManager().getTraces()) {
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
			this.getField().setPathCenter(
					new LatLon(latMid.getMid(), lonMid.getMid()));
			this.getField().setSceneCenter(
					new LatLon(latMid.getMid(), lonMid.getMid()));
			
			
			LatLon lt = new LatLon(latMid.getMin(), lonMid.getMin());
			LatLon rb = new LatLon(latMid.getMax(), lonMid.getMax());
			
			this.getField().setPathEdgeLL(lt, rb);
			
			this.getField().adjustZoom(400, 700);
			
		} else {
			Sout.p("GPS coordinates not found");
			this.getField().setPathCenter(null);
			this.getField().setSceneCenter(null);
		}
	}

	public void init() {
		
		this.updateMaxHeightInSamples();
		
		this.updateSgyFileOffsets();
		
		//
		
		
		//
		
		this.updateAuxElements();
	}

	public List<SgyFile> getUndoFiles() {
		return undoFiles;
	}

	public void setUndoFiles(List<SgyFile> undoFiles) {
		this.undoFiles = undoFiles;
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
		
		for (SgyFile file : getFileManager().getFiles()) {
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
	}

	Map<File, SensorLineChart> csvFiles = new HashMap<>();

	/** 
	 * Initialize chart for the given CSV file
	 * @param csvFile CSV file to initialize chart for
	 * @param broadcast Broadcast to notify listeners
	 * @return void
	 */
	public void initChart(File csvFile, Broadcast broadcast) {
		if (getChart(csvFile).isPresent()) {
			return;
		} 
		var sensorLineChart = new SensorLineChart(this, broadcast);
		var plotData = sensorLineChart.generatePlotData(csvFile);
		for (SgyFile file: plotData.keySet()) {
			chartsContainer.getChildren().add(sensorLineChart.createChartWithMultipleYAxes(file, plotData.get(file)));
		}
		csvFiles.put(csvFile, sensorLineChart);
	}

	public void updateChart(File csvFile, Broadcast broadcast) {
		Optional<SensorLineChart> currentChart = getChart(csvFile);
		if (currentChart.isEmpty()) {
			return;
		} 
		
		currentChart.get().close();

		var sensorLineChart = new SensorLineChart(this, broadcast);
		var plotData = sensorLineChart.generatePlotData(csvFile);
		for (SgyFile file: plotData.keySet()) {
			chartsContainer.getChildren().add(sensorLineChart.createChartWithMultipleYAxes(file, plotData.get(file)));
		}
		csvFiles.put(csvFile, sensorLineChart);
	}

	/**
	 * Get chart for the given file if it exists in the model
	 * @param file CSV file to get chart for
	 * @return Optional of SensorLineChart
	 */
    public Optional<SensorLineChart> getChart(File csvFile) {
		csvFiles.forEach((file, chart) -> {
			chart.removeVerticalMarker();
		});
		return Optional.ofNullable(csvFiles.get(csvFile));
    }

	public void chartsZoomOut() {
		csvFiles.forEach((file, chart) -> {
			chart.zoomOut();
		});
	}

	public void closeAllCharts() {
		csvFiles.forEach((file, chart) -> {
			chart.close(false);
		});
		csvFiles.clear();
	}

	public void removeChart(File csvFile) {
		csvFiles.remove(csvFile);
    }

	Map<String, Color> semanticColors = new HashMap<>();

	public Color getColorBySemantic(String semantic) {
		return semanticColors.computeIfAbsent(semantic, k -> generateRandomColor());
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

	public boolean isOnlyCsvLoaded() {
		return fileManager.getFiles().stream()
				.allMatch(SgyFile::isCsvFile);
	}
}