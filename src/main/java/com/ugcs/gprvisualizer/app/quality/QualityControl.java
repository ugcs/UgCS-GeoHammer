package com.ugcs.gprvisualizer.app.quality;

import com.ugcs.gprvisualizer.app.parcers.GeoData;

import java.util.ArrayList;
import java.util.List;

public class QualityControl {

    public List<QualityIssue> getQualityIssues(List<GeoData> values, List<QualityCheck> checks) {

        List<QualityIssue> result = new ArrayList<>();
        for (QualityCheck check : checks) {
            List<QualityIssue> issues = check.check(values);
            if (issues == null || issues.isEmpty()) {
                continue;
            }
            result.addAll(issues);
        }
        return result;
    }
}
