package com.ugcs.gprvisualizer.draw;

public class GriddingParamsSetted extends WhatChanged {

    private final double cellSize;
    private final double blankingDistance;
    private final boolean toAll;

    public GriddingParamsSetted(double cellSize, double blankingDistance) {
        this(cellSize, blankingDistance, false);
    }

    public GriddingParamsSetted(double cellSize, double blankingDistance, boolean toAll) {
        super(Change.setGriddingParams);
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