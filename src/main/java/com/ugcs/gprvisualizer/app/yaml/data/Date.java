package com.ugcs.gprvisualizer.app.yaml.data;

public class Date extends DateTime {
    
    private Source source;

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public enum Source {
        Column,
        FileName
    }
}