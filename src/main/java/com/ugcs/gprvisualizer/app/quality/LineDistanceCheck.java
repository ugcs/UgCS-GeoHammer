package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SphericalMercator;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.utils.Range;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

            Range range = e.getValue();
            Range previousRange = lineSchema.getRanges().get(previousLineIndex);
            LineComponents previousLine = lineSchema.getComponents().get(previousLineIndex);
            // index of the previous line points
            // ordered by projection scalar relative to line origin point
            TreeMap<Double, LatLon> index = buildLineIndex(values, previousRange, previousLine);

            LatLon issueFrom = null;
            LatLon issueTo = null;
            for (int i = range.getMin().intValue(); i <= range.getMax().intValue(); i++) {
                GeoData value = values.get(i);
                LatLon latlon = new LatLon(value.getLatitude(), value.getLongitude());
                if (issueTo != null && latlon.getDistance(issueTo) < DISTANCE_THRESHOLD) {
                    // skip check by distance
                    continue;
                }

                LatLon nearest = getNearestToProjection(index, previousLine, latlon);
                boolean hasIssue = nearest != null
                        && nearest.getDistance(latlon) > max + tolerance;
                if (hasIssue) {
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

    private double getProjectionScalar(LineComponents line, Point2D point) {
        Point2D ab = point.subtract(line.getPoint());
        return ab.dotProduct(line.getDirection());
    }

    private TreeMap<Double, LatLon> buildLineIndex(List<GeoData> values, Range range, LineComponents line) {
        int from = range.getMin().intValue();
        int to = range.getMax().intValue();

        TreeMap<Double, LatLon> index = new TreeMap<>();
        for (int i = from; i <= to; i++) {
            GeoData value = values.get(i);
            LatLon point = new LatLon(value.getLatitude(), value.getLongitude());
            double d = getProjectionScalar(line, SphericalMercator.project(point));
            index.put(d, point);
        }
        return index;
    }

    private LatLon getNearestToProjection(TreeMap<Double, LatLon> index, LineComponents line, LatLon point) {
        double d = getProjectionScalar(line, SphericalMercator.project(point));

        LatLon exact = index.get(d);
        if (exact != null) {
            return exact;
        }

        // on the left and right from the point projection
        // are two candidates, select one with the min
        // distance to d
        Map.Entry<Double, LatLon> l = index.lowerEntry(d);
        Map.Entry<Double, LatLon> r = index.higherEntry(d);
        if (l != null) {
            if (r != null) {
                return Math.abs(d - l.getKey()) <= Math.abs(d - r.getKey())
                        ? l.getValue()
                        : r.getValue();
            } else {
                return l.getValue();
            }
        } else {
            return r != null ? r.getValue() : null;
        }
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
