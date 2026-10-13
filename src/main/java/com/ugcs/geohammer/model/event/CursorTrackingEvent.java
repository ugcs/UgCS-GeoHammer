package com.ugcs.geohammer.model.event;

import com.ugcs.geohammer.model.TraceKey;
import org.jspecify.annotations.Nullable;

public class CursorTrackingEvent extends BaseEvent {

    private final @Nullable TraceKey trace;

    public CursorTrackingEvent(Object source, @Nullable TraceKey trace) {
        super(source);
        this.trace = trace;
    }

    public @Nullable TraceKey getTrace() {
        return trace;
    }
}
