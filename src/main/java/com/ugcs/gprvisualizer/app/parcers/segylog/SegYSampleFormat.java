package com.ugcs.gprvisualizer.app.parcers.segylog;

public enum SegYSampleFormat {
    
    IbmFloat32BitFormat(1),
    Integer32BitFormat(2),
    Integer16BitFormat(3),
    Integer16BitWithGainFormat(4);

    private final int formatCode;

    SegYSampleFormat(int formatCode) {
        this.formatCode = formatCode;
    }

    public int getFormatCode() {
        return this.formatCode;
    }
}