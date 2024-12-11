package com.ugcs.gprvisualizer.event;

public class GriddingParamsSetted extends BaseEvent {

    private final double cellSize;
    private final double blankingDistance;
    private final boolean toAll;

    public GriddingParamsSetted(Object source, double cellSize, double blankingDistance) {
        this(source, cellSize, blankingDistance, false);
    }

    public GriddingParamsSetted(Object source, double cellSize, double blankingDistance, boolean toAll) {
        super(source);
        this.cellSize = cellSize;
        this.blankingDistance = blankingDistance;
        this.toAll = toAll;
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
}