package com.ugcs.geohammer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TraceUnitTest {

    private static final double EPSILON = 1e-9;
    private static final double DISPLAY_EPSILON = 1e-5;

    @Test
    void internationalMileIsOneMile() {
        assertEquals(1.0, TraceUnit.convert(1609.344, TraceUnit.MILES), EPSILON);
    }

    @Test
    void internationalFootIsOneFoot() {
        assertEquals(1.0, TraceUnit.convert(0.3048, TraceUnit.FEET), EPSILON);
    }

    @Test
    void tenKilometersInMiles() {
        assertEquals(6.21371, TraceUnit.convert(10_000, TraceUnit.MILES), DISPLAY_EPSILON);
    }
}
