package com.github.thecoldwine.sigrun.serialization;

public class FormatEntry {
    public final int posStart;
    public final int posEnd;


    private FormatEntry(int posStart, int posEnd) {
        this.posStart = posStart;
        this.posEnd = posEnd;
    }

    public static FormatEntry create(int posStart, int posEnd) {
        return new FormatEntry(posStart, posEnd);
    }
}
