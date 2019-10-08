package com.ugcs.gprvisualizer.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.text.StyleContext.SmallAttributeSet;

import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.draw.SatelliteMap;
import com.ugcs.gprvisualizer.draw.SatelliteMap2;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.draw.Work;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.PaletteBuilder;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.RecalculationLevel;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LayersWindowBuilder extends Work{
	
	private ImageView imageView = new ImageView();
	private BufferedImage img;
	private int[] palette = new PaletteBuilder().build();
	private Stage stage;
	
	public LayersWindowBuilder(Model model) {
		super(model);
		
		
		stage = new Stage();
		stage.setTitle("layers");
		stage.setScene(build());
		
		
		
		getLayers().add(new SatelliteMap(model, listener));
		//getLayers().add(new SatelliteMap2(model, listener));
		//getLayers().add(new SatelliteMap2(model, listener));
		getLayers().add(new RadarMap(model, listener));
		getLayers().add(new GpsTrack(model, listener));
		// layers.add(new AuxControl());
		
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void recalc() {
		//controller.render(null);
	}
	
	
	
		
	public Scene build() {
		
		BorderPane bPane = new BorderPane();   
		bPane.setCenter(imageView);
		
		bPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	System.out.println("mouse click");
            	WhatChanged sc = new WhatChanged();
            	sc.setFileopened(true);
				somethingChanged(sc);            	
            }
		});
		
		
		Scene scene = new Scene(bPane, 1024, 768);
		
		return scene;
	}
	
	protected void repaintEvent() {
		System.out.println("repaintEvent()");
		img = draw();
		
		updateWindow();
	}

	
//	private RecalculationController controller = new RecalculationController(new Consumer<RecalculationLevel>() {
//
//		@Override
//		public void accept(RecalculationLevel obj) {
//
//			img = render();
//			
//			updateWindow();			
//		}
//
//	});

	private void updateWindow() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
			    Image i = SwingFXUtils.toFXImage(img, null);
			    imageView.setImage(i);
            }
          });
	}


}
