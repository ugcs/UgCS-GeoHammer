package com.github.thecoldwine.sigrun.converters;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by maksenov on 17/01/15.
 */
public class IEEEConverter implements SeismicValuesConverter {
    @Override
    public float[] convert(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes array cannot be null");
        }

        if ((bytes.length % FLOAT_SIZE) != 0) {
            throw new IllegalArgumentException("Byte array has wrong length");
        }

        float[] result = new float[bytes.length / FLOAT_SIZE];

        FloatBuffer floatBuffer = ByteBuffer.wrap(bytes).asFloatBuffer();

        for (int i = 0; i < result.length; i++) {
            result[i] = floatBuffer.get();
        }

        return result;
    }
}
