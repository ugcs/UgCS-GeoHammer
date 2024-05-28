package com.ugcs.gprvisualizer.app.parcers;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;

public class GeoData extends GeoCoordinates {

    private final List<SensorValue> sensorValues;
    
    private final int lineNumber;

    public GeoData(int lineNumber, List<SensorValue> sensorValues, GeoCoordinates geoCoordinates) {
        super(geoCoordinates.getLatitude(), geoCoordinates.getLongitude(), geoCoordinates.getAltitude(), geoCoordinates.getTimeInMs(), geoCoordinates.getTraceNumber(), geoCoordinates.getDateTime());
        this.sensorValues = sensorValues;
        this.lineNumber = lineNumber;
    }

    public List<SensorValue> getSensorValues() {
        return sensorValues;
    }

    public boolean isFit(Trace trace) {
        return new LatLon(getLatitude(), getLongitude()).equals(trace.getLatLon());
    }

    public void setLine(int lineNumber) {
        for(SensorValue sensorValue : sensorValues) {
            if ("Line".equals(sensorValue.semantic())) {
                sensorValues.add(new SensorValue(sensorValue.semantic(), sensorValue.units(), lineNumber, sensorValue.originalData()));
                sensorValues.remove(sensorValue);
                break;
            }
        }
    }

    public SensorValue getLine() {
        SensorValue result = null;
        for(SensorValue sensorValue : sensorValues) {
            if ("Line".equals(sensorValue.semantic())) {
                result = sensorValue;
                break;
            }
        }
        return result;    
    }

    public int getLineNumber() {
        return lineNumber;
    }



}
