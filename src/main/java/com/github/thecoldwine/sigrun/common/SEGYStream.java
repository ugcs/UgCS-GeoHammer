package com.github.thecoldwine.sigrun.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderReader;
import com.github.thecoldwine.sigrun.serialization.SEGYFormatException;
import com.github.thecoldwine.sigrun.serialization.TextHeaderReader;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderReader;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by maksenov on 15/01/15.
 */
public class SEGYStream implements Iterable<SeismicTrace>, Closeable {
    private static final Logger log = LoggerFactory.getLogger(SEGYStream.class);
    private final ReadableByteChannel chan;
    private final TraceHeaderReader traceHeaderReader;
    private TextHeader textHeader;
    private BinaryHeader binaryHeader;
    private long position = 0;
    private Set<ParseProgressListener> listeners = new HashSet<ParseProgressListener>();
    private SeismicTrace nextTrace;

    protected SEGYStream(ReadableByteChannel chan,
                         TextHeaderReader textHeaderReader,
                         BinaryHeaderReader binaryHeaderReader,
                         TraceHeaderReader traceHeaderReader,
                         Collection<ParseProgressListener> listeners) {
        this.position = 0;
        this.listeners.addAll(listeners);

        try {
            readTextHeader(chan, textHeaderReader);
            readBinaryHeader(chan, binaryHeaderReader);
        } catch (IOException e) {
            e.printStackTrace();// log.error(e.getMessage());
            throw new IllegalArgumentException("Looks like file is not a SEGY, aborting");
        }

        this.traceHeaderReader = traceHeaderReader;
        this.chan = chan;
    }

    private void readTextHeader(ReadableByteChannel chan, TextHeaderReader textHeaderReader) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(TextHeader.TEXT_HEADER_SIZE);

        if (chan.read(buf) != TextHeader.TEXT_HEADER_SIZE) {
            throw new SEGYFormatException("Unexpected end of file");
        }

        this.textHeader = textHeaderReader.read(buf.array());
        increasePosition(TextHeader.TEXT_HEADER_SIZE);

        assert this.position == TextHeader.TEXT_HEADER_SIZE;
    }

    private void readBinaryHeader(ReadableByteChannel chan, BinaryHeaderReader binaryHeaderReader) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(BinaryHeader.BIN_HEADER_LENGTH);

        if (chan.read(buf) != BinaryHeader.BIN_HEADER_LENGTH) {
            throw new SEGYFormatException("Unexpected end of file");
        }

        this.binaryHeader = binaryHeaderReader.read(buf.array());
        increasePosition(BinaryHeader.BIN_HEADER_LENGTH);

        assert this.position == (TextHeader.TEXT_HEADER_SIZE + BinaryHeader.BIN_HEADER_LENGTH);
    }

    private boolean tryReadTrace() {
        ByteBuffer traceBuf = ByteBuffer.allocate(TraceHeader.TRACE_HEADER_LENGTH);

        try {
            if (!chan.isOpen()) {
                return false;
            }

            if (chan.read(traceBuf) != TraceHeader.TRACE_HEADER_LENGTH) {
                log.info("Not enough bytes for next trace. Closing.");
                chan.close();
                return false;
            }

            final TraceHeader header = traceHeaderReader.read(traceBuf.array());
            final int dataLength = binaryHeader.getDataSampleCode().getSize() * header.getNumberOfSamples();

            ByteBuffer dataBuf = ByteBuffer.allocate(dataLength);

            if (chan.read(dataBuf) != dataLength) {
                log.info("Not enough bytes to read trace data. Looks like file is corrupted. Exiting.");
                chan.close();
                return false;
            }

            this.nextTrace = SeismicTrace.create(header, dataBuf.array(), binaryHeader.getDataSampleCode());
            increasePosition(TraceHeader.TRACE_HEADER_LENGTH + dataLength);

            return true;
        } catch (IOException e) {
        	
        	e.printStackTrace();
            //log.error(e.getLocalizedMessage());

            return false;
        }
    }

    private void increasePosition(long increment) {
        this.position += increment;
        notifyProgressListeners(this.position);
    }

    /**
     * Called when stream parses next portion of bytes.
     *
     * @param progress
     */
    private void notifyProgressListeners(long progress) {
        for (ParseProgressListener listener : listeners) {
            listener.progress(progress);
        }
    }

    public synchronized void registerListener(ParseProgressListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void unregisterListener(ParseProgressListener listener) {
        this.listeners.remove(listener);
    }

    public TextHeader getTextHeader() {
        return textHeader;
    }

    public BinaryHeader getBinaryHeader() {
        return binaryHeader;
    }

    public long getPosition() {
        return this.position;
    }

    @Override
    public Iterator<SeismicTrace> iterator() {
        return new SeismicTraceIterator(this);
    }

    private class SeismicTraceIterator implements Iterator<SeismicTrace> {
        private final SEGYStream parent;

        private SeismicTraceIterator(SEGYStream parent) {
            this.parent = parent;
        }

        @Override
        public boolean hasNext() {
            if (parent.nextTrace == null) {
                parent.tryReadTrace();
            }

            return parent.nextTrace != null;
        }

        @Override
        public SeismicTrace next() {
            if (hasNext()) {
                SeismicTrace result = parent.nextTrace;
                parent.nextTrace = null;

                return result;
            }

            return null;
        }

        /**
         * Not implemented.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Operation is not supported");
        }
    }

    public void close() throws IOException {
        this.chan.close();
    }
}
