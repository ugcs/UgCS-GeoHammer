package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum SweepTypeCode {
    UNDEFINED((short) 0, "Undefined"),
    LINEAR((short) 1, "Linear"),
    PARABOLIC((short) 2, "Parabolic"),
    EXPONENTIAL((short) 3, "Exponential"),
    OTHER((short) 4, "Other");

    private final short code;
    private final String description;

    SweepTypeCode(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SweepTypeCode create(short code) {
        switch (code) {
            case 0:
                return UNDEFINED;
            case 1:
                return LINEAR;
            case 2:
                return PARABOLIC;
            case 3:
                return EXPONENTIAL;
            case 4:
                return OTHER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
