package com.ugcs.gprvisualizer.draw;

import com.github.thecoldwine.sigrun.common.ext.GoogleCoordUtils;
import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.ugcs.gprvisualizer.app.quality.PointQualityIssue;
import com.ugcs.gprvisualizer.app.quality.QualityIssue;
import com.ugcs.gprvisualizer.app.quality.Segment;
import com.ugcs.gprvisualizer.app.quality.StripeQualityIssue;
import com.ugcs.gprvisualizer.event.FileSelectedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.utils.Check;
import javafx.geometry.Point2D;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;

@Component
public class QualityLayer extends BaseLayer {

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

        for (QualityIssue issue : issues) {
            if (issue instanceof PointQualityIssue pointIssue) {
                drawPointIssue(g2, field, pointIssue);
            }
            if (issue instanceof StripeQualityIssue stripeIssue) {
                drawStripeIssue(g2, field, stripeIssue);
            }
        }
    }

    private void drawPointIssue(Graphics2D g2, MapField field, PointQualityIssue pointIssue) {
        Point2D center = field.latLonToScreen(pointIssue.getCenter());

        double pixelSize = GoogleCoordUtils.getPixelSize(pointIssue.getCenter(), field.getZoom());
        double r = Math.max(1.5, pointIssue.getRadius() / pixelSize);

        g2.setColor(pointIssue.getColor());
        g2.fillOval(
                (int) (center.getX() - r),
                (int) (center.getY() - r),
                (int) (2 * r),
                (int) (2 * r));
    }

    private void drawStripeIssue(Graphics2D g2, MapField field, StripeQualityIssue stripeIssue) {
        Segment segment = stripeIssue.getSegment();
        Point2D p1 = field.latLonToScreen(segment.getFrom());
        Point2D p2 = field.latLonToScreen(segment.getTo());

        double pixelSize = GoogleCoordUtils.getPixelSize(
                segment.getFrom(),
                field.getZoom());
        double width = Math.max(1.0, stripeIssue.getWidth() / pixelSize);

        // direction vector
        Point2D d = p2.subtract(p1);

        // orthogonal to direction, scaled by half width
        Point2D n = new Point2D(-d.getY(), d.getX())
                .normalize()
                .multiply(0.5 * width);

        List<Point2D> points = Arrays.asList(
                p1.add(n),
                p1.subtract(n),
                p2.subtract(n),
                p2.add(n)
        );

        Path2D stripe = new Path2D.Double();
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            if (i == 0) {
                stripe.moveTo(point.getX(), point.getY());
            } else {
                stripe.lineTo(point.getX(), point.getY());
            }
        }
        stripe.closePath();

        g2.setColor(stripeIssue.getColor());
        g2.fill(stripe);
    }
}
