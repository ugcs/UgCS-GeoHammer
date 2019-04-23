package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum CoordinateUnitsCode {
    UNDEFINED((short) 0, "Undefined"),
    LENGTH((short) 1, "Meters or feet"),
    SECONDS_OF_ARC((short) 2, "Seconds of arc"),
    DECIMAL_DEGREES((short) 3, "Decimal degrees"),
    DMS((short) 4, "DMS");

    private final short code;
    private final String description;

    CoordinateUnitsCode(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CoordinateUnitsCode create(short code) {
    	//System.out.println(code);
        switch (code) {
            case 0:
                return UNDEFINED;
            case 1:
                return LENGTH;
            case 2:
                return SECONDS_OF_ARC;
            case 3:
                return DECIMAL_DEGREES;
            case 4:
                return DMS;
            default:
                //throw new IllegalArgumentException("Coordinate units code should be in range 0-4. Current value is " + code);
                return UNDEFINED;
        }
    }

    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
