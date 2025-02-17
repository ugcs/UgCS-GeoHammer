package com.ugcs.gprvisualizer.app.quality;

import com.ugcs.gprvisualizer.utils.Check;

import java.awt.*;

public class StripeQualityIssue extends QualityIssue {

    private final Segment segment;

    // in meters
    private final double width;

    public StripeQualityIssue(Color color, Segment segment, double width) {
        super(color);

        Check.notNull(segment);
        Check.condition(width > 0.0);

        this.segment = segment;
        this.width = width;
    }

    public Segment getSegment() {
        return segment;
    }

    public double getWidth() {
        return width;
    }
}
