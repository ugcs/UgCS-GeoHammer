package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum TraceIdentificationCode {
    OTHER((short) -1, "Other"),
    UNKNOWN((short) 0, "Unknown"),
    SEISMIC_DATA((short) 1, "Seismic data"),
    DEAD((short) 2, "Dead"),
    DUMMY((short) 3, "Dummy"),
    TIME_BREAK((short) 4, "Time break"),
    UPHOLE((short) 5, "Uphole"),
    SWEEP((short) 6, "Sweep"),
    TIMING((short) 7, "Timing"),
    WATERBREAK((short) 8, "Waterbreak"),
    NEAR_FIELD_GUN_SIGNATURE((short) 9, "Near-field gun signature"),
    FAR_FIELD_GUN_SIGNATURE((short) 10, "Far-field gun signature"),
    SEISMIC_PRESSURE_SENSOR((short) 11, "Seismic pressure sensor"),
    MCSS_VC((short) 12, "Multicomponent seismic sensor - Vertical component"),
    MCSS_CC((short) 13, "Multicomponent seismic sensor - Cross-line component"),
    MCSS_IC((short) 14, "Multicomponent seismic sensor - In-line component"),
    RMCSS_VC((short) 15, "Rotated multicomponent seismic sensor - Vertical component"),
    RMCSS_CC((short) 16, "Rotated multicomponent seismic sensor - Cross-line component"),
    RMCSS_IC((short) 17, "Rotated multicomponent seismic sensor - In-line component"),
    VIBRATOR_REACTION_MASS((short) 18, "Vibrator reaction mass"),
    VIBRATOR_BASEPLATE((short) 19, "Vibrator baseplate"),
    VIBRATOR_ESTIMATED_GROUND_FORCE((short) 20, "Vibrator estimated ground force"),
    VIBRATOR_REFERENCE((short) 21, "Vibrator reference"),
    TIME_VELOCITY_PAIRS((short) 22, "Time-velocity pairs"),
    CUSTOM();


    private final short code;

    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    TraceIdentificationCode() {
        this.code = -2;
        this.description = "Optional use";
    }

    TraceIdentificationCode(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TraceIdentificationCode create(short code) {
        /*  if (code < -1)
throw new IllegalArgumentException("By SEG Y standard code cannot be less than -1");*/

        switch (code) {
            case -1:
                return OTHER;
            case 0:
                return UNKNOWN;
            case 1:
                return SEISMIC_DATA;
            case 2:
                return DEAD;
            case 3:
                return DUMMY;
            case 4:
                return TIME_BREAK;
            case 5:
                return UPHOLE;
            case 6:
                return SWEEP;
            case 7:
                return TIMING;
            case 8:
                return WATERBREAK;
            case 9:
                return NEAR_FIELD_GUN_SIGNATURE;
            case 10:
                return FAR_FIELD_GUN_SIGNATURE;
            case 11:
                return SEISMIC_PRESSURE_SENSOR;
            case 12:
                return MCSS_VC;
            case 13:
                return MCSS_CC;
            case 14:
                return MCSS_IC;
            case 15:
                return RMCSS_VC;
            case 16:
                return RMCSS_CC;
            case 17:
                return RMCSS_IC;
            case 18:
                return VIBRATOR_REACTION_MASS;
            case 19:
                return VIBRATOR_BASEPLATE;
            case 20:
                return VIBRATOR_ESTIMATED_GROUND_FORCE;
            case 21:
                return VIBRATOR_REFERENCE;
            case 22:
                return TIME_VELOCITY_PAIRS;
            default:
                return CUSTOM;
        }
    }
}
