package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SphericalMercator;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.app.parcers.SensorValue;
import com.ugcs.gprvisualizer.math.DouglasPeucker;
import com.ugcs.gprvisualizer.utils.Range;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AltitudeCheck implements QualityCheck {

    private static final double MIN_WIDTH = 0.15;
    private static final double DISTANCE_THRESHOLD = MIN_WIDTH / 2;

    private final double max;
    private final double tolerance;
    private final double width;

    public AltitudeCheck(double max, double altitudeTolerance, double width) {
        this.max = max;
        this.tolerance = altitudeTolerance;
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
            Range lineRange = e.getValue();
            LineComponents lineComponents = lineSchema.getComponents().get(lineIndex);

            List<LatLon> issuePoints = new ArrayList<>();
            for (int i = lineRange.getMin().intValue(); i <= lineRange.getMax().intValue(); i++) {
                GeoData value = values.get(i);
                SensorValue sensorValue = value.getSensorValue(GeoData.Semantic.ALTITUDE_AGL);
                Double altitudeAgl = sensorValue.data() != null
                        ? sensorValue.data().doubleValue()
                        : null;
                if (altitudeAgl == null || altitudeAgl.isNaN()) {
                    continue;
                }

                LatLon latlon = new LatLon(value.getLatitude(), value.getLongitude());
                if (!issuePoints.isEmpty() && latlon.getDistance(issuePoints.getLast()) < DISTANCE_THRESHOLD) {
                    // skip check by distance
                    continue;
                }

                if (altitudeAgl > max + tolerance) {
                    issuePoints.add(latlon);
                } else {
                    if (!issuePoints.isEmpty()) {
                        issues.addAll(createStripeIssues(issuePoints, lineComponents.getDirection()));
                        issuePoints = new ArrayList<>();
                    }
                }
            }
            // close range
            if (!issuePoints.isEmpty()) {
                issues.addAll(createStripeIssues(issuePoints, lineComponents.getDirection()));
            }
        }
        return issues;
    }

    private List<QualityIssue> createStripeIssues(List<LatLon> points, Point2D defaultDirection) {
        if (points.size() > 2) {
            List<Point2D> projected = points.stream().map(SphericalMercator::project).toList();
            List<Integer> indices = DouglasPeucker.approximatePolyline(
                    projected, 0.5 * width, 2);
            List<LatLon> selected = new ArrayList<>(indices.size());
            for (int i : indices) {
                selected.add(points.get(i));
            }
            points = selected;
        }
        if (points.size() == 1) {
            points.add(points.getFirst());
        }
        List<QualityIssue> issues = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            issues.add(createStripeIssue(
                    points.get(i),
                    points.get(i + 1),
                    defaultDirection));
        }
        return issues;
    }

    private QualityIssue createStripeIssue(LatLon from, LatLon to, Point2D defaultDirection) {
        Segment segment = new Segment(from, to);

        double minLength = width;
        if (segment.getLength() < minLength) {
            segment = segment.expand(minLength, defaultDirection);
        }

        return new StripeQualityIssue(
                QualityColors.ALTITUDE,
                segment,
                width
        );
    }
}
