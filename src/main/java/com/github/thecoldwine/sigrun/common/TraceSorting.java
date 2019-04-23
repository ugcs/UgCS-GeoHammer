package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum TraceSorting {
    OTHER((short) -1, "Other"),
    UNKNOWN((short) 0, "Unknown"),
    AS_RECORDED((short) 1, "As recorded (no sorting)"),
    CDP_ENSEMBLE((short) 2, "CDP Ensemble"),
    SINGLE_FOLD((short) 3, "Single fold continuous profile"),
    HORIZONTALLY_STACKED((short) 4, "Horizontally stacked"),
    COMMON_SOURCE_POINT((short) 5, "Common source point"),
    COMMON_RECEIVER_POINT((short) 6, "Common receiver point"),
    COMMON_OFFSET_POINT((short) 7, "Common offset point"),
    COMMON_MID_POINT((short) 8, "Common mid-point"),
    COMMON_CONVERSION_POINT((short) 9, "Common conversion point");

    private final short code;
    private final String description;

    TraceSorting(short code, String description) {
        this.description = description;
        this.code = code;
    }


    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TraceSorting create(short code) {
        switch (code) {
            case -1:
                return OTHER;
            case 0:
                return UNKNOWN;
            case 1:
                return AS_RECORDED;
            case 2:
                return CDP_ENSEMBLE;
            case 3:
                return SINGLE_FOLD;
            case 4:
                return HORIZONTALLY_STACKED;
            case 5:
                return COMMON_SOURCE_POINT;
            case 6:
                return COMMON_RECEIVER_POINT;
            case 7:
                return COMMON_OFFSET_POINT;
            case 8:
                return COMMON_MID_POINT;
            case 9:
                return COMMON_CONVERSION_POINT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
