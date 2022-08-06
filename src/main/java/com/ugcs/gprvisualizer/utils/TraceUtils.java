package com.ugcs.gprvisualizer.utils;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.Trace;

import java.util.List;

public class TraceUtils {

    public static int findNearestTraceIndex(List<Trace> traces, LatLon ll) {

        int resultIndex = 0;
        double mindst = ll.getDistance(traces.get(0).getLatLon());

        for (int i = 0; i < traces.size(); i++) {

            double dst = ll.getDistance(traces.get(i).getLatLon());
            if (dst < mindst) {
                resultIndex = i;

                mindst = dst;
            }

        }

        return resultIndex;
    }
}
