package com.ugcs.geohammer.service.magnetics;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record GridMicroLevelingResult(List<@Nullable Number> values, List<@Nullable Number> corrections) {
    public GridMicroLevelingResult {
        values = Collections.unmodifiableList(new ArrayList<>(values));
        corrections = Collections.unmodifiableList(new ArrayList<>(corrections));
    }
}
