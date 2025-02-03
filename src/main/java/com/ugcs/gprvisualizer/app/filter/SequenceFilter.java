package com.ugcs.gprvisualizer.app.filter;

import java.util.List;

public interface SequenceFilter {

    List<Number> apply(List<Number> values);
}
