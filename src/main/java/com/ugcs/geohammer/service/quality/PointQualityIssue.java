package com.ugcs.geohammer.service.quality;

import com.ugcs.geohammer.util.Check;
import org.locationtech.jts.geom.Coordinate;

public class PointQualityIssue extends QualityIssue {

    private final Coordinate center;

    // in meters
    private final double radius;

    public PointQualityIssue(Type type, Coordinate center, double radius) {
        super(type);

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
