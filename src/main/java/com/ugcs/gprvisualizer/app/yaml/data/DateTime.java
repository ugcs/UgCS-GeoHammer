package com.ugcs.gprvisualizer.app.yaml.data;

public class DateTime extends BaseData {

    private String format;
    private Type type;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        UTC,
        GPST
    }
    
}
