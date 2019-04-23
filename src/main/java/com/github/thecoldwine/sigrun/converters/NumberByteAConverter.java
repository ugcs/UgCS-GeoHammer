package com.github.thecoldwine.sigrun.converters;

@SuppressWarnings("WeakerAccess")
public final class NumberByteAConverter {
    public static byte[] intToByteA(int source) {
        byte[] destination = new byte[Integer.SIZE / 8];

        return writeIntToByteA(source, destination, 0);
    }

    public static byte[] writeIntToByteA(int source, byte[] destination, int offset) {
        if (destination == null)
            throw new IllegalArgumentException("Destination array cannot be null");

        if (offset + (Integer.SIZE / 8) <= destination.length) {
            destination[offset++] = (byte) ((source >> 24) & 0xFF);
            destination[offset++] = (byte) ((source >> 16) & 0xFF);
            destination[offset++] = (byte) ((source >> 8) & 0xFF);
            destination[offset] = (byte) (source & 0xFF);

            return destination;
        } else {
            throw new IllegalArgumentException("Offset is too big for destination array");
        }
    }

    public static byte[] shortToByteA(short source) {
        byte[] destination = new byte[Short.SIZE / 8];

        return writeShortToByteA(source, destination, 0);
    }

    public static byte[] writeShortToByteA(short source, byte[] destination, int offset) {
        if (destination == null)
            throw new IllegalArgumentException("Destination array cannot be null");

        if (offset + (Short.SIZE / 8) <= destination.length) {
            destination[offset++] = (byte) ((source >> 8) & 0xFF);
            destination[offset] = (byte) (source & 0xFF);

            return destination;
        } else {
            throw new IllegalArgumentException("Offset is too big for destination array");
        }
    }
}