package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import javafx.geometry.Point2D;

import java.awt.Rectangle;

public abstract class ScrollableData {

    protected int startSample = 0;
    protected Rectangle mainRectRect = new Rectangle();
    private double realAspect = 0.5;
    private int middleTrace;
    private double vertScale = 1.0;

    private int zoom = 1;
    public static final double ZOOM_A = 1.2;

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = Math.clamp(zoom, 1, 100);
        vertScale = Math.pow(ZOOM_A, zoom);
    }

    public final int getMiddleTrace() {
        return middleTrace;
    }

    public void setMiddleTrace(int selectedTrace) {
        this.middleTrace = Math.clamp(selectedTrace, 0,
                        Math.max(0, getTracesCount() - 1));
    }

    public TraceSample screenToTraceSample(Point2D point) {

        int trace = middleTrace + (int) (-1 + (point.getX()) / getHScale());
        int sample = getStartSample() + (int) ((point.getY() - getTopMargin()) / getVScale());

        return new TraceSample(trace, sample);
    }

    public int sampleToScreen(int sample) {
        return (int) ((sample - getStartSample()) * getVScale() + getTopMargin());
    }

    public Point2D traceSampleToScreen(TraceSample ts) {
        return new Point2D(traceToScreen(ts.getTrace()), sampleToScreen(ts.getSample()));
    }

    public Point2D traceSampleToScreenCenter(TraceSample ts) {
        return new Point2D(
            traceToScreen(ts.getTrace()) + (int) (getHScale() / 2),
            sampleToScreen(ts.getSample()) + (int) (getVScale() / 2));
    }

    public abstract int getVisibleNumberOfTrace();

    public abstract int getTracesCount();

    public void setRealAspect(double realAspect) {
        this.realAspect = realAspect;
    }

    public double getRealAspect() {
        return realAspect;
    }

    public double getVScale() {
        return vertScale;
    }

    public double getHScale() {
        return vertScale * realAspect;
    }

    public int traceToScreen(int trace) {
        return (int) ((trace - middleTrace) * getHScale());
    }

    public final int getStartSample() {
        return startSample;
    }

    protected int getTopMargin() {
        return mainRectRect.y;
    }

    protected void clear() {
        setZoom(1);
    }
}