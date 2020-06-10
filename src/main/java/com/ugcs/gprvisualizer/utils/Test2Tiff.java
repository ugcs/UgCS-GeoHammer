package com.ugcs.gprvisualizer.utils;


import java.awt.image.RenderedImage;
import java.io.File;

//import org.geotools.coverage.CoverageFactoryFinder;
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.io.AbstractGridFormat;
//import org.geotools.gce.geotiff.GeoTiffFormat;
//import org.geotools.gce.geotiff.GeoTiffReader;
//import org.geotools.gce.geotiff.GeoTiffWriter;
//import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.referencing.CRS;
//import org.geotools.referencing.operation.transform.AffineTransform2D;
//import org.geotools.util.factory.GeoTools;
//import org.opengis.parameter.GeneralParameterValue;
//import org.opengis.parameter.ParameterValue;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.opengis.referencing.operation.MathTransform;
//

public class Test2Tiff {

	public static void main(String[] args) throws Exception {
		
		new Test2Tiff().test();
	}

	public void test() throws Exception {
//    String file = "d:\\tmp\\tiff\\c3.tif";
//
//    final File input = new File(file);//TestData.file(GeoTiffReaderTest.class, file);
//    
//    final AbstractGridFormat format = new GeoTiffFormat();
//    System.out.println(format.accepts(input));
//
//    // getting a reader
//    GeoTiffReader reader = new GeoTiffReader(input);
//
//    // reading the coverage
//    GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
//
//    // check coverage and crs
//    //assertNotNull(coverage);
//    //assertNotNull(coverage.getCoordinateReferenceSystem());
//    reader.dispose();
//
//    // get a writer
//    final File writedir = new File("./target", "testWriter");
//    writedir.mkdirs();
//    final File output = new File(writedir, "target.tif");
//    GeoTiffWriter writer = new GeoTiffWriter(output);
//
//    ParameterValue<Boolean> value = GeoTiffFormat.RETAIN_AXES_ORDER.createValue();
//    value.setValue(true);
//
//    // switching axes
//    final CoordinateReferenceSystem latLon4267 = CRS.decode("EPSG:4267");
//    //assertEquals(CRS.getAxisOrder(latLon4267), AxisOrder.NORTH_EAST);
//    final GeneralEnvelope envelope =
//            (GeneralEnvelope) CRS.transform(coverage.getEnvelope(), latLon4267);
//    envelope.setCoordinateReferenceSystem(latLon4267);
//
//    coverage =
//            CoverageFactoryFinder.getGridCoverageFactory(GeoTools.getDefaultHints())
//                    .create(coverage.getName(), coverage.getRenderedImage(), envelope);
//
//    writer.write(coverage, new GeneralParameterValue[] {value});
//    writer.dispose();
//    coverage.dispose(true);
//
//    // getting a reader
//    reader = new GeoTiffReader(output, null); // this way I do not impose the lonlat ordering
//    final GridCoverage2D gc = reader.read(null);
//    final MathTransform g2w_ = gc.getGridGeometry().getGridToCRS();
//    //assertTrue(g2w_ instanceof AffineTransform2D);
//    AffineTransform2D g2w = (AffineTransform2D) g2w_;
//    //assertTrue(XAffineTransform.getSwapXY(g2w) == -1);
//    //assertEquals(AxisOrder.NORTH_EAST, CRS.getAxisOrder(gc.getCoordinateReferenceSystem()));
//    RenderedImage ri = gc.getRenderedImage();
//    //assertEquals(ri.getWidth(), 120);
//    //assertEquals(ri.getHeight(), 121);
////    assertTrue(
////            ((GeneralEnvelope) gc.getEnvelope())
////                    .equals(
////                            coverage.getEnvelope(),
////                            XAffineTransform.getScaleX0(g2w) * 1E-1,
////                            false));
//    reader.dispose();
}
}
