package com.ugcs.gprvisualizer.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.event.GriddingParamsSetted;
import com.ugcs.gprvisualizer.event.FileSelectedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.math.LevelFilter;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.controlsfx.control.RangeSlider;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.PositionFile;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PrefSettings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Component
public class OptionPane extends VBox implements InitializingBean {
	
	private static final int RIGHT_BOX_WIDTH = 350;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Autowired
	private MapView mapView;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private UiUtils uiUtils;

	@Autowired
	private ProfileView profileView;
	
	@Autowired
	private CommandRegistry commandRegistry;
	
	@Autowired
	private Model model;

	//@Autowired
	//private RadarMap radarMap;
	
	//@Autowired
	//private HoughScan houghScan;
	
	//@Autowired
	//private ExpHoughScan expHoughScan;

	@Autowired
	private LevelFilter levelFilter;

	@Autowired
	private PrefSettings prefSettings;
	
	private ToggleButton showGreenLineBtn = new ToggleButton("", 
			ResourceImageHolder.getImageView("level.png"));

	private final TabPane tabPane = new TabPane();

	private final Tab gprTab = new Tab("GPR");
	private final Tab csvTab = new Tab("CSV");

	private SgyFile selectedFile;

	private static final String BORDER_STYLING = """
		-fx-border-color: gray; 
		-fx-border-insets: 5;
		-fx-border-width: 1;
		-fx-border-style: solid;
		""";

	private ToggleButton  gridding = new ToggleButton("Gridding");
	private Map<String, TextField> filterInputs = new HashMap<>();
	private ProgressIndicator griddingProgressIndicator;
	private Button showGriddingButton;
	private Button showGriddingAllButton;

	public RangeSlider getGriddingRangeSlider() {
		return griddingRangeSlider;
	}

	private RangeSlider griddingRangeSlider;

	@Override
	public void afterPropertiesSet() throws Exception {

		this.setPadding(new Insets(3, 3, 3, 3));
		this.setPrefWidth(RIGHT_BOX_WIDTH);
		this.setMinWidth(0);
		this.setMaxWidth(RIGHT_BOX_WIDTH);

		prepareTabPane();
        this.getChildren().addAll(tabPane);
	}
	
	private void prepareTabPane() {

		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tab tab2 = new Tab("Experimental");

		prepareCsvTab(csvTab);

        if (!AppContext.PRODUCTION) {
        	tabPane.getTabs().add(tab2);
        }
	}

	private void prepareCsvTab(Tab tab) {
		ToggleButton lowPassFilterButton = new ToggleButton("Low-pass filter");
		ToggleButton timeLagButton = new ToggleButton("GNSS time-lag");
		Button button3 = new Button("Heading error compensation");
		Button button5 = new Button("Quality control");

		lowPassFilterButton.setMaxWidth(Double.MAX_VALUE);
		gridding.setMaxWidth(Double.MAX_VALUE);

		timeLagButton.setMaxWidth(Double.MAX_VALUE);
		button3.setMaxWidth(Double.MAX_VALUE);
		button5.setMaxWidth(Double.MAX_VALUE);

		VBox t3 = new VBox();
		t3.setPadding(new Insets(10,5,5,5));
		t3.setSpacing(5);

		StackPane lowPassOptions = createFilterOptions(Filter.lowpass,"Enter cutoff wavelength (fiducials)",
				i -> {
					int value = Integer.parseInt(i);
					return value != 0 && value < 10000;
				},
				i -> applyLowPassFilter(Integer.parseInt(i)),
				i -> applyLowPassFilterToAll(Integer.parseInt(i))
		);

		StackPane timeLagOptions = createFilterOptions(Filter.timelag,"Enter time-lag (fiducials)",
				i -> {
					int value = Integer.parseInt(i);
					return value < 10000;
				},
				i -> applyGnssTimeLag(Integer.parseInt(i)),
				i -> applyGnssTimeLagToAll(Integer.parseInt(i))
		);

		griddingProgressIndicator = new ProgressIndicator();
		griddingProgressIndicator.setVisible(false);
		griddingProgressIndicator.setManaged(false);
		VBox griddingOptions = createGriddingOptions(griddingProgressIndicator);
		StackPane griddingPane = new StackPane(griddingOptions, griddingProgressIndicator);

		t3.getChildren().addAll(List.of(lowPassFilterButton, lowPassOptions,
				gridding, griddingPane,
				timeLagButton, timeLagOptions,
				button5,
				button3));
		t3.setPrefHeight(500);

		lowPassFilterButton.setOnAction(getChangeVisibleAction(lowPassOptions));
		gridding.setOnAction(getChangeVisibleAction(griddingPane));
		timeLagButton.setOnAction(getChangeVisibleAction(timeLagOptions));

		button3.setOnAction(e -> {
			showHeadingErrorCompensation();
		});
		
		button5.setOnAction(e -> {
			showQualityControl();
		});

		tab.setContent(t3);
	}

	private static @NotNull EventHandler<ActionEvent> getChangeVisibleAction(StackPane filterOptionsStackPane) {
		return e -> {
			filterOptionsStackPane.getChildren()
					.stream().filter(n -> n instanceof VBox).forEach(options -> {
						boolean visible = options.isVisible();
						options.setVisible(!visible);
						options.setManaged(!visible);
					});
		};
	}

	private void setGriddingMinMax() {
		if (!(selectedFile instanceof CsvFile)) {
			return;
		}

		double max = model.getChart((CsvFile) selectedFile).get().getSemanticMaxValue();
		double min = model.getChart((CsvFile) selectedFile).get().getSemanticMinValue();

		griddingRangeSlider.setMin(min);
		griddingRangeSlider.setMax(max);

		double width = max - min;
		if (width > 0.0) {
			griddingRangeSlider.setMajorTickUnit(width / 100);
			griddingRangeSlider.setMinorTickCount((int)(width / 1000));
			griddingRangeSlider.setBlockIncrement(width / 2000);
		}

		griddingRangeSlider.setLowValue(min);
		griddingRangeSlider.setHighValue(max);

		griddingRangeSlider.setDisable(false);
	}

	private enum Filter {
		lowpass, timelag, gridding_cellsize, gridding_blankingdistance
	}

	private VBox createGriddingOptions(ProgressIndicator progressIndicator) {
		VBox griddingOptions = new VBox(5);
		griddingOptions.setPadding(new Insets(10, 0, 10, 0));

		VBox filterInput = new VBox(5);

		TextField gridCellSize = new TextField();
		gridCellSize.setPromptText("Enter cell size");
		filterInputs.put(Filter.gridding_cellsize.name(), gridCellSize);

		TextField gridBlankingDistance = new TextField();
		gridBlankingDistance.setPromptText("Enter blanking distance");
		filterInputs.put(Filter.gridding_blankingdistance.name(), gridBlankingDistance);

		showGriddingButton = new Button("Apply");
		showGriddingButton.setOnAction(e -> {
			prefSettings.saveSetting(Filter.gridding_cellsize.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), gridCellSize.getText()));
			prefSettings.saveSetting(Filter.gridding_blankingdistance.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), gridBlankingDistance.getText()));

			Platform.runLater(() -> setGriddingMinMax()); // min, max, min, max)); // minValue, maxValue));

			eventPublisher.publishEvent(new GriddingParamsSetted(this, Double.parseDouble(gridCellSize.getText()),
					Double.parseDouble(gridBlankingDistance.getText())));
			//griddingRangeSlider.setDisable(true);
		});
		showGriddingButton.setDisable(true);

		showGriddingAllButton = new Button("Apply to all");
		showGriddingAllButton.setOnAction(e -> {
			prefSettings.saveSetting(Filter.gridding_cellsize.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), gridCellSize.getText()));
			prefSettings.saveSetting(Filter.gridding_blankingdistance.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), gridBlankingDistance.getText()));

			//Platform.runLater(() -> setGriddingMinMax()); // min, max, min, max)); // minValue, maxValue));

			eventPublisher.publishEvent(new GriddingParamsSetted(this, Double.parseDouble(gridCellSize.getText()),
					Double.parseDouble(gridBlankingDistance.getText()), true));
		});
		showGriddingAllButton.setDisable(true);

		gridCellSize.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (newValue == null) {
					showGriddingButton.setDisable(true);
					showGriddingAllButton.setDisable(true);
					return;
				}
				double value = Double.parseDouble(newValue);
				boolean isValid = !newValue.isEmpty() && value > 0 && value < 100;
				showGriddingButton.setDisable(!isValid || gridBlankingDistance.getText().isEmpty());
				showGriddingAllButton.setDisable(!isValid || gridBlankingDistance.getText().isEmpty());
			} catch (NumberFormatException e) {
				showGriddingButton.setDisable(true);
				showGriddingAllButton.setDisable(true);
			}
		});

		gridBlankingDistance.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (newValue == null) {
					showGriddingButton.setDisable(true);
					showGriddingAllButton.setDisable(true);
					return;
				}
				double value = Double.parseDouble(newValue);
				boolean isValid = !newValue.isEmpty() && value > 0 && value < 100;
				showGriddingButton.setDisable(!isValid || gridCellSize.getText().isEmpty());
				showGriddingAllButton.setDisable(!isValid || gridCellSize.getText().isEmpty());
			} catch (NumberFormatException e) {
				showGriddingButton.setDisable(true);
				showGriddingAllButton.setDisable(true);
			}
		});

		Label label = new Label("Range");

		HBox coloursInput = new HBox(5);

		griddingRangeSlider = new RangeSlider();
		griddingRangeSlider.setShowTickLabels(true);
		griddingRangeSlider.setShowTickMarks(true);
		griddingRangeSlider.setLowValue(0);
		griddingRangeSlider.setHighValue(Double.MAX_VALUE);
		griddingRangeSlider.setDisable(true);
		griddingRangeSlider.setShowTickLabels(false);
		griddingRangeSlider.setShowTickMarks(false);

		Label minLabel = new Label("Min"); //+ griddingRangeSlider.getLowValue());
		Label maxLabel = new Label("Max"); //+ griddingRangeSlider.getHighValue());
		HBox center = new HBox(5);
		HBox.setHgrow(center, Priority.ALWAYS);
		coloursInput.getChildren().addAll(minLabel, center, maxLabel);

		griddingRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
				minLabel.setText("Min: " + newVal.intValue());//String.format("%.2f", newVal.doubleValue()));
				eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		});

		griddingRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> {
			maxLabel.setText("Max: " + newVal.intValue());//String.format("%.2f", newVal.doubleValue()));
			eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		});

		VBox vbox = new VBox(10, griddingRangeSlider, coloursInput);

		filterInput.getChildren().addAll(gridCellSize, gridBlankingDistance, label, vbox);

		HBox filterButtons = new HBox(5);
		HBox rightBox = new HBox();
		HBox leftBox = new HBox(5);
		leftBox.getChildren().addAll(showGriddingButton);
		HBox.setHgrow(leftBox, Priority.ALWAYS);
		rightBox.getChildren().addAll(showGriddingAllButton);

		filterButtons.getChildren().addAll(leftBox, rightBox);

		griddingOptions.getChildren().addAll(filterInput, filterButtons);
		griddingOptions.setVisible(false);
		griddingOptions.setManaged(false);

		return griddingOptions;
	}

	public void griddingProgress(boolean inProgress) {
		showGriddingButton.setDisable(inProgress);
		showGriddingAllButton.setDisable(inProgress);
		griddingProgressIndicator.setVisible(inProgress);
		griddingProgressIndicator.setManaged(inProgress);
	}

	private @NotNull StackPane createFilterOptions(Filter filter, String promptText, Predicate<String> valueConstraint,
												   Consumer<String> applyAction, Consumer<String> applyAllAction) {
		VBox filterOptions = new VBox(5);
		filterOptions.setPadding(new Insets(10, 0, 10, 0));

		TextField filterInput = new TextField();
		filterInputs.put(filter.name(), filterInput);
		filterInput.setPromptText(promptText);

		Button applyButton = new Button("Apply");
		applyButton.setDisable(true);
		filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (newValue == null) {
					applyButton.setDisable(true);
					return;
				}
				boolean isValid = !newValue.isEmpty()
						&& (valueConstraint == null || valueConstraint.test(newValue));
				applyButton.setDisable(!isValid);
			} catch (NumberFormatException e) {
				applyButton.setDisable(true);
			}
		});

		Button applyAllButton = new Button("Apply to all");
		applyAllButton.setDisable(true);

		Button undoButton = new Button("Undo");
		undoButton.setDisable(true);

		undoButton.setOnAction(e -> {
			var chart = model.getChart((CsvFile) selectedFile);
			chart.ifPresent(c -> c.undoFilter(c.getSelectedSeriesName()));
			undoButton.setDisable(true);
			applyAllButton.setDisable(true);
		});

		ProgressIndicator progressIndicator = new ProgressIndicator();

		applyButton.setOnAction(e -> {
			progressIndicator.setVisible(true);
			progressIndicator.setManaged(true);

			filterInput.setDisable(true);
			applyButton.setDisable(true);
			undoButton.setDisable(true);
			applyAllButton.setDisable(true);

			executor.submit(() -> {
				prefSettings.saveSetting(filter.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), filterInput.getText()));
				applyAction.accept(filterInput.getText());

				filterInput.setDisable(false);
				applyButton.setDisable(false);
				undoButton.setDisable(false);
				applyAllButton.setDisable(false);

				progressIndicator.setVisible(false);
				progressIndicator.setManaged(false);
			});
		});

		applyAllButton.setOnAction(e -> {
			progressIndicator.setVisible(true);
			progressIndicator.setManaged(true);

			filterInput.setDisable(true);
			applyButton.setDisable(true);
			undoButton.setDisable(true);
			applyAllButton.setDisable(true);

			executor.submit(() -> {
				prefSettings.saveSetting(filter.name(), Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), filterInput.getText()));
				applyAllAction.accept(filterInput.getText());

				filterInput.setDisable(false);
				applyButton.setDisable(false);
				undoButton.setDisable(false);

				progressIndicator.setVisible(false);
				progressIndicator.setManaged(false);

			});
		});

		HBox filterButtons = new HBox(5);
		HBox rightBox = new HBox();
		HBox leftBox = new HBox(5);
		leftBox.getChildren().addAll(applyButton, undoButton);
		HBox.setHgrow(leftBox, Priority.ALWAYS);
		rightBox.getChildren().addAll(applyAllButton);

		filterButtons.getChildren().addAll(leftBox, rightBox);

		filterOptions.getChildren().addAll(filterInput, filterButtons);
		filterOptions.setVisible(false);
		filterOptions.setManaged(false);

		progressIndicator.setVisible(false);
		progressIndicator.setManaged(false);
		return new StackPane(filterOptions, progressIndicator);
	}

	private void getNoImplementedDialog() {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Not Implemented");
		dialog.setHeaderText("Feature Not Implemented");
		dialog.setContentText("This feature is not yet implemented.");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		dialog.showAndWait();
	}

	private void showQualityControl() {
		getNoImplementedDialog();
	}

	private void showHeadingErrorCompensation() {
		getNoImplementedDialog();
	}

	private void applyGnssTimeLag(int value) {
		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(c -> c.gnssTimeLag(c.getSelectedSeriesName(), value));
		eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.csvDataFiltered));
	}

	private void applyGnssTimeLagToAll(int value) {
		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(sc -> {
			String seriesName = sc.getSelectedSeriesName();
			model.getCharts().stream()
					.filter(c -> c != sc && c.isSameTemplate((CsvFile) selectedFile))
					.forEach(c -> c.gnssTimeLag(seriesName, value));
		});
		eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.csvDataFiltered));
	}

	private void applyLowPassFilter(int value) {
		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(c -> c.lowPassFilter(c.getSelectedSeriesName(), value));
		eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.csvDataFiltered));
	}

	private void applyLowPassFilterToAll(int value) {
		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(sc -> {
			String seriesName = sc.getSelectedSeriesName();
			model.getCharts().stream()
					.filter(c -> c != sc && c.isSameTemplate((CsvFile) selectedFile))
					.forEach(c -> c.lowPassFilter(seriesName, value));
		});
		eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.csvDataFiltered));
	}

	private Tab prepareGprTab(Tab tab1, SgyFile file) {
		VBox t1 = new VBox();

		//contrast
		t1.getChildren().addAll(profileView.getRight(file));
		// buttons
		t1.getChildren().addAll(levelFilter.getToolNodes());
		// map
		t1.getChildren().addAll(mapView.getRight(file));

		t1.getChildren().addAll(getPositionCsvButtons(file.getGroundProfileSource()));
        tab1.setContent(t1);
		return tab1;
	}

	private List<Node> getPositionCsvButtons(PositionFile positionFile) {
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10, 10, 10, 10));
		
		vBox.setStyle(BORDER_STYLING);
				
		vBox.getChildren().add(new Label("Elevation source: " + getSourceName(positionFile)));

		vBox.getChildren().addAll(levelFilter.getToolNodes2());

		return List.of(vBox);	
	}

	private String getSourceName(PositionFile positionFile) {
		return (positionFile != null && positionFile.getPositionFile() != null) ? positionFile.getPositionFile().getName() : "not found";
	}

	private ToggleButton prepareToggleButton(String title, 
			String imageName, MutableBoolean bool, Consumer<ToggleButton> consumer ) {
		
		ToggleButton btn = new ToggleButton(title, 
				ResourceImageHolder.getImageView(imageName));
		
		btn.setSelected(bool.booleanValue());
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				bool.setValue(btn.isSelected());
				
				//eventPublisher.publishEvent(new WhatChanged(change));
				
				consumer.accept(btn);
			}
		});
		
		return btn;
	}

	@EventListener
    private void handleFileSelectedEvent(FileSelectedEvent event) {
        selectedFile = event.getFile();
		if (selectedFile == null) {return;}
        if (selectedFile instanceof CsvFile) {
            showTab(csvTab);
            setGriddingMinMax();
            setSavedFilterInputValue(Filter.lowpass);
            setSavedFilterInputValue(Filter.timelag);
            setSavedFilterInputValue(Filter.gridding_cellsize);
            setSavedFilterInputValue(Filter.gridding_blankingdistance);
        } else {
            showTab(gprTab);
            prepareGprTab(gprTab, selectedFile);
        }
    }

    //@EventListener(condition = "#event.isFileopened()")
	@EventListener
    private void fileOpened(FileOpenedEvent event) {
            clear();
    }

	private void setSavedFilterInputValue(Filter filter) {
		var savedValue = prefSettings.getSetting(filter.name(), ((CsvFile) selectedFile).getParser().getTemplate().getName());
		filterInputs.get(filter.name()).setText(savedValue);
	}

	private void clear() {
		tabPane.getTabs().clear();
	}

	private void showTab(Tab tab) {
        if (!tabPane.getTabs().contains(tab)) {
			clear();
            tabPane.getTabs().add(tab);
        }
        tabPane.getSelectionModel().select(tab);
    }

	public ToggleButton getGridding() {
		return gridding;
	}

	public void handleGriddingParamsSetted(double cellSize, double blankingDistance) {
		eventPublisher.publishEvent(new GriddingParamsSetted(this, cellSize, blankingDistance));
	}
}