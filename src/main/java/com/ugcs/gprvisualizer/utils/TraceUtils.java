package com.ugcs.gprvisualizer.utils;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
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

    public static Trace findNearestTrace(List<Trace> traces, LatLon ll) {
        if (traces == null || traces.isEmpty()) {
            return null;
        }
        if (ll == null) {
            return null;
        }

        Trace result = traces.getFirst();
        double mindst = ll.getDistance(result.getLatLon());

        for (Trace current : traces) {
            double dst = ll.getDistance(current.getLatLon());
            if (dst < mindst) {
                result = current;
                mindst = dst;
            }
        }
        
        return result;
    }

    public static Trace findNearestTrace(List<Trace> traces, LatLon ll, double maxDistance) {
        Trace nearest = findNearestTrace(traces, ll);
        if (nearest == null) {
            return null;
        }
        double distance = nearest.getLatLon().getDistance(ll);
        return distance <= maxDistance ? nearest : null;
    }

    public static Trace findNearestTraceInFiles(Iterable<SgyFile> files, LatLon ll, double maxDistance) {
        Trace nearest = null;
        for (SgyFile file : files) {
            Trace nearestInFile = findNearestTrace(file.getTraces(), ll, maxDistance);
            if (nearestInFile == null) {
                continue;
            }

            if (nearest == null) {
                nearest = nearestInFile;
            } else {
                if (nearestInFile.getLatLon().getDistance(ll) < nearest.getLatLon().getDistance(ll)) {
                    nearest = nearestInFile;
                }
            }
        }
        return nearest;
    }
}
