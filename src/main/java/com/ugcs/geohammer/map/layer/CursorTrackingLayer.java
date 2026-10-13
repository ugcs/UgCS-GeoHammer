package com.ugcs.geohammer.map.layer;

import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.math.GoogleCoordUtils;
import com.ugcs.geohammer.model.ActivationPolicy;
import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.model.Model;
import com.ugcs.geohammer.model.ToolNode;
import com.ugcs.geohammer.model.TraceKey;
import com.ugcs.geohammer.model.event.CursorTrackingEvent;
import com.ugcs.geohammer.model.event.FileClosedEvent;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.util.Traces;
import javafx.geometry.Point2D;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

@Component
public class CursorTrackingLayer extends BaseLayer {

    private static final double PICK_DISTANCE_PIXELS = 12;

    private static final Color CROSSHAIR_COLOR = new Color(0x00, 0xD9, 0xFF);

    private final Model model;

    private final ToggleButton trackingButton = new ToggleButton("Track cursor");

    private @Nullable TraceKey trackedTrace;

    public CursorTrackingLayer(Model model) {
        this.model = model;

        trackingButton.setTooltip(new Tooltip("Link map and line-chart cursor positions"));
        trackingButton.setOnAction(event -> {
            if (!trackingButton.isSelected()) {
                clearTrace();
            }
        });
    }

    @Override
    public void draw(Graphics2D g2, MapField field) {
        if (!trackingButton.isSelected() || trackedTrace == null) {
            return;
        }

        Point2D point = field.latLonToScreen(trackedTrace.getLatLon());
        int x = (int) Math.round(point.getX());
        int y = (int) Math.round(point.getY());
        g2.setColor(CROSSHAIR_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - 7, y - 7, 14, 14);
        g2.drawLine(x - 12, y, x + 12, y);
        g2.drawLine(x, y - 12, x, y + 12);
    }

    @Override
    public boolean mouseMove(Point2D point) {
        if (!trackingButton.isSelected()) {
            return false;
        }

        SgyFile file = model.getCurrentFile();
        LatLon center = model.getMapField().getSceneCenter();
        if (file == null || center == null) {
            clearTrace();
            return false;
        }

        LatLon location = model.getMapField().screenTolatLon(point);
        double limit = GoogleCoordUtils.getPixelSize(center, model.getMapField().getZoom()) * PICK_DISTANCE_PIXELS;
        Traces.findNearestTrace(file, location, limit).ifPresentOrElse(this::trackTrace, this::clearTrace);
        return false;
    }

    public void clearTrace() {
        if (trackedTrace == null) {
            return;
        }

        SensorLineChart chart = getSensorChart(trackedTrace.getFile());
        if (chart != null) {
            chart.clearTrackingMarker();
        }
        trackedTrace = null;
        repaint();
    }

    @Override
    public List<ToolNode> getToolNodes() {
        return List.of(new ToolNode(trackingButton, ActivationPolicy.fileSelected()));
    }

    @EventListener
    private void cursorTracked(CursorTrackingEvent event) {
        if (trackingButton.isSelected()) {
            TraceKey trace = event.getTrace();
            if (trace != null) {
                trackTrace(trace);
            } else {
                clearTrace();
            }
        }
    }

    @EventListener
    private void fileSelected(FileSelectedEvent event) {
        clearTrace();
    }

    @EventListener
    private void fileClosed(FileClosedEvent event) {
        if (trackedTrace != null && Objects.equals(event.getFile(), trackedTrace.getFile())) {
            clearTrace();
        }
    }

    private void trackTrace(TraceKey trace) {
        if (!isCurrentSensorTrace(trace) || isTracked(trace)) {
            return;
        }

        clearTrace();
        trackedTrace = trace;
        SensorLineChart chart = getSensorChart(trace.getFile());
        if (chart != null) {
            chart.createTrackingMarker(trace.getIndex());
        }
        repaint();
    }

    private boolean isCurrentSensorTrace(TraceKey trace) {
        return Objects.equals(trace.getFile(), model.getCurrentFile())
                && getSensorChart(trace.getFile()) != null;
    }

    private boolean isTracked(TraceKey trace) {
        return trackedTrace != null
                && Objects.equals(trackedTrace.getFile(), trace.getFile())
                && trackedTrace.getIndex() == trace.getIndex();
    }

    private @Nullable SensorLineChart getSensorChart(SgyFile file) {
        return model.getChart(file) instanceof SensorLineChart sensorChart ? sensorChart : null;
    }

    private void repaint() {
        if (getRepaintListener() != null) {
            getRepaintListener().repaint();
        }
    }
}
