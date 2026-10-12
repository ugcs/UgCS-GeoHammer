package com.ugcs.geohammer.math;

import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.util.Check;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

import java.util.Arrays;

public class VerticalContinuationFilter {

    private static final double MAX_DOWNWARD_GAIN = 100.0;

    private final float[][] source;

    private final float[][] grid;

    private final int width;

    private final int height;

    private final double cellWidth;

    private final double cellHeight;

    private final double distance;

    private final boolean upward;

    public VerticalContinuationFilter(float[][] grid, LatLon min, LatLon max, double distance, boolean upward) {
        Check.notNull(grid);
        Check.condition(Double.isFinite(distance) && distance > 0.0,
                "Continuation distance must be a positive finite value");

        source = grid;
        width = grid.length;
        height = width > 0 ? grid[0].length : 0;
        this.grid = copy(grid);
        cellWidth = AnalyticSignalFilter.getCellWidth(grid, min, max);
        cellHeight = AnalyticSignalFilter.getCellHeight(grid, min, max);
        this.distance = distance;
        this.upward = upward;
        GridInterpolator.interpolate(this.grid, cellWidth, cellHeight);
    }

    public float[][] apply() {
        FloatFFT_2D fft = new FloatFFT_2D(width, height);
        float[][] values = new float[width][2 * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                values[x][2 * y] = grid[x][y];
            }
        }
        fft.complexForward(values);

        double dkx = 2.0 * Math.PI / (width * cellWidth);
        double dky = 2.0 * Math.PI / (height * cellHeight);
        for (int x = 0; x < width; x++) {
            int xIndex = x <= width / 2 ? x : x - width;
            double kx = xIndex * dkx;
            for (int y = 0; y < height; y++) {
                int yIndex = y <= height / 2 ? y : y - height;
                double ky = yIndex * dky;
                double exponent = Math.hypot(kx, ky) * distance;
                if (!upward) {
                    exponent = Math.min(exponent, Math.log(MAX_DOWNWARD_GAIN));
                }
                double multiplier = Math.exp(upward ? -exponent : exponent);
                values[x][2 * y] = (float) (values[x][2 * y] * multiplier);
                values[x][2 * y + 1] = (float) (values[x][2 * y + 1] * multiplier);
            }
        }
        fft.complexInverse(values, true);

        float[][] transformed = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                transformed[x][y] = Float.isNaN(source[x][y]) ? Float.NaN : values[x][2 * y];
            }
        }
        return transformed;
    }

    private static float[][] copy(float[][] values) {
        float[][] copy = new float[values.length][];
        for (int index = 0; index < values.length; index++) {
            copy[index] = Arrays.copyOf(values[index], values[index].length);
        }
        return copy;
    }
}
