package com.ugcs.gprvisualizer.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import com.github.thecoldwine.sigrun.common.ext.LatLon;

public class GeoTiffImagingCreation {

	public static void main(String[] args) throws Exception {
		File f1 = new File("created1.tif");
		System.out.println(f1.getAbsolutePath());
		BufferedImage img = createSomeImage();
		LatLon lt =  new LatLon(54, 			24.00); 
		LatLon rb = new LatLon(54 - 0.02, 	24.02);

		new GeoTiffImagingCreation().save(f1, img, lt, rb);
	}

	private static BufferedImage createSomeImage() {
		BufferedImage img = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();

		g2.setColor(Color.GRAY);

		int mrg = img.getHeight() / 4;

		g2.fillRect(mrg, mrg, img.getWidth() - 2 * mrg, img.getHeight() - 2 * mrg);

		g2.setColor(Color.RED);
		for (int x = 10; x < img.getWidth(); x += 20) {
			g2.drawLine(x, 30, x, img.getHeight() - 30);
		}
		return img;
	}

	/**
	 * Saves a BufferedImage as a GeoTIFF file with the specified geographic coordinates.
	 *
	 * @param outputFile The file to save the GeoTIFF to
	 * @param image The image to save
	 * @param topLeft The top-left coordinate (maximum latitude, minimum longitude)
	 * @param bottomRight The bottom-right coordinate (minimum latitude, maximum longitude)
	 * @throws Exception if an error occurs during file writing
	 */
	public void save(File outputFile,
		BufferedImage image,
		LatLon topLeft,
		LatLon bottomRight) throws Exception {

		String EPSG4326 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
		CoordinateReferenceSystem crs = CRS.parseWKT(EPSG4326);

		// For EPSG:4326, GeoTools with parseWKT expects (longitude, latitude) order
		// Create envelope with geographic coordinates (minY, maxY, minX, maxX)
		ReferencedEnvelope envelope = new ReferencedEnvelope(
			Math.min(topLeft.getLonDgr(), bottomRight.getLonDgr()),     // min longitude
			Math.max(topLeft.getLonDgr(), bottomRight.getLonDgr()),     // max longitude
			Math.min(topLeft.getLatDgr(), bottomRight.getLatDgr()),     // min latitude
			Math.max(topLeft.getLatDgr(), bottomRight.getLatDgr()),     // max latitude
			crs
		);

		// Create GeoTIFF coverage
		GridCoverageFactory factory = new GridCoverageFactory();
		GridCoverage2D coverage = factory.create(
			"GeoTIFF",
			image,
			envelope
		);

		// Write GeoTIFF using GeoTools
		GeoTiffWriter writer = new GeoTiffWriter(outputFile);
		try {
			writer.write(coverage, null);
		} finally {
			writer.dispose();
			coverage.dispose(true);
		}
	}
}
