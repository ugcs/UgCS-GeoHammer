package com.ugcs.geohammer.service.quality;

import com.ugcs.geohammer.util.Check;

public class QualityIssue {

    private final Type type;

    public QualityIssue(Type type) {
        Check.notNull(type);

        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ALTITUDE,
        LINE_DISTANCE,
        DATA
    }
}
