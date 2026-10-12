package com.ugcs.geohammer.math;

import com.ugcs.geohammer.model.LatLon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerticalContinuationFilterTest {

    @Test
    void upwardContinuationAttenuatesAndDownwardContinuationAmplifiesSignal() {
        float[][] grid = createSineGrid(16);
        LatLon min = new LatLon(0.0, 0.0);
        LatLon max = new LatLon(16.0 / 111_000.0, 16.0 / 111_000.0);
        double waveNumber = 2.0 * Math.PI / (grid.length * AnalyticSignalFilter.getCellWidth(grid, min, max));

        float upward = new VerticalContinuationFilter(grid, min, max, 1.0, true).apply()[4][0];
        float downward = new VerticalContinuationFilter(grid, min, max, 1.0, false).apply()[4][0];

        assertEquals(Math.exp(-waveNumber), upward, 0.001);
        assertEquals(Math.exp(waveNumber), downward, 0.001);
    }

    private static float[][] createSineGrid(int size) {
        float[][] grid = new float[size][size];
        for (int x = 0; x < size; x++) {
            float value = (float) Math.sin(2.0 * Math.PI * x / size);
            for (int y = 0; y < size; y++) {
                grid[x][y] = value;
            }
        }
        return grid;
    }
}
