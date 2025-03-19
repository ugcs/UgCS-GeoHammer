package com.ugcs.gprvisualizer.app.quality;

import com.ugcs.gprvisualizer.utils.Check;
import org.locationtech.jts.geom.Coordinate;

import java.awt.Color;

public class PointQualityIssue extends QualityIssue {

    private final Coordinate center;

    // in meters
    private final double radius;

    public PointQualityIssue(Color color, Coordinate center, double radius) {
        super(color);

        Check.notNull(center);
        Check.condition(radius > 0.0);

        this.center = center;
        this.radius = radius;
    }

    public Coordinate getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
