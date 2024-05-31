package com.ugcs.gprvisualizer.app;

import java.util.function.Consumer;

import com.ugcs.gprvisualizer.math.LevelFilter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScan;
import com.ugcs.gprvisualizer.app.commands.AlgorithmicScanFull;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanHP;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.ExpHoughScan;
import com.ugcs.gprvisualizer.math.HoughScan;
import com.ugcs.gprvisualizer.math.TraceStacking;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
public class OptionPane extends VBox implements InitializingBean {
	
	private static final int RIGHT_BOX_WIDTH = 330;
	
	@Autowired
	private MapView mapView;
	
	@Autowired
	private Broadcast broadcast; 
	
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
	
	public OptionPane() {
		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {

		this.setPadding(new Insets(3, 13, 3, 3));
		this.setPrefWidth(RIGHT_BOX_WIDTH);
		this.setMinWidth(0);
		this.setMaxWidth(RIGHT_BOX_WIDTH);

		//this.getChildren().addAll(levelFilter.getToolNodes());

		//this.getChildren().addAll(profileView.getRight());
		
        this.getChildren().addAll(prepareTabPane());
        
	}
	
	private TabPane prepareTabPane() {
		TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tab tab1 = new Tab("GPR");
        Tab tab2 = new Tab("Experimental");
		Tab tab3 = new Tab("CSV");

        tabPane.getTabs().add(tab1);
        
        if (!AppContext.PRODUCTION) {
        	tabPane.getTabs().add(tab2);
        }
        
        prepareTab1(tab1);

        prepareTab2(tab2);

		tabPane.getTabs().add(tab3);
        
		return tabPane;
	}

	private void prepareTab1(Tab tab1) {
		VBox t1 = new VBox();
		t1.getChildren().addAll(levelFilter.getToolNodes());
        t1.getChildren().addAll(profileView.getRight());
		t1.getChildren().addAll(mapView.getRight());
        tab1.setContent(t1);
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
			broadcast.notifyAll(new WhatChanged(Change.justdraw));
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
							broadcast.notifyAll(new WhatChanged(Change.justdraw));
							
							profileView.printHoughSlider.requestFocus();
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
							broadcast.notifyAll(new WhatChanged(Change.justdraw));
							//levelCalculated = true; 
							//updateButtons(); 
						})

				
			);
		
		

//		KeyCombination kc = new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN);
//		Mnemonic mn = new Mnemonic(shEdge, kc); // you can also use kp
//		AppContext.scene.addMnemonic(mn);
		
        tab2.setContent(t2);
	}

}
