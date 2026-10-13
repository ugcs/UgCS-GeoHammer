package com.ugcs.geohammer.service.gridding;

import com.ugcs.geohammer.map.layer.GridLayer;
import com.ugcs.geohammer.model.LatLon;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoTiffGridExporterTest {

    @Test
    void exportsGeoTiff() throws Exception {
        GridLayer.Grid grid = new GridLayer.Grid(
                "MAG",
                new float[][] {{1, 2}, {3, Float.NaN}},
                new float[0],
                new LatLon(44, -74),
                new LatLon(45, -73),
                null,
                null,
                0,
                null);
        Path target = Files.createTempFile("geohammer-grid", ".tif");
        Files.delete(target);

        try {
            new GeoTiffGridExporter().export(grid, target.toFile());

            assertTrue(Files.isRegularFile(target));
            assertTrue(Files.size(target) > 0);
        } finally {
            Files.deleteIfExists(target);
        }
    }
}
