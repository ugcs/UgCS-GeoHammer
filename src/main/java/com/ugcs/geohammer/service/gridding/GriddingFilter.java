package com.ugcs.geohammer.service.gridding;

import com.ugcs.geohammer.model.Range;
import com.ugcs.geohammer.service.palette.PaletteType;
import com.ugcs.geohammer.service.palette.SpectrumType;

public record GriddingFilter(
        Range range,
        boolean analyticSignal,
        boolean horizontalGradientMagnitude,
        boolean firstVerticalDerivative,
        boolean reductionToPole,
        boolean upwardContinuation,
        boolean downwardContinuation,
        double continuationDistance,
        double rtpInclination,
        double rtpDeclination,
        boolean hillShading,
        boolean smoothing,
        PaletteType paletteType,
        SpectrumType spectrumType
) {
    public boolean usesDerivativeProduct() {
        return analyticSignal || horizontalGradientMagnitude || firstVerticalDerivative;
    }
}
