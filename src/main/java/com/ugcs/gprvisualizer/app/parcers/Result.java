package com.ugcs.gprvisualizer.app.parcers;

public class Result {
    
    private int countOfReplacedLines;

    private int countOfLines;

    public int getCountOfReplacedLines() {
        return countOfReplacedLines;
    }

    public void setCountOfReplacedLines(int countOfReplacedLines) {
        this.countOfReplacedLines = countOfReplacedLines;
    }

    public int getCountOfLines() {
        return countOfLines;
    }

    public void setCountOfLines(int countOfLines) {
        this.countOfLines = countOfLines;
    }

    public void incrementCountOfReplacedLines() {
        this.countOfReplacedLines++;
    }

    public void incrementCountOfLines() {
        this.countOfLines++;
    }
}