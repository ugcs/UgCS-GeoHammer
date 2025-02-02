package com.ugcs.gprvisualizer.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.thecoldwine.sigrun.common.ext.LatLon;

class GeoTiffImagingCreationTest {
    private File testFile;
    private BufferedImage testImage;
    private LatLon topLeft;
    private LatLon bottomRight;
    private GeoTiffImagingCreation geoTiff;

    @BeforeEach
    void setUp() {
        testFile = new File("test_geotiff.tif");
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        topLeft = new LatLon(54.0, 24.0);
        bottomRight = new LatLon(53.98, 24.02);
        geoTiff = new GeoTiffImagingCreation();
    }

    @AfterEach
    void tearDown() {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testSaveCreatesValidGeoTiff() throws Exception {
        // When
        geoTiff.save(testFile, testImage, topLeft, bottomRight);

        // Then
        assertTrue(testFile.exists(), "GeoTIFF file should be created");
        assertTrue(testFile.length() > 0, "GeoTIFF file should not be empty");

        // Verify GeoTIFF metadata
        GeoTiffReader reader = new GeoTiffReader(testFile);
        try {
            GridCoverage2D coverage = reader.read(null);
            assertNotNull(coverage, "Should create valid coverage");

            // Verify coordinate system
            CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
            assertNotNull(crs, "Should have coordinate reference system");
            assertTrue(crs.getName().toString().contains("WGS") 
                && crs.getName().toString().contains("84"), 
                "Should use WGS84 coordinate system");

            // Verify bounds
            // Get the envelope corners
            double minX = coverage.getEnvelope2D().getMinX();
            double minY = coverage.getEnvelope2D().getMinY();
            double maxX = coverage.getEnvelope2D().getMaxX();
            double maxY = coverage.getEnvelope2D().getMaxY();

            System.out.println("[DEBUG_LOG] Test - Coverage bounds:");
            System.out.println("[DEBUG_LOG] X (lon) range: " + minX + " to " + maxX);
            System.out.println("[DEBUG_LOG] Y (lat) range: " + minY + " to " + maxY);

            // Verify longitude bounds (X coordinates)
            assertEquals(24.0, minX, 0.0001, "Minimum longitude should match");
            assertEquals(24.02, maxX, 0.0001, "Maximum longitude should match");

            // Verify latitude bounds (Y coordinates)
            assertEquals(53.98, minY, 0.0001, "Minimum latitude should match");
            assertEquals(54.0, maxY, 0.0001, "Maximum latitude should match");

            coverage.dispose(true);
        } finally {
            reader.dispose();
        }
    }
}
