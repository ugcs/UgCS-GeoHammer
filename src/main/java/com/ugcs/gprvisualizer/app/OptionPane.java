package com.ugcs.gprvisualizer.app;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ugcs.gprvisualizer.math.LevelFilter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.PositionFile;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScanFull;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.app.fir.FIRFilter;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PrefSettings;
import com.ugcs.gprvisualizer.math.ExpHoughScan;
import com.ugcs.gprvisualizer.math.HoughScan;
import com.ugcs.gprvisualizer.math.TraceStacking;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Component
public class OptionPane extends VBox implements SmthChangeListener, InitializingBean {
	
	private static final int RIGHT_BOX_WIDTH = 350;
	
	@Autowired
	private MapView mapView;
	
	//@Autowired
	//private Broadcast broadcast; 
	
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

	private TextField filterInput = new TextField();
		
	@Override
	public void afterPropertiesSet() throws Exception {

		this.setPadding(new Insets(3, 3, 3, 3));
		this.setPrefWidth(RIGHT_BOX_WIDTH);
		this.setMinWidth(0);
		this.setMaxWidth(RIGHT_BOX_WIDTH);

		//this.getChildren().addAll(levelFilter.getToolNodes());

		//this.getChildren().addAll(profileView.getRight());

		prepareTabPane();
		
        this.getChildren().addAll(tabPane);

	}
	
	private void prepareTabPane() {

		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tab tab2 = new Tab("Experimental");

		prepareCsvTab(csvTab);

        //tabPane.getTabs().add(gprTab);
        
        if (!AppContext.PRODUCTION) {
        	tabPane.getTabs().add(tab2);
        }
        
        //prepareGprTab(gprTab);

        //prepareTab2(tab2);

		//tabPane.getTabs().add(csvTab);        
	}

	private void prepareCsvTab(Tab tab) {
		ToggleButton lowPassFilterButton = new ToggleButton("Low-pass filter");
		Button button2 = new Button("GNSS time-lag");
		Button button3 = new Button("Heading error compensation");
		Button button4 = new Button("Gridding");
		Button button5 = new Button("Quality control");

		lowPassFilterButton.setMaxWidth(Double.MAX_VALUE);
		button2.setMaxWidth(Double.MAX_VALUE);
		button3.setMaxWidth(Double.MAX_VALUE);
		button4.setMaxWidth(Double.MAX_VALUE);
		button5.setMaxWidth(Double.MAX_VALUE);

		VBox t3 = new VBox();
		t3.setPadding(new Insets(10,5,5,5));
		t3.setSpacing(5);

		VBox filterOptions = new VBox(5);
        filterOptions.setPadding(new Insets(10, 0, 10, 0));
		filterInput.setPromptText("Enter cutoff wavelength (fiducials)");
		Button applyButton = new Button("Apply");
		applyButton.setDisable(true);
		filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (newValue == null) {
					applyButton.setDisable(true);
					return;
				}
				int value = Integer.parseInt(newValue);
				boolean isValid = !newValue.isEmpty() && value > 1 && value < 10000;
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

		applyButton.setOnAction(e -> {
			applyLowPassFilter(Integer.parseInt(filterInput.getText()));
			undoButton.setDisable(false);
			applyAllButton.setDisable(false);
		});

		applyAllButton.setOnAction(e -> {
			applyLowPassFilterToAll(Integer.parseInt(filterInput.getText()));
			undoButton.setDisable(false);
			applyAllButton.setDisable(true);
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

		t3.getChildren().addAll(List.of(lowPassFilterButton, filterOptions, button2, button3, button4, button5));
		t3.setPrefHeight(500);

		lowPassFilterButton.setOnAction(e -> {
			boolean visible = filterOptions.isVisible();
            filterOptions.setVisible(!visible);
			filterOptions.setManaged(!visible);
		});
		
		button2.setOnAction(e -> {
			showGnssTimeLag();
		});
		
		button3.setOnAction(e -> {
			showHeadingErrorCompensation();
		});
		
		button4.setOnAction(e -> {
			showGridding();
		});
		
		button5.setOnAction(e -> {
			showQualityControl();
		});

		tab.setContent(t3);
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

	private void showGridding() {
		getNoImplementedDialog();
	}

	private void showHeadingErrorCompensation() {
		getNoImplementedDialog();
	}

	private void showGnssTimeLag() {
		getNoImplementedDialog();
	}

	private void applyLowPassFilter(int value) {
		prefSettings.saveSetting("lowpass", Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), value));

		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(c -> c.lowPassFilter(c.getSelectedSeriesName(), value));
	}

	private void applyLowPassFilterToAll(int value) {
		prefSettings.saveSetting("lowpass", Map.of(((CsvFile) selectedFile).getParser().getTemplate().getName(), value));

		var chart = model.getChart((CsvFile) selectedFile);
		chart.ifPresent(sc -> {
			String seriesName = sc.getSelectedSeriesName();
			model.getCharts().stream()
					.filter(c -> c != sc && c.isSameTemplate((CsvFile) selectedFile))
					.forEach(c -> c.lowPassFilter(seriesName, value));
		});
	}

	private Tab prepareGprTab(Tab tab1, SgyFile file) {
		VBox t1 = new VBox();
		//contrast
		t1.getChildren().addAll(profileView.getRight());
		// buttons
		t1.getChildren().addAll(levelFilter.getToolNodes());
		// map
		t1.getChildren().addAll(mapView.getRight());

		t1.getChildren().addAll(getPositionCsvButtons(file.getGroundProfileSource()));
        tab1.setContent(t1);
		return tab1;
	}

	private List<Node> getPositionCsvButtons(PositionFile positionFile) {
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10, 10, 10, 10));
		
		vBox.setStyle(BORDER_STYLING);
				
		vBox.getChildren().addAll(
			List.of(new Label("Elevation source: " + getSourceName(positionFile))));

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
				
				//broadcast.notifyAll(new WhatChanged(change));
				
				consumer.accept(btn);
			}
		});
		
		return btn;
	}
	
	private void prepareTab2(Tab tab2) {
		
		showGreenLineBtn.setTooltip(new Tooltip("Show/hide anomaly probability chart"));
		showGreenLineBtn.setSelected(model.getSettings().showGreenLine);
		showGreenLineBtn.setOnAction(e -> {
			model.getSettings().showGreenLine = showGreenLineBtn.isSelected();
			//broadcast.notifyAll(new WhatChanged(Change.justdraw));
		});
		
		
		VBox t2 = new VBox(10);		
        t2.getChildren().addAll(profileView.getRightSearch());
        
		ToggleButton shEdge;
		t2.getChildren().addAll(
				//new HBox(
						
				//	commandRegistry.createAsinqTaskButton(
				//		expHoughScan, 
				//		e -> radarMap.selectAlgMode()						
				//	)

				//	,
				//	commandRegistry.createAsinqTaskButton(
				//		houghScan,
				//		e -> radarMap.selectAlgMode()
				//	)
				//),

				//new HBox(
				//		commandRegistry.createAsinqTaskButton(
				//		new AlgorithmicScanFull(),
				//		e -> radarMap.selectAlgMode()
				//	),
				//	commandRegistry.createAsinqTaskButton(
				//			new PluginRunner(model),
				//			e -> {}
				//	)
				//),
				
				//commandRegistry.createAsinqTaskButton(
				//		new AlgorithmicScan()),				
				
				new HBox(
					prepareToggleButton("Hyperbola detection mode", 
						"hypLive.png", 
						model.getSettings().getHyperliveview(),
						e -> {
							//broadcast.notifyAll(new WhatChanged(Change.justdraw));
							
							profileView.getPrintHoughSlider().requestFocus();
						}),
					showGreenLineBtn),
				
				
				
				//new HBox(
				//		commandRegistry.createButton(
				//				new EdgeFinder()),
				//		commandRegistry.createButton(
				//				new EdgeSubtractGround())
				//		),
				new HBox(
						shEdge = 
							uiUtils.prepareToggleButton("show edge", null, 
								model.getSettings().showEdge, 
								Change.justdraw),
						
							uiUtils.prepareToggleButton("show good", null, 
								model.getSettings().showGood, 
								Change.justdraw)
						),
				commandRegistry.createButton(new TraceStacking()), 
				commandRegistry.createButton(new LevelScanHP(), 
						e -> {
							//broadcast.notifyAll(new WhatChanged(Change.justdraw));
							//levelCalculated = true; 
							//updateButtons(); 
						})

				
			);
		
		

//		KeyCombination kc = new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN);
//		Mnemonic mn = new Mnemonic(shEdge, kc); // you can also use kp
//		AppContext.scene.addMnemonic(mn);
		
        tab2.setContent(t2);
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if (changed.isFileopened()) {
			
			//if (gprTab.getContent() instanceof VBox) {
			//	((VBox)gprTab.getContent()).getChildren().forEach(node -> {
			//		node.setDisable(!model.isActive() && !model.getFileManager().getGprFiles().isEmpty());
			//	});
			//}

			if (model.isActive()) {
				if (!model.getFileManager().getGprFiles().isEmpty()) {
					showTab(prepareGprTab(gprTab, model.getFileManager().getGprFiles().get(0)));
				} else {
					showTab(csvTab);
				}
			} else {
				clear();
			}

		}

		if (changed.isFileSelected()) {
			SgyFile file = ((FileSelected) changed).getSelectedFile();
			this.selectedFile = file;

			if (file instanceof CsvFile) {
				var lowpassvalue = prefSettings.getSetting("lowpass", ((CsvFile) selectedFile).getParser().getTemplate().getName());
				filterInput.setText(lowpassvalue);
				showTab(csvTab);
			} else {
				if (file == null) {
					clear();
				} else {
					showTab(prepareGprTab(gprTab, file));
				}	
			}
		}
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


}
