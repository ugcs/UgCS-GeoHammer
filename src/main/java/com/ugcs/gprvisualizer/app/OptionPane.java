package com.ugcs.gprvisualizer.app;

import java.util.List;
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
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.ExpHoughScan;
import com.ugcs.gprvisualizer.math.HoughScan;
import com.ugcs.gprvisualizer.math.TraceStacking;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class OptionPane extends VBox implements SmthChangeListener, InitializingBean {
	
	private static final int RIGHT_BOX_WIDTH = 330;
	
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

	@Autowired
	private RadarMap radarMap;
	
	@Autowired
	private HoughScan houghScan;
	
	@Autowired
	private ExpHoughScan expHoughScan;

	@Autowired
	private LevelFilter levelFilter;
	
	private ToggleButton showGreenLineBtn = new ToggleButton("", 
			ResourceImageHolder.getImageView("level.png"));

	private final TabPane tabPane = new TabPane();

	private final Tab gprTab = new Tab("GPR");
	private final Tab csvTab = new Tab("CSV");

	private static final String BORDER_STYLING = """
		-fx-border-color: gray; 
		-fx-border-insets: 5;
		-fx-border-width: 1;
		-fx-border-style: solid;
		""";
		
	@Override
	public void afterPropertiesSet() throws Exception {

		this.setPadding(new Insets(3, 13, 3, 3));
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
		Button button1 = new Button("Low-pass filter");
		HBox button1Box = new HBox(button1);
		

		Button button2 = new Button("GNSS time-lag");
		Button button3 = new Button("Heading error compensation");
		Button button4 = new Button("Gridding");
		Button button5 = new Button("Quality control");

		VBox t3 = new VBox();
		t3.getChildren().add(button1Box);
		t3.setPadding(new Insets(10,5,5,5));
		t3.setSpacing(5);
		t3.getChildren().addAll(List.of(button1, button2, button3, button4, button5));
		button1.setOnAction(e -> {
			showLowPassFilter();
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

		csvTab.setContent(t3);

        //tabPane.getTabs().add(gprTab);
        
        if (!AppContext.PRODUCTION) {
        	tabPane.getTabs().add(tab2);
        }
        
        prepareTab1(gprTab);

        prepareTab2(tab2);

		//tabPane.getTabs().add(csvTab);        
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

	private void showLowPassFilter() {
		getNoImplementedDialog();
	}

	private void prepareTab1(Tab tab1) {
		VBox t1 = new VBox();
		//contrast
		t1.getChildren().addAll(profileView.getRight());
		// buttons
		t1.getChildren().addAll(levelFilter.getToolNodes());
		// map
		t1.getChildren().addAll(mapView.getRight());

		t1.getChildren().addAll(getPositionCsvButtons(null));
        tab1.setContent(t1);
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
				new HBox(
						
					commandRegistry.createAsinqTaskButton(
						expHoughScan, 
						e -> radarMap.selectAlgMode()						
					)
				//	,
				//	commandRegistry.createAsinqTaskButton(
				//		houghScan,
				//		e -> radarMap.selectAlgMode()
				//	)
				),
				new HBox(
						commandRegistry.createAsinqTaskButton(
						new AlgorithmicScanFull(),
						e -> radarMap.selectAlgMode()
					),
					commandRegistry.createAsinqTaskButton(
							new PluginRunner(model),
							e -> {}
					)
				),

				
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
			if (gprTab.getContent() instanceof VBox) {
				((VBox)gprTab.getContent()).getChildren().forEach(node -> {
					node.setDisable(!model.isActive() && !model.getFileManager().getGprFiles().isEmpty());
				});
			}

			if (model.isActive()) {
				if (!model.getFileManager().getGprFiles().isEmpty()) {
					showTab(gprTab);
				} else {
					showTab(csvTab);
				}
			} else {
				clear();
			}

		}

		if (changed.isFileSelected()) {
			SgyFile file = ((FileSelected) changed).getSelectedFile();
			if (file instanceof CsvFile) {
				showTab(csvTab);
			} else {
				if (file == null) {
					clear();
				} else {
					showTab(gprTab);
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
