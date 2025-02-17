package com.ugcs.gprvisualizer.app.quality;

import com.ugcs.gprvisualizer.utils.Check;

import java.awt.Color;

public class QualityIssue {

    private final Color color;

    public QualityIssue(Color color) {
        Check.notNull(color);

        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
