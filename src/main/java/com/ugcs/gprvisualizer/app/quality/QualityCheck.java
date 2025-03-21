package com.ugcs.gprvisualizer.app.quality;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;

import java.util.List;

public interface QualityCheck {

    List<QualityIssue> check(List<CsvFile> files);
}
