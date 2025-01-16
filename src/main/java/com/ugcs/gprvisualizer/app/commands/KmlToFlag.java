package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.auxcontrol.ConstPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.TraceUtils;

public class KmlToFlag implements Command {
    @Override
    public String getButtonText() {
        return "insert marks into SEG-Y file";
    }

    @Override
    public void execute(SgyFile file, ProgressListener listener) {

        Model model = AppContext.model;

        model.getAuxElements().stream()
            .filter(a -> a instanceof ConstPlace)
            .map(a -> (ConstPlace) a)
            .forEach(c -> {

                int traceIndex = TraceUtils.findNearestTraceIndex(
                    model.getGprTraces(), c.getLatLon());
                //TODO: fix to get all files
                SgyFile sf = null; //model.getSgyFileByTrace(traceIndex);

                FoundPlace rect = new FoundPlace(sf.getTraces().get(
                    sf.getOffset().globalToLocal(traceIndex)), sf.getOffset(), AppContext.model);

                sf.getAuxElements().add(rect);
                sf.setUnsaved(true);
            } );

        model.getAuxElements().removeIf(p -> p instanceof ConstPlace);

        model.updateAuxElements();

        model.setKmlToFlagAvailable(false);

        model.publishEvent(new WhatChanged(this, WhatChanged.Change.updateButtons));
    }
}
