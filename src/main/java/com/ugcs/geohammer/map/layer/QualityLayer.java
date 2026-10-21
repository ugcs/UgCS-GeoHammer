package com.ugcs.geohammer.map.layer;

import com.ugcs.geohammer.map.MapColors;
import com.ugcs.geohammer.math.GoogleCoordUtils;
import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.service.quality.PointQualityIssue;
import com.ugcs.geohammer.service.quality.PolygonQualityIssue;
import com.ugcs.geohammer.service.quality.QualityIssue;
import com.ugcs.geohammer.service.quality.Spatial;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.model.event.WhatChanged;
import com.ugcs.geohammer.util.Check;
import javafx.geometry.Point2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;

@Component
public class QualityLayer extends BaseLayer {

    // in pixels
    private static final double MIN_POINT_RADIUS = 3.0;

    // in pixels
    private static final double POINT_RING_WIDTH = 2.0;

    // same-color stroke keeps polygons visible when zoomed out
    private static final BasicStroke POLYGON_STROKE
            = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // later types are drawn on top
    private static final List<QualityIssue.Type> DRAW_ORDER = List.of(
            QualityIssue.Type.LINE_DISTANCE,
            QualityIssue.Type.ALTITUDE,
            QualityIssue.Type.DATA);

    private List<QualityIssue> issues;

    public void setIssues(List<QualityIssue> issues) {
        this.issues = issues;
    }

    @EventListener
    private void onFileSelected(FileSelectedEvent event) {
        if (event.getFile() == null && issues != null && !issues.isEmpty()) {
            issues.clear();
        }
    }

    @EventListener
    private void somethingChanged(WhatChanged changed) {
        if (changed.isTraceCut()) {
            if (issues != null) {
                issues.clear();
            }
        }
    }

    @Override
    public void draw(Graphics2D g2, MapField field) {
        Check.notNull(g2);
        Check.notNull(field);

        if (!isActive()) {
            return;
        }

        if (issues == null || issues.isEmpty()) {
            return;
        }

        for (QualityIssue.Type type : DRAW_ORDER) {
            for (QualityIssue issue : issues) {
                if (issue.getType() != type) {
                    continue;
                }
                if (issue instanceof PointQualityIssue pointIssue) {
                    drawPointIssue(g2, field, pointIssue);
                }
                if (issue instanceof PolygonQualityIssue polygonIssue) {
                    drawPolygonIssue(g2, field, polygonIssue);
                }
            }
        }
    }

    private static Color getColor(QualityIssue.Type type) {
        return switch (type) {
            case ALTITUDE -> MapColors.ALTITUDE_ISSUE;
            case LINE_DISTANCE -> MapColors.LINE_DISTANCE_ISSUE;
            case DATA -> MapColors.DATA_ISSUE;
        };
    }

    private void drawPointIssue(Graphics2D g2, MapField field, PointQualityIssue pointIssue) {
        LatLon center = Spatial.toLatLon(pointIssue.getCenter());
        double pixelSize = GoogleCoordUtils.getPixelSize(center, field.getZoom());
        double r = Math.max(MIN_POINT_RADIUS, pointIssue.getRadius() / pixelSize);

        Point2D centerScreen = field.latLonToScreen(center);
        g2.setColor(MapColors.DATA_ISSUE_RING);
        g2.fill(createCircle(centerScreen, r + POINT_RING_WIDTH));
        g2.setColor(getColor(pointIssue.getType()));
        g2.fill(createCircle(centerScreen, r));
    }

    private static Shape createCircle(Point2D center, double r) {
        return new Ellipse2D.Double(center.getX() - r, center.getY() - r, 2 * r, 2 * r);
    }

    private void drawPolygonIssue(Graphics2D g2, MapField field, PolygonQualityIssue polygonIssue) {
        Polygon polygon = polygonIssue.getPolygon();
        // even-odd keeps holes empty whatever the ring orientation
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

        addRing(path, field, polygon.getExteriorRing());
        int numHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < numHoles; i++) {
            addRing(path, field, polygon.getInteriorRingN(i));
        }

        g2.setColor(getColor(polygonIssue.getType()));
        g2.fill(path);
        g2.setStroke(POLYGON_STROKE);
        g2.draw(path);
    }

    private void addRing(Path2D path, MapField field, LinearRing ring) {
        if (ring == null) {
            return;
        }

        Coordinate[] points = ring.getCoordinates();
        if (points.length == 0) {
            return;
        }
        Point2D point = field.latLonToScreen(Spatial.toLatLon(points[0]));
        path.moveTo(point.getX(), point.getY());
        for (int i = 1; i < points.length - 1; i++) {
            point = field.latLonToScreen(Spatial.toLatLon(points[i]));
            path.lineTo(point.getX(), point.getY());
        }
        path.closePath();
    }
}
