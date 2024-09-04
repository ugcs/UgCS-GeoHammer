package com.ugcs.gprvisualizer.app.parcers;

import java.util.ArrayList;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;

public class GeoData extends GeoCoordinates {

    public enum Semantic {

        LINE("Line"),
        ALTITUDE_AGL("Altitude AGL"),
        TMI("TMI");

        private String name;

        Semantic(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isFit(Trace trace) {
        return new LatLon(getLatitude(), getLongitude()).equals(trace.getLatLon());
    }

    public void setLine(int lineNumber) {
        setSensorValue(Semantic.LINE.name, lineNumber);
    }

    public SensorValue getLine() {
        return getSensorValue(Semantic.LINE);    
    }

    public SensorValue getSensorValue(Semantic semantic) {
        return getSensorValue(semantic.name);
    }

    public SensorValue getSensorValue(String semantic) {
        SensorValue result = null;
        for(SensorValue sensorValue : sensorValues) {
            if (semantic.equals(sensorValue.semantic())) {
                result = sensorValue;
                break;
            }
        }
        return result;
    }

    public void setSensorValue(String semantic, Number value) {
        for(SensorValue sensorValue : sensorValues) {
            if (semantic.equals(sensorValue.semantic())) {
                sensorValues.add(sensorValue.withValue(value));
                sensorValues.remove(sensorValue);
                break;
            }
        }
    }

    public void undoSensorValue(String semantic) {
        for(SensorValue sensorValue : sensorValues) {
            if (semantic.equals(sensorValue.semantic())) {
                sensorValues.add(sensorValue.withValue(sensorValue.originalData()));
                sensorValues.remove(sensorValue);
                break;
            }
        }
    }
}
