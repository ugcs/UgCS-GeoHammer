package com.ugcs.gprvisualizer.app;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.ugcs.gprvisualizer.draw.GpsTrack;
import com.ugcs.gprvisualizer.draw.GridLayer;
import com.ugcs.gprvisualizer.draw.RadarMap;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.GeoTiffImagingCreation;

import javafx.stage.FileChooser;

public class TiffImageExport {

	private static final Dimension maxTifSize = new Dimension(1800, 1800);
	 
	private Model model;

	private RadarMap radarMap;
	private GpsTrack gpsTrack;
	private GridLayer gridLayer;

	public TiffImageExport(Model model, RadarMap radarMap,
			GpsTrack gpsTrack, GridLayer gridLayer) {
		this.model = model;
		this.radarMap = radarMap;
		this.gpsTrack = gpsTrack;
		this.gridLayer = gridLayer;
	}
	
	public void execute() {
		
		File tiffFile = askUserChooseFile(); 

		if (tiffFile == null) {
			return;
		}

		MapField field = prepareField();
		
		Point2D scrrb = field.latLonToScreen(field.getPathRightBottom());
		
		Dimension tiffActualSize = new Dimension(
				(int) Math.abs(scrrb.getX() * 2) + 100,
				(int) Math.abs(scrrb.getY() * 2) + 100);
		
		BufferedImage tiffImg = drawTiff(field, 
				tiffActualSize.width, tiffActualSize.height);
		

		LatLon lt = field.screenTolatLon(
				new Point2D.Double(
						-tiffImg.getWidth() / 2,
						-tiffImg.getHeight() / 2));
		LatLon rb = field.screenTolatLon(
				new Point2D.Double(
						+tiffImg.getWidth() / 2,
						+tiffImg.getHeight() / 2));

		try {
			
			new GeoTiffImagingCreation().save(
					tiffFile, 
					tiffImg, lt, rb);
			
			AppContext.status.showProgressText("Export to '" + tiffFile.getName() + "' finished!");
			
		} catch (Exception e) {
			AppContext.status.showProgressText("Error during export to' " + tiffFile.getName() + "'");
			
			e.printStackTrace();
			
			MessageBoxHelper.showError(
					"Error", "Can`t save file");

		}
	}

	public MapField prepareField() {
		MapField field = new MapField(model.getMapField());
		field.setSceneCenter(model.getMapField().getPathCenter());		
		field.adjustZoom(maxTifSize.width, maxTifSize.height);
		return field;
	}

	public File askUserChooseFile() {
		FileChooser chooser = new FileChooser();

		chooser.setTitle("Save tiff file");
		
		if (model.getSettings().lastExportFolder != null) {
			chooser.setInitialDirectory(model.getSettings().lastExportFolder);
		}
		
		chooser.setInitialFileName("geohammer.tif");
		
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TIFF files (*.tif)", "*.tif");
		chooser.getExtensionFilters().add(extFilter);

		File tiffFile = chooser.showSaveDialog(AppContext.stage);
		
		if (tiffFile != null) {
			model.getSettings().lastExportFolder = tiffFile.getParentFile();
		}
		
		return tiffFile;
	}
	
	protected BufferedImage drawTiff(MapField field, int width, int height) {
		if (width <= 0 || height <= 0) {
			return null;
		}

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = (Graphics2D) img.getGraphics();

		g2.setComposite(AlphaComposite.Src);

		// g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
		// g2.setColor(new Color(0, 0, 0, 0)); // Transparent
		// g2.fillRect(0, 0, width, height);

		g2.translate(width / 2, height / 2);

		radarMap.createHiRes(field, img);

		// radarMap.draw(g2, field, imgRadar);

		if (gridLayer != null && gridLayer.isActive()) {
			gridLayer.draw(g2, field);
		}

		if (gpsTrack != null && gpsTrack.isActive()) {
			gpsTrack.drawTrack(g2, field);
		}

		return img;
	}
	
}
