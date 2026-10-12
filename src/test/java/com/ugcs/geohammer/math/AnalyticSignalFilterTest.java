package com.ugcs.geohammer.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyticSignalFilterTest {

    @Test
    void horizontalGradientMagnitude_usesBothHorizontalDerivatives() {
        float[][] grid = new float[7][7];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = i + 2 * j;
            }
        }

        float[][] hgm = new AnalyticSignalFilter(grid, 1.0, 1.0).evaluateHorizontalGradientMagnitude();

        assertEquals(Math.sqrt(5.0), hgm[3][3], 0.0001);
    }

    @Test
    void firstVerticalDerivative_ofConstantGridIsZero() {
        float[][] grid = new float[8][8];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = 42.0f;
            }
        }

        float[][] derivative = new AnalyticSignalFilter(grid, 1.0, 1.0).evaluateFirstVerticalDerivative();

        assertEquals(0.0, derivative[4][4], 0.0001);
    }
}
