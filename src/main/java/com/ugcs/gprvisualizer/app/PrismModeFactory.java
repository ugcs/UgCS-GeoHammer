package com.ugcs.gprvisualizer.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.AmplitudeMatrix;
import com.github.thecoldwine.sigrun.common.ext.PrismImageProducer;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

public class PrismModeFactory implements ModeFactory, SmthChangeListener {

	private Model model;
	private ImageView imageView = new ImageView();	
	private int scale = 1;
	
	private BufferedImage img;
	private Button initBtn = new Button("Init");
	private SmthChangeListener listener;
	
	public PrismModeFactory(Model model, SmthChangeListener listener){
		this.model = model;		
		this.listener = listener;
		
		initBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {

		    	updateImage();
		    }
		});
		
	}
	
	@Override
	public Node getCenter() {

		imageView.setSmooth(false);
		
	    ScrollPane scrollPane = new ScrollPane();
	    scrollPane.setFitToHeight(true);
	    scrollPane.setFitToWidth(true);
		scrollPane.setContent(imageView);
		scrollPane.setPannable(true);
		
//		scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
//			//zoom
//			scale =  scale + (event.getDeltaY() > 0 ? 1 : -1 ) ;
//			double scc = Math.pow(2, scale);
//			imageView.setFitWidth(img.getWidth() * scc);
//			imageView.setFitHeight(img.getHeight() * scc);
//			
//			System.out.println(imageView.getBoundsInLocal().getWidth() + " " + imageView.getBoundsInLocal().getHeight() 
//					+ "  " +
//					imageView.getBoundsInParent().getWidth() + " " + imageView.getBoundsInParent().getHeight());
//			
//			event.consume();
//	    } );		
		
		return scrollPane;
	}

	@Override
	public List<Node> getRight() {
		
		return Arrays.asList(initBtn);
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if(changed.isFileopened()) {
			
			updateImage();
			
		}
		
	}

	private void updateImage() {
		
		PrismImageProducer pip = new PrismImageProducer();
		img = pip.getImg(model.getFileManager().getTraces()); 
		Image i = SwingFXUtils.toFXImage(img, null);
		imageView.setImage(i);
		
		
		WhatChanged changed = new WhatChanged();
		changed.setJustdraw(true);
		listener.somethingChanged(changed);
	}

	@Override
	public void show(int width, int height) {

		//am.init(model.getFileManager().getFiles().get(0).getTraces());
		
		updateImage();
		
		
		
	}

	
}
