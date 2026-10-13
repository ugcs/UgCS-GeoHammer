package com.ugcs.geohammer.service.gridding;

import com.ugcs.geohammer.map.layer.GridLayer;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

@Service
public class GeoTiffGridExporter {

    public void export(GridLayer.Grid grid, File target) throws IOException {
        float[][] values = grid.values();
        int width = values.length;
        int height = width > 0 ? values[0].length : 0;
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Grid has no values to export");
        }

        WritableRaster raster = createRaster(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                raster.setSample(x, height - y - 1, 0, values[x][y]);
            }
        }

        ReferencedEnvelope bounds = new ReferencedEnvelope(
                grid.minLatLon().getLonDgr(), grid.maxLatLon().getLonDgr(),
                grid.minLatLon().getLatDgr(), grid.maxLatLon().getLatDgr(),
                DefaultGeographicCRS.WGS84);
        GridCoverage2D coverage = new GridCoverageFactory().create(grid.seriesName(), raster, bounds);
        GeoTiffWriter writer = new GeoTiffWriter(target);
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }
    }

    private static WritableRaster createRaster(int width, int height) {
        SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        return Raster.createWritableRaster(sampleModel, buffer, new Point());
    }
}
