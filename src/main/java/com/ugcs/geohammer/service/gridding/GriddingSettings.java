package com.ugcs.geohammer.service.gridding;

import com.ugcs.geohammer.Settings;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.model.Range;
import com.ugcs.geohammer.service.palette.PaletteType;
import com.ugcs.geohammer.service.palette.SpectrumType;
import com.ugcs.geohammer.util.Strings;
import com.ugcs.geohammer.util.Templates;
import com.ugcs.geohammer.util.Text;
import org.springframework.stereotype.Component;

@Component
public class GriddingSettings {

    private final Settings settings;

    public GriddingSettings(Settings settings) {
        this.settings = settings;
    }

    public GriddingParams loadParams(SgyFile file) {
        double cellSize = 0.1;
        double blankingDistance = 1.0;

        String templateName = Templates.getTemplateName(file);
        if (!Strings.isNullOrEmpty(templateName)) {
            cellSize = settings.getDoubleOrDefault("gridding_cellsize", templateName, cellSize);
            if (cellSize <= 0.0) {
                cellSize = 0.1;
            }
            blankingDistance = settings.getDoubleOrDefault("gridding_blankingdistance", templateName, blankingDistance);
            if (blankingDistance <= 0.0) {
                blankingDistance = 1.0;
            }
        }

        return new GriddingParams(cellSize, blankingDistance);
    }

    public GriddingFilter loadFilter(SgyFile file, String seriesName, Range defaultRange) {
        Range range = defaultRange;
        boolean analyticSignal = false;
        boolean horizontalGradientMagnitude = false;
        boolean firstVerticalDerivative = false;
        boolean reductionToPole = false;
        boolean upwardContinuation = false;
        boolean downwardContinuation = false;
        double continuationDistance = 100.0;
        double rtpInclination = 60.0;
        double rtpDeclination = 0.0;
        boolean hillShading = false;
        boolean smoothing = false;
        PaletteType paletteType = PaletteType.defaultPaletteType();
        SpectrumType spectrumType = SpectrumType.defaultSpectrumType();

        String templateName = Templates.getTemplateName(file);
        if (!Strings.isNullOrEmpty(templateName)) {
            if (!Strings.isNullOrEmpty(seriesName)) {
                Double rangeMin = settings.getDouble("gridding_range_min", templateName + "." + seriesName);
                Double rangeMax = settings.getDouble("gridding_range_max", templateName + "." + seriesName);
                if (rangeMin != null && rangeMax != null) {
                    range = new Range(rangeMin, rangeMax);
                }
            }
            analyticSignal = settings.getBooleanOrDefault("gridding_analytic_signal_enabled", templateName, analyticSignal);
            horizontalGradientMagnitude = settings.getBooleanOrDefault(
                    "gridding_horizontal_gradient_magnitude_enabled", templateName, horizontalGradientMagnitude);
            firstVerticalDerivative = settings.getBooleanOrDefault(
                    "gridding_first_vertical_derivative_enabled", templateName, firstVerticalDerivative);
            reductionToPole = settings.getBooleanOrDefault("gridding_rtp_enabled", templateName, reductionToPole);
            upwardContinuation = settings.getBooleanOrDefault(
                    "gridding_upward_continuation_enabled", templateName, upwardContinuation);
            downwardContinuation = settings.getBooleanOrDefault(
                    "gridding_downward_continuation_enabled", templateName, downwardContinuation);
            continuationDistance = settings.getDoubleOrDefault(
                    "gridding_continuation_distance", templateName, continuationDistance);
            if (continuationDistance <= 0.0) {
                continuationDistance = 100.0;
            }
            rtpInclination = settings.getDoubleOrDefault("gridding_rtp_inclination", templateName, rtpInclination);
            rtpDeclination = settings.getDoubleOrDefault("gridding_rtp_declination", templateName, rtpDeclination);
            hillShading = settings.getBooleanOrDefault("gridding_hillshading_enabled", templateName, hillShading);
            smoothing = settings.getBooleanOrDefault("gridding_smoothing_enabled", templateName, smoothing);
            paletteType = PaletteType.findByName(settings.getStringOrDefault("gridding_palette", templateName, Strings.empty()));
            spectrumType = SpectrumType.findByName(settings.getStringOrDefault("gridding_spectrum", templateName, Strings.empty()));
        }

        return new GriddingFilter(
                range,
                analyticSignal,
                horizontalGradientMagnitude,
                firstVerticalDerivative,
                reductionToPole,
                upwardContinuation,
                downwardContinuation,
                continuationDistance,
                rtpInclination,
                rtpDeclination,
                hillShading,
                smoothing,
                paletteType,
                spectrumType);
    }

    public void saveParams(SgyFile file, GriddingParams params) {
        if (params == null) {
            return;
        }
        String templateName = Templates.getTemplateName(file);
        if (Strings.isNullOrEmpty(templateName)) {
            return;
        }

        settings.setValue("gridding_cellsize", templateName, Text.formatNumber(params.cellSize()));
        settings.setValue("gridding_blankingdistance", templateName, Text.formatNumber(params.blankingDistance()));
    }

    public void saveFilter(SgyFile file, String seriesName, GriddingFilter filter) {
        if (filter == null) {
            return;
        }
        String templateName = Templates.getTemplateName(file);
        if (Strings.isNullOrEmpty(templateName)) {
            return;
        }

        settings.setValue("gridding_hillshading_enabled", templateName, Boolean.toString(filter.hillShading()));
        settings.setValue("gridding_smoothing_enabled", templateName, Boolean.toString(filter.smoothing()));
        settings.setValue("gridding_analytic_signal_enabled", templateName, Boolean.toString(filter.analyticSignal()));
        settings.setValue("gridding_horizontal_gradient_magnitude_enabled", templateName,
                Boolean.toString(filter.horizontalGradientMagnitude()));
        settings.setValue("gridding_first_vertical_derivative_enabled", templateName,
                Boolean.toString(filter.firstVerticalDerivative()));
        settings.setValue("gridding_rtp_enabled", templateName, Boolean.toString(filter.reductionToPole()));
        settings.setValue("gridding_upward_continuation_enabled", templateName,
                Boolean.toString(filter.upwardContinuation()));
        settings.setValue("gridding_downward_continuation_enabled", templateName,
                Boolean.toString(filter.downwardContinuation()));
        settings.setValue("gridding_continuation_distance", templateName,
                Text.formatNumber(filter.continuationDistance()));
        settings.setValue("gridding_rtp_inclination", templateName, Text.formatNumber(filter.rtpInclination()));
        settings.setValue("gridding_rtp_declination", templateName, Text.formatNumber(filter.rtpDeclination()));
        settings.setValue("gridding_palette", templateName, filter.paletteType().name());
        settings.setValue("gridding_spectrum", templateName, filter.spectrumType().name());
        Range range = filter.range();
        if (range != null && !Strings.isNullOrEmpty(seriesName)) {
            settings.setValue("gridding_range_min", templateName + "." + seriesName, Text.formatNumber(range.getMin()));
            settings.setValue("gridding_range_max", templateName + "." + seriesName, Text.formatNumber(range.getMax()));
        }
    }
}
