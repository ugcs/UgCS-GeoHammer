package com.github.thecoldwine.sigrun.common;

import com.github.thecoldwine.sigrun.serialization.*;
import com.github.thecoldwine.sigrun.formats.SEGYRevision;

import java.io.FileInputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by maksenov on 15/01/15.
 */
public class SEGYStreamFactory {
    private final TextHeaderReader textHeaderReader;
    private final BinaryHeaderReader binaryHeaderReader;
    private final TraceHeaderReader traceHeaderReader;

    private SEGYStreamFactory(final Charset charset,
                              final BinaryHeaderFormat binaryHeaderFormat,
                              final TraceHeaderFormat traceHeaderFormat) {
        this.textHeaderReader = new TextHeaderReader(charset);
        this.binaryHeaderReader = new BinaryHeaderReader(binaryHeaderFormat);
        this.traceHeaderReader = new TraceHeaderReader(traceHeaderFormat);
    }

    public static SEGYStreamFactory create(final SEGYRevision revision) {
        return create(revision.charset(), revision.binaryHeaderFormat(), revision.traceHeaderFormat());
    }

    public static SEGYStreamFactory create(final Charset charset,
                                           final BinaryHeaderFormat binaryHeaderFormat,
                                           final TraceHeaderFormat traceHeaderFormat) {
        return new SEGYStreamFactory(charset, binaryHeaderFormat, traceHeaderFormat);
    }

    public SEGYStream makeStream(ReadableByteChannel chan) {
        return makeStream(chan, Collections.<ParseProgressListener>emptySet());
    }

    public SEGYStream makeStream(ReadableByteChannel chan, Collection<ParseProgressListener> listeners) {
        return new SEGYStream(chan, textHeaderReader, binaryHeaderReader, traceHeaderReader, listeners);
    }

    public SEGYStream makeStream(FileInputStream fileInputStream) {
        return makeStream(fileInputStream, Collections.<ParseProgressListener>emptySet());
    }

    public SEGYStream makeStream(FileInputStream fileInputStream, Collection<ParseProgressListener> listeners) {
        return new SEGYStream(fileInputStream.getChannel(), textHeaderReader, binaryHeaderReader, traceHeaderReader, listeners);
    }
}
