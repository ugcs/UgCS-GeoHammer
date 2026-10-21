package com.ugcs.geohammer.model.event;

public enum FilterType {
    LOW_PASS("lowpass"),
    RUNNING_MEDIAN("running_median"),
    TIME_LAG("timelag"),
    BACKGROUND_REMOVAL("background_removal"),
    FLATTEN_SURFACE("flatten_surface");

    private final String code;

    FilterType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
