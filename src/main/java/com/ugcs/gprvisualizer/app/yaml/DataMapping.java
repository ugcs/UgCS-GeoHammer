package com.ugcs.gprvisualizer.app.yaml;

import com.ugcs.gprvisualizer.app.yaml.data.BaseData;
import com.ugcs.gprvisualizer.app.yaml.data.Date;
import com.ugcs.gprvisualizer.app.yaml.data.DateTime;

public class DataMapping {

    private BaseData latitude;
    private BaseData longitude;
    private BaseData altitude;
    private Date date;
    private DateTime time;
    private DateTime dateTime;
    private BaseData timestamp;
    private BaseData traceNumber;

    public BaseData getLatitude() {
        return latitude;
    }

    public void setLatitude(BaseData latitude) {
        this.latitude = latitude;
    }

    public BaseData getLongitude() {
        return longitude;
    }

    public void setLongitude(BaseData longitude) {
        this.longitude = longitude;
    }

    public BaseData getAltitude() {
        return altitude;
    }

    public void setAltitude(BaseData altitude) {
        this.altitude = altitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public BaseData getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BaseData timestamp) {
        this.timestamp = timestamp;
    }

    public BaseData getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(BaseData traceNumber) {
        this.traceNumber = traceNumber;
    }

}
