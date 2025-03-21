package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;

import java.util.ArrayList;
import java.util.List;

// isolated per-file quality check
public abstract class FileQualityCheck implements QualityCheck {

    @Override
    public List<QualityIssue> check(List<CsvFile> files) {
        if (files == null) {
            return List.of();
        }

        List<QualityIssue> issues = new ArrayList<>();
        for (CsvFile file : files) {
            issues.addAll(checkFile(file));
        }
        return issues;
    }

    public abstract List<QualityIssue> checkFile(CsvFile file);
}
