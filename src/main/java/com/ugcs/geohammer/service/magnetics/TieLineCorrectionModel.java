package com.ugcs.geohammer.service.magnetics;

public enum TieLineCorrectionModel {
    MEDIAN("Median adjustment"),
    LEAST_SQUARES("Least-squares"),
    POLYNOMIAL("Polynomial"),
    SPLINE("Spline");

    private final String label;

    TieLineCorrectionModel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
