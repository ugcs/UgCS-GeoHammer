package com.ugcs.geohammer.service.magnetics;

public record ConventionalCrossover(int surveyLine, int tieLine, double latitude, double longitude,
                                    double surveyDistance, double tieDistance, double surveyValue,
                                    double tieValue) {

    public double misTie() {
        return surveyValue - tieValue;
    }
}
