package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.utils.Check;

import java.awt.Color;

public class PointQualityIssue extends QualityIssue {

    private final LatLon center;

    // in meters
    private final double radius;

    public PointQualityIssue(Color color, LatLon center, double radius) {
        super(color);

        Check.notNull(center);
        Check.condition(radius > 0.0);

        this.center = center;
        this.radius = radius;
    }

    public LatLon getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
