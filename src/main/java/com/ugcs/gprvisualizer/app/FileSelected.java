package com.ugcs.gprvisualizer.app;

import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;

public class FileSelected extends WhatChanged {

    private final SgyFile selectedFile;

    public FileSelected(SgyFile file) {
        super(Change.fileSelected);
        this.selectedFile = file;
    }

    public FileSelected(List<SgyFile> files) {
        this(files.isEmpty() ? null : files.get(0));
    }

    public SgyFile getSelectedFile() {
        return selectedFile;
    }

}