package com.ugcs.gprvisualizer.app.parcers;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;

public class GeoData extends GeoCoordinates {

    public enum Semantic {

        LINE("Line"),
        ALTITUDE_AGL("Altitude AGL");

        private String name;

        Semantic(String name) {
            this.name = name;
        }
    }

    private final List<SensorValue> sensorValues;
    
    /** 
     * Line number in the source file
     */
    private final int lineNumber;

    public GeoData(int lineNumber, List<SensorValue> sensorValues, GeoCoordinates geoCoordinates) {
        super(geoCoordinates.getLatitude(), geoCoordinates.getLongitude(), geoCoordinates.getAltitude(), geoCoordinates.getTimeInMs(), geoCoordinates.getTraceNumber(), geoCoordinates.getDateTime());
        this.sensorValues = sensorValues;
        this.lineNumber = lineNumber;
    }

    public GeoData(GeoData geoData) {
        super(geoData.getLatitude(), geoData.getLongitude(), geoData.getAltitude(), geoData.getTimeInMs(), geoData.getTraceNumber(), geoData.getDateTime());
        this.sensorValues = new ArrayList<>();
        for (SensorValue sensorValue : geoData.sensorValues) {
            sensorValues.add(new SensorValue(sensorValue));
        }
        this.lineNumber = geoData.lineNumber;
    }

    public List<SensorValue> getSensorValues() {
        return sensorValues;
    }

    public boolean isFit(Trace trace) {
        return new LatLon(getLatitude(), getLongitude()).equals(trace.getLatLon());
    }

    public void setLine(int lineNumber) {
        for(SensorValue sensorValue : sensorValues) {
            if (Semantic.LINE.name.equals(sensorValue.semantic())) {
                sensorValues.add(sensorValue.withValue(lineNumber));
                sensorValues.remove(sensorValue);
                break;
            }
        }
    }

    public SensorValue getLine() {
        return getSensorValue(Semantic.LINE);    
    }

    public SensorValue getSensorValue(Semantic semantic) {
        SensorValue result = null;
        for(SensorValue sensorValue : sensorValues) {
            if (semantic.name.equals(sensorValue.semantic())) {
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
