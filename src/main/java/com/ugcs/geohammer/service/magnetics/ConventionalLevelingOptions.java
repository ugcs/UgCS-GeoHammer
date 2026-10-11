package com.ugcs.geohammer.service.magnetics;

import com.ugcs.geohammer.util.Check;

import java.util.Set;

public record ConventionalLevelingOptions(
        String inputSeries,
        String outputSeries,
        Set<Integer> tieLineIds,
        TieLineCorrectionModel correctionModel,
        int polynomialOrder
) {
    public ConventionalLevelingOptions {
        Check.notEmpty(inputSeries);
        Check.notEmpty(outputSeries);
        Check.notEmpty(tieLineIds);
        Check.notNull(correctionModel);
        Check.condition(polynomialOrder >= 0 && polynomialOrder <= 3,
                "Polynomial order must be between 0 and 3");
        tieLineIds = Set.copyOf(tieLineIds);
    }
}
