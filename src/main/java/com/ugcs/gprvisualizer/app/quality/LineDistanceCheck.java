package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.utils.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineDistanceCheck implements QualityCheck {

    private static final double MIN_WIDTH = 0.15;
    private static final double MIN_LENGTH = 0.15;
    private static final double DISTANCE_THRESHOLD = MIN_LENGTH / 2;

    private final double max;
    private final double tolerance;
    private final double width;

    public LineDistanceCheck(double max, double tolerance, double width) {
        this.max = max;
        this.tolerance = tolerance;
        this.width = Math.max(width, MIN_WIDTH);
    }

    @Override
    public List<QualityIssue> check(List<GeoData> values) {
        if (values == null) {
            return List.of();
        }

        // compute line components
        LineSchema lineSchema = new LineSchema(values);

        List<QualityIssue> issues = new ArrayList<>();
        for (Map.Entry<Integer, Range> e : lineSchema.getRanges().entrySet()) {
            Integer lineIndex = e.getKey();
            Integer previousLineIndex = lineSchema.getPreviousLineIndex(lineIndex);
            if (previousLineIndex == null) {
                continue;
            }
            LineComponents previousLine = lineSchema.getComponents().get(previousLineIndex);
            Range lineRange = e.getValue();

            LatLon issueFrom = null;
            LatLon issueTo = null;
            for (int i = lineRange.getMin().intValue(); i <= lineRange.getMax().intValue(); i++) {
                GeoData value = values.get(i);
                LatLon latlon = new LatLon(value.getLatitude(), value.getLongitude());
                if (issueTo != null && latlon.getDistance(issueTo) < DISTANCE_THRESHOLD) {
                    // skip check by distance
                    continue;
                }

                double distanceToPreviousLine = previousLine.getDistanceToPoint(latlon);
                if (distanceToPreviousLine > max + tolerance) {
                    issueTo = latlon;
                    if (issueFrom == null) {
                        issueFrom = issueTo;
                    }
                } else {
                    // close current issue
                    if (issueTo != null) {
                        issues.add(createStripeIssue(issueFrom, issueTo, previousLine));
                        issueFrom = null;
                        issueTo = null;
                    }
                }
            }
            // close range
            if (issueTo != null) {
                issues.add(createStripeIssue(issueFrom, issueTo, previousLine));
            }
        }
        return issues;
    }

    private QualityIssue createStripeIssue(LatLon from, LatLon to, LineComponents referenceLine) {

        // shift segment in direction of a reference line
        Segment fromProjection = new Segment(
                from,
                referenceLine.projectToLine(from));
        Segment toProjection = new Segment(
                to,
                referenceLine.projectToLine(to));
        Segment segment = new Segment(
                fromProjection.getGeodeticMidpoint(),
                toProjection.getGeodeticMidpoint());

        if (segment.getLength() < MIN_LENGTH) {
            segment = segment.expand(MIN_LENGTH, referenceLine.getDirection());
        }

        return new StripeQualityIssue(
                QualityColors.LINE_DISTANCE,
                segment,
                width
        );
    }
}
