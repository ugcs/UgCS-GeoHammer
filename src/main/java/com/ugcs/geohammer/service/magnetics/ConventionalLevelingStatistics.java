package com.ugcs.geohammer.service.magnetics;

public record ConventionalLevelingStatistics(int count, double mean, double median, double standardDeviation,
                                             double rms, double maximumAbsolute) {

    public static ConventionalLevelingStatistics empty() {
        return new ConventionalLevelingStatistics(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
}
