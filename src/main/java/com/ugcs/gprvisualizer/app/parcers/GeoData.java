package com.ugcs.gprvisualizer.app.parcers;

import java.util.List;

public class GeoData extends GeoCoordinates {
    private final List<SensorValue> sensorValues;

    public GeoData(List<SensorValue> sensorValues, GeoCoordinates geoCoordinates) {
        super(geoCoordinates.getLatitude(), geoCoordinates.getLongitude(), geoCoordinates.getAltitude(), geoCoordinates.getTimeInMs(), geoCoordinates.getTraceNumber(), geoCoordinates.getDateTime());
        this.sensorValues = sensorValues;
    }

    public List<SensorValue> getSensorValues() {
        return sensorValues;
    }

}
