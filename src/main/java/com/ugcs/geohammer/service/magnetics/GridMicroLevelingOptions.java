package com.ugcs.geohammer.service.magnetics;

import com.ugcs.geohammer.util.Check;

import java.util.Set;

public record GridMicroLevelingOptions(String inputSeries, String outputSeries, String noiseGridSeries,
                                       double smoothingWavelength, double maximumCorrection,
                                       Set<Integer> primaryLineIds) {
    public GridMicroLevelingOptions {
        Check.notEmpty(inputSeries);
        Check.notEmpty(outputSeries);
        Check.notEmpty(noiseGridSeries);
        Check.condition(smoothingWavelength > 0, "Smoothing wavelength must be positive");
        Check.condition(maximumCorrection > 0, "Maximum correction must be positive");
        Check.notEmpty(primaryLineIds);
        primaryLineIds = Set.copyOf(primaryLineIds);
    }
}
