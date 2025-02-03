package com.ugcs.gprvisualizer.app.parcers.exceptions;

import java.io.File;

public class CSVParsingException extends RuntimeException {

    private final File file;

    public CSVParsingException(File file, String message) {
        super(message);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
