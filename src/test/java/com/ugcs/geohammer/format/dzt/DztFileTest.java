package com.ugcs.geohammer.format.dzt;

import com.ugcs.geohammer.format.gpr.Trace;
import com.ugcs.geohammer.format.meta.Meta;
import com.ugcs.geohammer.format.meta.TraceGeoData;
import com.ugcs.geohammer.model.IndexRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DztFileTest {

    private static final int NUM_CHANNELS = 2;

    private static final int NUM_TRACES = 8;

    private static final int NUM_SAMPLES = 6;

    private static final int BITS = 16;

    private static final int BYTES_PER_SAMPLE = BITS / 8;

    // 16-bit samples are stored with this offset
    private static final int SAMPLE_OFFSET = 32767;

    private static final short HEADER_TAG = 0x00ff;

    private static final int REMOVED_TRACES = 3;

    @TempDir
    Path directory;

    @Test
    void open_syntheticFile_readsAllChannelsAndTraces() throws IOException {
        DztFile file = openSource();

        assertEquals(NUM_CHANNELS, file.numChannels());
        for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
            assertTraces(file, channelIndex, 0, NUM_TRACES, 0, NUM_SAMPLES);
        }
    }

    @Test
    void save_afterFirstTracesRemoved_writesKeptTraces() throws IOException {
        DztFile source = openSource();
        removeFirstTraces(source, REMOVED_TRACES);

        File target = directory.resolve("saved.dzt").toFile();
        source.save(target);

        DztFile saved = open(target);
        int numKept = NUM_TRACES - REMOVED_TRACES;
        for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
            assertTraces(saved, channelIndex, REMOVED_TRACES, numKept, 0, NUM_SAMPLES);
        }
        assertDztIndices(target, numKept, NUM_SAMPLES);
        assertDzgIndices(target, numKept);
        assertPositions(source, saved, REMOVED_TRACES, numKept);
    }

    @Test
    void saveRange_afterFirstTracesRemoved_writesRangeTraces() throws IOException {
        DztFile source = openSource();
        removeFirstTraces(source, REMOVED_TRACES);

        // meta rows 2..3 are source traces 5..6
        IndexRange range = new IndexRange(2, 4);
        File target = directory.resolve("line.dzt").toFile();
        source.save(target, range);

        DztFile saved = open(target);
        int firstTraceIndex = REMOVED_TRACES + range.from();
        for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
            assertTraces(saved, channelIndex, firstTraceIndex, range.size(), 0, NUM_SAMPLES);
        }
        assertDztIndices(target, range.size(), NUM_SAMPLES);
        assertDzgIndices(target, range.size());
        assertPositions(source, saved, firstTraceIndex, range.size());
    }

    @Test
    void save_afterSampleCrop_cropsAllChannels() throws IOException {
        DztFile source = openSource();
        Meta meta = source.getMeta();
        assertNotNull(meta);

        IndexRange sampleRange = new IndexRange(2, 5);
        meta.setSampleRange(sampleRange);
        source.syncMeta();

        File target = directory.resolve("cropped.dzt").toFile();
        source.save(target);

        DztFile saved = open(target);
        for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
            assertTraces(saved, channelIndex, 0, NUM_TRACES, sampleRange.from(), sampleRange.size());
        }
    }

    private DztFile openSource() throws IOException {
        File file = directory.resolve("source.dzt").toFile();
        writeDzt(file);
        writeDzg(directory.resolve("source.dzg"));
        return open(file);
    }

    private static DztFile open(File file) throws IOException {
        DztFile dztFile = new DztFile();
        dztFile.open(file);
        return dztFile;
    }

    private static void removeFirstTraces(DztFile file, int numTraces) {
        Meta meta = file.getMeta();
        assertNotNull(meta);

        List<TraceGeoData> values = meta.getValues();
        meta.setValues(new ArrayList<>(values.subList(numTraces, values.size())));
    }

    private static int sampleValue(int channelIndex, int traceIndex, int sampleIndex) {
        return channelIndex * 1000 + traceIndex * 10 + sampleIndex;
    }

    private static int traceSize(int numSamples) {
        return BYTES_PER_SAMPLE * (numSamples + 1);
    }

    private static void writeDzt(File file) throws IOException {
        int dataSize = NUM_TRACES * NUM_CHANNELS * traceSize(NUM_SAMPLES);
        ByteBuffer buffer = ByteBuffer
                .allocate(NUM_CHANNELS * DztHeader.MINHEADSIZE + dataSize)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
            int headerStart = channelIndex * DztHeader.MINHEADSIZE;
            buffer.putShort(headerStart, HEADER_TAG);
            // rh_data >= MINHEADSIZE: data starts at rh_nchan * rh_data
            buffer.putShort(headerStart + 2, (short) DztHeader.MINHEADSIZE);
            buffer.putShort(headerStart + 4, (short) (NUM_SAMPLES + 1));
            buffer.putShort(headerStart + 6, (short) BITS);
            buffer.putShort(headerStart + 52, (short) NUM_CHANNELS);
        }

        buffer.position(NUM_CHANNELS * DztHeader.MINHEADSIZE);
        for (int traceIndex = 0; traceIndex < NUM_TRACES; traceIndex++) {
            for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
                buffer.putShort((short) (traceIndex + SAMPLE_OFFSET));
                for (int sampleIndex = 0; sampleIndex < NUM_SAMPLES; sampleIndex++) {
                    int value = sampleValue(channelIndex, traceIndex, sampleIndex);
                    buffer.putShort((short) (value + SAMPLE_OFFSET));
                }
            }
        }
        Files.write(file.toPath(), buffer.array());
    }

    private static void writeDzg(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int traceIndex = 0; traceIndex < NUM_TRACES; traceIndex++) {
            lines.add("$GSSIS," + traceIndex + ",0");
            lines.add(String.format(Locale.ROOT,
                    "$GPGGA,120000.00,%.4f,N,%.4f,E,1,08,1.0,10.0,M,0.0,M,,*00",
                    5630.0 + traceIndex, 2400.0 + traceIndex));
            lines.add("");
        }
        Files.write(path, lines);
    }

    private static void assertTraces(DztFile file, int channelIndex, int firstTraceIndex, int numTraces,
            int firstSampleIndex, int numSamples) {
        List<Trace> traces = file.getChannel(channelIndex).getTraces();
        assertEquals(numTraces, traces.size());
        for (int index = 0; index < numTraces; index++) {
            Trace trace = traces.get(index);
            assertEquals(numSamples, trace.numSamples());
            for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
                int expected = sampleValue(channelIndex, firstTraceIndex + index, firstSampleIndex + sampleIndex);
                assertEquals(expected, (int) trace.getSample(sampleIndex),
                        "channel " + channelIndex + ", trace " + index + ", sample " + sampleIndex);
            }
        }
    }

    // the first value of every saved trace is its index in the saved file
    private static void assertDztIndices(File file, int numTraces, int numSamples) throws IOException {
        int traceSize = traceSize(numSamples);
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_SAMPLE).order(ByteOrder.LITTLE_ENDIAN);
        try (FileChannel in = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            for (int index = 0; index < numTraces; index++) {
                for (int channelIndex = 0; channelIndex < NUM_CHANNELS; channelIndex++) {
                    long position = (long) NUM_CHANNELS * DztHeader.MINHEADSIZE
                            + (long) (index * NUM_CHANNELS + channelIndex) * traceSize;
                    buffer.clear();
                    in.read(buffer, position);
                    assertEquals(index, (buffer.getShort(0) & 0xFFFF) - SAMPLE_OFFSET);
                }
            }
        }
    }

    private static void assertDzgIndices(File file, int numTraces) throws IOException {
        Path dzgPath = file.toPath().resolveSibling(file.getName().replace(".dzt", ".dzg"));
        List<Integer> indices = new ArrayList<>();
        for (String line : Files.readAllLines(dzgPath)) {
            if (line.startsWith("$GSSIS")) {
                indices.add(Integer.parseInt(line.split(",")[1]));
            }
        }
        List<Integer> expected = new ArrayList<>();
        for (int index = 0; index < numTraces; index++) {
            expected.add(index);
        }
        assertEquals(expected, indices);
    }

    private static void assertPositions(DztFile source, DztFile saved, int firstTraceIndex, int numTraces) {
        List<Trace> sourceTraces = source.getChannel(0).getTraces();
        List<Trace> savedTraces = saved.getChannel(0).getTraces();
        for (int index = 0; index < numTraces; index++) {
            assertNotNull(savedTraces.get(index).getLatLon());
            assertEquals(sourceTraces.get(firstTraceIndex + index).getLatLon(), savedTraces.get(index).getLatLon(),
                    "position of trace " + index);
        }
    }
}
