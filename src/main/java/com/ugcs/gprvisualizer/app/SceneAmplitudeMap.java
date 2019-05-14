package com.ugcs.gprvisualizer.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.ugcs.gprvisualizer.gpr.AmpMapDrawer;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;
import com.ugcs.gprvisualizer.gpr.Scan;
import com.ugcs.gprvisualizer.gpr.ScanBuilder;
import com.ugcs.gprvisualizer.gpr.ScreenCoordinatesCalculator;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.ui.BaseCheckBox;
import com.ugcs.gprvisualizer.ui.BaseSlider;
import com.ugcs.gprvisualizer.ui.DepthSlider;
import com.ugcs.gprvisualizer.ui.DepthWindowSlider;
import com.ugcs.gprvisualizer.ui.GainBottomSlider;
import com.ugcs.gprvisualizer.ui.GainTopSlider;
import com.ugcs.gprvisualizer.ui.RadiusSlider;
import com.ugcs.gprvisualizer.ui.ThresholdSlider;
import com.ugcs.gprvisualizer.ui.ZoomSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SceneAmplitudeMap {

	private Model model;
	
	private AmpMapDrawer drawer;
	private BufferedImage img;
	
	private ScreenCoordinatesCalculator coordinator;
	//private BufferedImage spectrumImg;
	private VerticalCut verticalCut;
	
	private TextField bottom = new TextField();
	{
		bottom.setEditable(false);
		bottom.setPrefWidth(240);
	}
	
	private ImageView imageView = new ImageView();
	private ImageView spectrumView = new ImageView();

	private BaseSlider depthSlider;
	private BaseSlider depthWindowSlider;
	private BaseSlider gainTopSlider;
	private BaseSlider gainBottomSlider;
	private BaseSlider thresholdSlider;
	private BaseSlider radiusSlider;
	private BaseSlider zoomSlider;
	private BaseCheckBox autoGainCheckbox;
	
	private Stage verticalCutStage;
	
	public SceneAmplitudeMap(Model model) {
		this.model = model;
		
		coordinator = new ScreenCoordinatesCalculator(model);
		drawer = new AmpMapDrawer(model);
		
		Settings settings = model.getSettings();
		
		depthSlider = new DepthSlider(settings, sliderListener);
		depthWindowSlider = new DepthWindowSlider(settings, sliderListener);
		gainTopSlider = new GainTopSlider(settings, sliderListener);
		gainBottomSlider = new GainBottomSlider(settings, sliderListener);
		thresholdSlider = new ThresholdSlider(settings, sliderListener);
		radiusSlider = new RadiusSlider(settings, sliderListener);
		zoomSlider = new ZoomSlider(settings, recalcSliderListener);
		autoGainCheckbox = new BaseCheckBox(settings, autoGainListener);
		
		
		verticalCut = new VerticalCut(model);
		
		verticalCutStage = new Stage();
		verticalCutStage.setTitle("vertical cut");
        //stage.setScene(new Scene(bPane, 450, 450));
		verticalCutStage.setScene(verticalCut.build());
		
	}

	private EventHandler<DragEvent> dragHandler = new EventHandler<DragEvent>() {

        @Override
        public void handle(DragEvent event) {
            if (//event.getGestureSource() != scrollPane
                  //  && 
                    event.getDragboard().hasFiles()) {

            	
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        }
    };
    
    private EventHandler<DragEvent> dropHandler = new EventHandler<DragEvent>() {

        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                //dropped.setText(db.getFiles().toString());
            	System.out.println(db.getFiles().toString());
            	
            	load(db.getFiles());
            	
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        }
    };
	
	
	
	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {

		@Override
		public void accept(RecalculationLevel level) {

			if(model.getScans() == null) {
				return;
			}
				
			if(level == RecalculationLevel.COORDINATES) {
				coordinator.calcLocalCoordinates();
			}
			
			img = drawer.render(level);
			//spectrumImg = data.getDepthSpectrum().toImg(200, 50);
			
			Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
				    Image i = SwingFXUtils.toFXImage(img, null);
				    imageView.setImage(i);
				    
				    //Image i2 = SwingFXUtils.toFXImage(spectrumImg, null);
				    //spectrumView.setImage(i2);
				    
	            }
	          });      				
			
			//setVizPanel.setImage(new SettingsVisualizer().draw(settings, data));
			//setVizPanel.repaint();
			
			
		}
		
	});

	private ChangeListener<Boolean> autoGainListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			
			gainBottomSlider.updateUI();
			gainTopSlider.updateUI();
			thresholdSlider.updateUI();
			
			controller.render(RecalculationLevel.BUFFERED_IMAGE);
		}
	};
	
	private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			
			controller.render(RecalculationLevel.BUFFERED_IMAGE);
		}
	};
	
	private ChangeListener<Number> recalcSliderListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			controller.render(RecalculationLevel.COORDINATES);
		}
	};
	
	
	
	public Scene build() {
		
		BorderPane bPane = new BorderPane();   
		bPane.setTop(getToolBar()); 
		bPane.setBottom(prepareStatus()); 
		bPane.setRight(getToolPane()); 
		bPane.setCenter(createCentral());
		
		Scene scene = new Scene(bPane, 1024, 768);
		
		return scene;
	}
	
	private Node getToolBar() {
		ToolBar toolBar = new ToolBar();

		Image imageFilter = new Image(getClass().getClassLoader().getResourceAsStream("filter.png"));		
		Button button2 = new Button(null, new ImageView(imageFilter));
		
		button2.setDisable(true);
		button2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        
		    	verticalCutStage.show();
		    }
		});
		
		
		
		Image imageRemove = new Image(getClass().getClassLoader().getResourceAsStream("broom2.png"));
		Button button3 = new Button(null, new ImageView(imageRemove));
		button3.setDisable(true);
		
		toolBar.getItems().addAll(button2, button3);
		
		return toolBar;
	}
	
	private Node getToolPane() {
		VBox vBox = new VBox(); 
		vBox.setPadding(new Insets(3, 13, 3, 3));
		
        HBox root = new HBox();
        root.setStyle("-fx-border-color: black");
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(5));
        root.setSpacing(5);        
        root.getChildren().addAll(spectrumView);
		
		vBox.getChildren().add(root);
		vBox.getChildren().add(depthSlider.produce());
		vBox.getChildren().add(depthWindowSlider.produce());
		vBox.getChildren().add(autoGainCheckbox.produce());
		vBox.getChildren().add(gainTopSlider.produce());
		vBox.getChildren().add(gainBottomSlider.produce());
		vBox.getChildren().add(thresholdSlider.produce());
		vBox.getChildren().add(radiusSlider.produce());
		vBox.getChildren().add(zoomSlider.produce());
		return vBox;
	}
	
	
	private Node createCentral() {
		StackPane centeredPane = new StackPane();
	    centeredPane.setStyle("-fx-border-color: black");
	    centeredPane.setPrefSize(200, 200);
	    
	    centeredPane.getChildren().add(new Text("Drag and drop sgy files here"));
	    centeredPane.getChildren().add(imageView);
	     
	    GridPane outerPane = new GridPane();
	    RowConstraints row = new RowConstraints();
	    row.setPercentHeight(100);
	    row.setFillHeight(false);
	    row.setValignment(VPos.CENTER);
	    outerPane.getRowConstraints().add(row);
	     
	    ColumnConstraints col = new ColumnConstraints();
	    col.setPercentWidth(100);
	    col.setFillWidth(false);
	    col.setHalignment(HPos.CENTER);
	    outerPane.getColumnConstraints().add(col);
	 
	    outerPane.add(centeredPane, 0, 0);		
////		
	    ScrollPane scrollPane = new ScrollPane();
	    
	    scrollPane.setOnDragOver(dragHandler);		
	    scrollPane.setOnDragDropped(dropHandler);
	    
	    scrollPane.setFitToHeight(true);
	    scrollPane.setFitToWidth(true);
		scrollPane.setContent(outerPane);
		scrollPane.setPannable(true);
		
		///////		
		
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
            	System.out.println(event.getX() + " " + event.getY());
            	
            	coordinator.findNearestScan((int)event.getX(), (int)event.getY());
            	
            	updateStatusLine();            	
            	            	
            	controller.render(RecalculationLevel.JUST_AUX_GRAPHICS);
            	
            	if(verticalCutStage.isShowing()) {
            		verticalCut.recalc();
            	}
            }

		});
		
		return scrollPane;
	}
	
	
	private Node prepareStatus() {
		
        HBox root = new HBox();        
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(0, 10, 0 , 10));
        root.setSpacing(5);        
        
		
		Image gpsIcon = new Image(getClass().getClassLoader().getResourceAsStream("gpsicon.png"));		
		new ImageView(gpsIcon);

		root.getChildren().addAll(
			new ImageView(gpsIcon),
			bottom
		);
		return root;
		
	}
	
	private String getStatusLineText() {
		        
        String line=""; 
        if(model.getSettings().selectedScanIndex >= 0) {
        	//line = "scans: " + data.getScans().size();
        
        	Scan scan = model.getScans().get(model.getSettings().selectedScanIndex);
        	if(scan != null) {
        		
        		// 40°02'04.0"W 65°11'00.2"N
        		line+=  dgrToDMS(scan.getLatDgr(), true ) +  " " + dgrToDMS(scan.getLonDgr(), false);
        	}
        	
        }
		return line;
	}
	
	private String dgrToDMS(double dgr, boolean lat) {
		String postfix = "";
	
		if(lat ) {
			postfix = dgr > 0 ? "N" : "S";			
		}else {
			postfix = dgr > 0 ? "E" : "W";
		}
		
		dgr = Math.abs(dgr);
		
		int justdgr = (int)dgr;
		int justmin = (int)( (dgr-justdgr) *60 );
		double justsec = ( dgr - (double)justdgr - (double)justmin / 60.0  ) * 3600;
		
		// 40°02'04.0"S 65°11'00.2"E
		return String.format(Locale.ROOT, "%d°%d'%.3f\"%s", justdgr, justmin, justsec, postfix);
		//return justdgr + "°" + justmin + "'" + justsec + "\"" + postfix;
		
	}

	private void updateStatusLine() {
		bottom.setText(getStatusLineText());
	}


	private void load(List<File> list) {
		
		// todo start progress
		
        SgyLoader loader = new SgyLoader(false);
        
        ScanBuilder scanBuilder = loader.processFileList(list);

        model.setScans(scanBuilder.getScans());
        model.setBounds(scanBuilder.getBounds());        
        model.getSettings().selectedScanIndex = -1;
        model.getSettings().maxsamples = model.getScans().get(0).values.length;
        
        drawer.clear();
        
        depthSlider.updateUI();

        //data.filter();
        //updateStatusLine();
        
        controller.render(RecalculationLevel.COORDINATES);
        
        // todo stop progress
		
	}
	
}
