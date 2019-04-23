package com.github.thecoldwine.sigrun.common;

import com.github.thecoldwine.sigrun.converters.SeismicValuesConverter;

/**
 * Created by maksenov on 15/01/15.
 */
public class SeismicTrace {
    private final TraceHeader header;
    private final float[] values;
    private final float min;
    private final float max;

    public static SeismicTrace create(final TraceHeader header, byte[] bytes, DataSample sample) {
        SeismicValuesConverter converter = ConverterFactory.getConverter(sample);
        final float[] values = converter.convert(bytes);

        float min = values[0];
        float max = values[0];

        for (int i = 1; i < values.length; i++) {
            min = Math.min(min, values[i]);
            max = Math.max(max, values[i]);
        }

        return new SeismicTrace(header, values, min, max);
    }

    private SeismicTrace(TraceHeader header, float[] values, float min, float max) {
        this.header = header;
        this.values = values;
        this.min = min;
        this.max = max;
    }

    public TraceHeader getHeader() {
        return header;
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    public float[] getValues() {
        return values;
    }
}