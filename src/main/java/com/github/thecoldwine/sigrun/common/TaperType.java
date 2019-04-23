package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum TaperType {
    UNDEFINED((short) 0, "Undefined"),
    LINEAR((short) 1, "Linear"),
    COS_2((short) 2, "Cos^2"),
    OTHER((short) 3, "Other");

    private final String description;
    private final short code;

    TaperType(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public short getCode() {
        return code;
    }

    public static TaperType create(int code) {
        switch (code) {
            case 0:
                return UNDEFINED;
            case 1:
                return LINEAR;
            case 2:
                return COS_2;
            case 3:
                return OTHER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
