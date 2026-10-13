package com.ugcs.geohammer.service.magnetics;

import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.util.Check;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecipeReplayService {

    private final IgrfRemovalService igrfRemovalService;
    private final DiurnalCorrectionService diurnalCorrectionService;
    private final HeadingCorrectionService headingCorrectionService;
    private final LineLevelingService lineLevelingService;
    private final CrossoverLevelingService crossoverLevelingService;
    private final ConventionalLevelingService conventionalLevelingService;
    private final MicroLevelingService microLevelingService;
    private final RegionalRemovalService regionalRemovalService;
    public RecipeReplayService(IgrfRemovalService igrfRemovalService, DiurnalCorrectionService diurnalCorrectionService,
                               HeadingCorrectionService headingCorrectionService,
                               LineLevelingService lineLevelingService, CrossoverLevelingService crossoverLevelingService,
                               ConventionalLevelingService conventionalLevelingService,
                               MicroLevelingService microLevelingService, RegionalRemovalService regionalRemovalService) {
        this.igrfRemovalService = igrfRemovalService;
        this.diurnalCorrectionService = diurnalCorrectionService;
        this.headingCorrectionService = headingCorrectionService;
        this.lineLevelingService = lineLevelingService;
        this.crossoverLevelingService = crossoverLevelingService;
        this.conventionalLevelingService = conventionalLevelingService;
        this.microLevelingService = microLevelingService;
        this.regionalRemovalService = regionalRemovalService;
    }

    public void replay(SensorLineChart chart, List<MagneticProcessingStep> steps) {
        Check.notNull(chart);
        Check.notEmpty(steps);
        for (MagneticProcessingStep step : steps) replayStep(chart, step);
    }

    private void replayStep(SensorLineChart chart, MagneticProcessingStep step) {
        String input = step.inputSeries();
        String output = step.outputSeries();
        switch (step.type()) {
            case DIURNAL_CORRECTION -> replayDiurnalCorrection(chart, step);
            case IGRF_REMOVAL -> {
                Instant fallback = step.settings().containsKey("fallbackTimestamp")
                        ? Instant.parse(step.settings().get("fallbackTimestamp")) : null;
                chart.createDerivedSeries(output, input, igrfRemovalService.remove(chart.getFile().getGeoData(), input, fallback));
            }
            case HEADING_CORRECTION -> {
                int bins = integerSetting(step, "headingBins");
                chart.createDerivedSeries(output, input, headingCorrectionService.correct(chart.getFile().getGeoData(), input, bins).values());
            }
            case LINE_LEVELING -> replayLineLeveling(chart, step);
            case MICRO_LEVELING -> {
                int window = integerSetting(step, "windowSize");
                chart.createDerivedSeries(output, input, microLevelingService.level(chart.getFile().getGeoData(), input, window));
            }
            case REGIONAL_REMOVAL -> {
                int order = integerSetting(step, "polynomialOrder");
                chart.createDerivedSeries(output, input, regionalRemovalService.remove(chart.getFile().getGeoData(), input, order).values());
            }
        }
    }

    private void replayDiurnalCorrection(SensorLineChart chart, MagneticProcessingStep step) {
        if (!step.settings().getOrDefault("baseSource", "base-station-file").equals("synchronized-series")) {
            throw new IllegalArgumentException("Diurnal replay requires a base-station file");
        }
        String baseSeries = step.settings().get("baseStationSeries");
        if (baseSeries == null || baseSeries.isBlank()) {
            throw new IllegalArgumentException("Diurnal replay is missing the synchronized base series");
        }
        Double referenceField = step.settings().containsKey("referenceField")
                ? Double.valueOf(step.settings().get("referenceField")) : null;
        chart.createDerivedSeries(step.outputSeries(), step.inputSeries(), diurnalCorrectionService.correctSynchronized(
                chart.getFile().getGeoData(), step.inputSeries(), baseSeries, referenceField).values());
    }

    private void replayLineLeveling(SensorLineChart chart, MagneticProcessingStep step) {
        String method = step.settings().getOrDefault("method", "line-median");
        if (method.equals("line-median")) {
            chart.createDerivedSeries(step.outputSeries(), step.inputSeries(), lineLevelingService.level(chart.getFile().getGeoData(), step.inputSeries()));
            return;
        }
        if (method.equals("tie-line-crossover")) {
            Set<Integer> ties = tieLines(step);
            chart.createDerivedSeries(step.outputSeries(), step.inputSeries(), crossoverLevelingService.level(chart.getFile().getGeoData(), step.inputSeries(), ties).values());
            return;
        }
        if (method.equals("conventional-tie-line")) {
            Set<Integer> ties = tieLines(step);
            TieLineCorrectionModel model = TieLineCorrectionModel.valueOf(step.settings().get("correctionModel"));
            int order = integerSetting(step, "polynomialOrder");
            ConventionalLevelingOptions options = new ConventionalLevelingOptions(step.inputSeries(), step.outputSeries(), ties,
                    model, order);
            ConventionalLevelingResult result = conventionalLevelingService.level(chart.getFile().getGeoData(), options);
            chart.createDerivedSeries(step.outputSeries() + "_TIE_CORRECTION", step.inputSeries(), result.tieCorrections());
            chart.createDerivedSeries(step.outputSeries() + "_LINE_CORRECTION", step.inputSeries(), result.surveyCorrections());
            chart.createDerivedSeries(step.outputSeries(), step.inputSeries(), result.values());
            return;
        }
        throw new IllegalArgumentException("Unsupported line-leveling method: " + method);
    }

    private static int integerSetting(MagneticProcessingStep step, String key) {
        try { return Integer.parseInt(step.settings().get(key)); }
        catch (RuntimeException e) { throw new IllegalArgumentException(step.type() + " is missing setting '" + key + "'"); }
    }

    private static Set<Integer> tieLines(MagneticProcessingStep step) {
        String value = step.settings().get("tieLineIds");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Crossover leveling is missing tie-line IDs");
        Set<Integer> ties = new LinkedHashSet<>();
        for (String item : value.split(",")) ties.add(Integer.parseInt(item));
        return ties;
    }
}
