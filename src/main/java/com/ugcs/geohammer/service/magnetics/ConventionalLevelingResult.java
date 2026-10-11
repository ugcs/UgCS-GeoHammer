package com.ugcs.geohammer.service.magnetics;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ConventionalLevelingResult(List<@Nullable Number> values, List<@Nullable Number> tieCorrections,
                                         List<@Nullable Number> surveyCorrections,
                                         List<ConventionalCrossover> crossovers,
                                         ConventionalLevelingStatistics before,
                                         ConventionalLevelingStatistics after) {
    public ConventionalLevelingResult {
        values = Collections.unmodifiableList(new ArrayList<>(values));
        tieCorrections = Collections.unmodifiableList(new ArrayList<>(tieCorrections));
        surveyCorrections = Collections.unmodifiableList(new ArrayList<>(surveyCorrections));
        crossovers = List.copyOf(crossovers);
    }
}
