package com.ugcs.gprvisualizer.utils;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.io.AbstractGridFormat;
//import org.geotools.coverage.grid.io.GridCoverage2DReader;
//import org.geotools.coverage.grid.io.GridFormatFinder;
//import org.geotools.gce.geotiff.GeoTiffReader;
//import org.geotools.util.factory.Hints;
//import org.opengis.geometry.Envelope;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Tiff23Test {

	
	public static void main(String[] args) throws IOException {
		
		File file = new File("d:\\tmp\\tiff\\fff4.tif");

		//AbstractGridFormat format = GridFormatFinder.findFormat( file );
		//GridCoverage2DReader reader = format.getReader( file );
		//You can also use GeoTiffReader directly:
		//File file = new File("test.tiff");

//		GeoTiffReader reader = new GeoTiffReader(file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
//		//You can use the reader to access a GridCoverage2D as normal:
//		
//
//		GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
//		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
//		Envelope env = coverage.getEnvelope();
//		RenderedImage image = coverage.getRenderedImage();
		
	}
	
}
