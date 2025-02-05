package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.app.parcers.GeoData;

import java.util.List;

public interface QualityCheck {

    List<QualityIssue> check(List<GeoData> values);
}
