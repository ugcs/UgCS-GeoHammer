package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum GainTypeForInstruments {
    UNDEFINED((short) 0, "Undefined"),
    FIXED((short) 1, "Fixed"),
    BINARY((short) 2, "Binary"),
    FLOATING_POINT((short) 3, "Floating point"),
    OPTIONAL_USE((short) 4, "Optional use");

    private final short code;

    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    GainTypeForInstruments(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static GainTypeForInstruments create(short code) {
        switch (code) {
            case 0:
                return UNDEFINED;
            case 1:
                return FIXED;
            case 2:
                return BINARY;
            case 3:
                return FLOATING_POINT;
            default:
                return OPTIONAL_USE;
        }
    }
}