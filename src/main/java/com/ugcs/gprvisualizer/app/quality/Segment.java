package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SphericalMercator;
import com.ugcs.gprvisualizer.utils.Check;
import javafx.geometry.Point2D;

public class Segment {

    private final LatLon from;

    private final LatLon to;

    public Segment(LatLon from, LatLon to) {
        Check.notNull(from);
        Check.notNull(to);

        this.from = from;
        this.to = to;
    }

    public LatLon getFrom() {
        return from;
    }

    public LatLon getTo() {
        return to;
    }

    public double getLength() {
        return from.getDistance(to);
    }

    public LatLon getGeodeticMidpoint() {
        // geodetic average of the segment endpoints
        return new LatLon(
                0.5 * (from.getLatDgr() + to.getLatDgr()),
                0.5 * (from.getLonDgr() + to.getLonDgr()));
    }

    public Segment expand(double newLength, Point2D defaultDirection) {
        // Lproj / L = k
        double f = SphericalMercator.scaleFactorAt(from.getLatDgr());
        double fInv = 1.0 / f;

        Point2D a = SphericalMercator.project(from);
        Point2D b = SphericalMercator.project(to);
        Point2D ab = b.subtract(a);

        // result
        Point2D a2;
        Point2D b2;

        // actual length in meters
        double l = fInv * ab.magnitude();
        if (l > 1e-6) {
            // growth factor of ab on both ends
            double k = 0.5 * (newLength + l) / l;

            a2 = b.add(ab.multiply(-k));
            b2 = a.add(ab.multiply(k));
        } else {
            // half of the targer length in a projected space
            double h = f * 0.5 * newLength;
            Point2D offset = defaultDirection.normalize().multiply(h);

            a2 = a.add(offset);
            b2 = a.subtract(offset);
        }

        return new Segment(
                SphericalMercator.restore(a2),
                SphericalMercator.restore(b2));
    }
}
