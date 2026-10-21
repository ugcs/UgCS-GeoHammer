package com.ugcs.geohammer.service.quality;

import com.ugcs.geohammer.util.Check;
import org.locationtech.jts.geom.Polygon;

public class PolygonQualityIssue extends QualityIssue {

    private final Polygon polygon;

    public PolygonQualityIssue(Type type, Polygon polygon) {
        super(type);

        Check.notNull(polygon);

        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
    }
}
