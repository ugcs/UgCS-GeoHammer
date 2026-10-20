package com.ugcs.geohammer.model.event;

import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.util.Check;

public class FilterAppliedEvent extends BaseEvent {

    private final SgyFile file;

    private final FilterType filterType;

    public FilterAppliedEvent(Object source, SgyFile file, FilterType filterType) {
        super(source);
        Check.notNull(file);
        Check.notNull(filterType);
        this.file = file;
        this.filterType = filterType;
    }

    public SgyFile getFile() {
        return file;
    }

    public FilterType getFilterType() {
        return filterType;
    }
}
