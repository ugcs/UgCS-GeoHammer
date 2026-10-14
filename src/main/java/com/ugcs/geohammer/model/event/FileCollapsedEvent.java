package com.ugcs.geohammer.model.event;

import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.util.Check;

public class FileCollapsedEvent extends BaseEvent {

    private final SgyFile file;

    private final boolean collapsed;

    public FileCollapsedEvent(Object source, SgyFile file, boolean collapsed) {
        super(source);
        this.file = Check.notNull(file);
        this.collapsed = collapsed;
    }

    public SgyFile getFile() {
        return file;
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
