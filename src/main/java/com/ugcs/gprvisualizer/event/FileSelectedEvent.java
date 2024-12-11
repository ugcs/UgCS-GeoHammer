package com.ugcs.gprvisualizer.event;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import java.util.List;

public class FileSelectedEvent extends BaseEvent {
    private final SgyFile file;

    public FileSelectedEvent(Object source, SgyFile file) {
        super(source);
        this.file = file;
    }

    public FileSelectedEvent(Object source, List<SgyFile> files) {
        super(source);
        this.file = files != null && files.size() > 0 ? files.get(0) : null;
    }

    public SgyFile getFile() {
        return file;
    }
}
