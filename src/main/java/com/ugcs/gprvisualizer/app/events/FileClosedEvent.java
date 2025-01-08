package com.ugcs.gprvisualizer.app.events;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.auxcontrol.RemoveFileButton;
import com.ugcs.gprvisualizer.event.BaseEvent;
import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class FileClosedEvent extends FileOpenedEvent {

    private final SgyFile sgyFile;

    public FileClosedEvent(Object source, List<@NotNull File> files, SgyFile sgyFile) {
        super(source, files, true);
        this.sgyFile = sgyFile;
    }

    public SgyFile getSgyFile() {
        return sgyFile;
    }
}
