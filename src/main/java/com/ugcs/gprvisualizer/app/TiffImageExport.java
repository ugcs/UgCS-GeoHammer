package com.ugcs.gprvisualizer.app;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.TiffImagingCreation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;

public class TiffImageExport {

	private Model model;
	private RadarMap radarMap;
	
	public TiffImageExport(Model model, RadarMap radarMap) {
		this.model = model;
		this.radarMap = radarMap;
	}
	
	public void execute() {

		Dimension dim = new Dimension(1400, 1400);
		
		FileChooser chooser = new FileChooser();

		chooser.setTitle("Save tiff file");
		chooser.setInitialFileName("geohammer.tif");
		
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TIFF files (*.tif)", "*.tif");
		chooser.getExtensionFilters().add(extFilter);

		File tiffFile = chooser.showSaveDialog(AppContext.stage); 

		if (tiffFile == null) {
			return;
		}

		
		MapField field = new MapField(model.getField());
		
		field.setSceneCenter(model.getField().getPathCenter());		
		field.adjustZoom(dim.width, dim.height);
		
		double radiusFactor = Math.pow(2, field.getZoom() - model.getField().getZoom());
		
		
		BufferedImage tiffImg = drawTiff(field, radarMap, 
				dim.width, dim.height, radiusFactor);
		

		LatLon lt = field.screenTolatLon(
				new Point2D.Double(
						-tiffImg.getWidth() / 2,
						-tiffImg.getHeight() / 2));
		LatLon rb = field.screenTolatLon(
				new Point2D.Double(
						+tiffImg.getWidth() / 2,
						+tiffImg.getHeight() / 2));

		try {
			
			new TiffImagingCreation().save(
					tiffFile, 
					tiffImg, lt, rb);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			MessageBoxHelper.showError(
					"Error", "Can`t save file");

		}
	}
	
	protected BufferedImage drawTiff(MapField field, RadarMap radarMap, 
			int width, int height, double radiusFactor) {
		if (width <= 0 || height <= 0) {
			return null;
		}
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		
		g2.translate(width / 2, height / 2);
		
		BufferedImage imgRadar = radarMap.createHiRes(field, width, height, radiusFactor);
		
		
		radarMap.draw(g2, field, imgRadar);
		
		new GpsTrack(null, model, null).draw(g2, field);
		
		return bi;
	}
	

}
