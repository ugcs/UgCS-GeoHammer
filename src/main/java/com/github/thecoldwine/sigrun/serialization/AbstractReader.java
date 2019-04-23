package com.github.thecoldwine.sigrun.serialization;

import com.github.thecoldwine.sigrun.common.ParseProgressListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by maksenov on 14/01/15.
 */
public abstract class AbstractReader {
    protected final Set<ParseProgressListener> listeners = new HashSet<ParseProgressListener>();

    protected synchronized void registerParseProgressListener(ParseProgressListener listener) {
        this.listeners.add(listener);
    }

    protected synchronized void unregisterParseProgressListener(ParseProgressListener listener) {
        this.listeners.remove(listener);
    }

    protected synchronized void fireProgressEvent(final long read) {
        for (ParseProgressListener listener : this.listeners) {
            listener.progress(read);
        }
    }
}
