package com.ugcs.gprvisualizer.app.parcers;

public record SensorValue(String semantic, String units, Number data, Number originalData) {
    
    public SensorValue(String semantic, String units, Number data) {
        this(semantic, units, data, data);
    }

    public SensorValue(SensorValue sensorValue) {
        this(sensorValue.semantic(), sensorValue.units(), sensorValue.data(), sensorValue.originalData());
    }
}