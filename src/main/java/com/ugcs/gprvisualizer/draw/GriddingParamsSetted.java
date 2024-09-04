package com.ugcs.gprvisualizer.draw;

public class GriddingParamsSetted extends WhatChanged {

    private final double cellSize;
    private final double blankingDistance;

    public GriddingParamsSetted(double cellSize, double blankingDistance) {
        super(Change.setGriddingParams);
        this.cellSize = cellSize;
        this.blankingDistance = blankingDistance;
    }

    public double getCellSize() {
        return cellSize;
    }

    public double getBlankingDistance() {
        return blankingDistance;
    }

}