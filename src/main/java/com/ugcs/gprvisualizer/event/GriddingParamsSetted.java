package com.ugcs.gprvisualizer.event;

/**
 * Event class for configuring grid interpolation parameters.
 * 
 * Supports two interpolation methods:
 * 1. SPLINES - Traditional spline interpolation, suitable for:
 *    - Dense, evenly spaced data
 *    - Small to medium cell sizes
 *    - Smooth continuous surfaces
 * 
 * 2. IDW (Inverse Distance Weighting) - Alternative method, better for:
 *    - Large cell sizes
 *    - Sparse or irregular data
 *    - Avoiding interpolation artifacts
 *    
 * IDW parameters:
 * - Power: Controls how quickly influence decreases with distance (typically 2.0)
 * - MinPoints: Minimum number of points to use for interpolation
 * - Search radius: Automatically adjusted based on cell size
 */
public class GriddingParamsSetted extends BaseEvent {

    public enum InterpolationMethod {
        SPLINES,
        IDW
    }

    private final double cellSize;
    private final double blankingDistance;
    private final boolean toAll;
    private final InterpolationMethod interpolationMethod;
    private final double idwPower;
    private final int idwMinPoints;

    // Default and limit values for IDW parameters
    private static final double DEFAULT_POWER = 2.0;
    private static final int DEFAULT_MIN_POINTS = 6;
    private static final double MIN_CELL_SIZE = 0.01;
    private static final double MAX_POWER = 3.0;
    private static final int MAX_MIN_POINTS = 12;
    private static final double IDW_CELL_SIZE_THRESHOLD = 0.09;

    public GriddingParamsSetted(Object source, double cellSize, double blankingDistance) {
        this(source, cellSize, blankingDistance, false, 
             cellSize > IDW_CELL_SIZE_THRESHOLD ? InterpolationMethod.IDW : InterpolationMethod.SPLINES,
             DEFAULT_POWER, DEFAULT_MIN_POINTS);
    }

    public GriddingParamsSetted(Object source, double cellSize, double blankingDistance, boolean toAll) {
        this(source, cellSize, blankingDistance, toAll,
             cellSize > IDW_CELL_SIZE_THRESHOLD ? InterpolationMethod.IDW : InterpolationMethod.SPLINES,
             DEFAULT_POWER, DEFAULT_MIN_POINTS);
    }

    public GriddingParamsSetted(Object source, double cellSize, double blankingDistance, boolean toAll,
                               InterpolationMethod interpolationMethod, double idwPower, int idwMinPoints) {
        super(source);

        // Validate and adjust parameters for optimal performance
        this.cellSize = Math.max(cellSize, MIN_CELL_SIZE);
        this.blankingDistance = blankingDistance;
        this.toAll = toAll;
        this.interpolationMethod = interpolationMethod;

        // Optimize IDW parameters for better performance
        this.idwPower = interpolationMethod == InterpolationMethod.IDW ?
            Math.min(Math.max(idwPower, DEFAULT_POWER), MAX_POWER) : DEFAULT_POWER;
        this.idwMinPoints = interpolationMethod == InterpolationMethod.IDW ?
            Math.min(Math.max(idwMinPoints, DEFAULT_MIN_POINTS), MAX_MIN_POINTS) : DEFAULT_MIN_POINTS;
    }

    public double getCellSize() {
        return cellSize;
    }

    public double getBlankingDistance() {
        return blankingDistance;
    }

    public boolean isToAll() {
        return toAll;
    }

    public InterpolationMethod getInterpolationMethod() {
        return interpolationMethod;
    }

    public double getIdwPower() {
        return idwPower;
    }

    public int getIdwMinPoints() {
        return idwMinPoints;
    }
}
