package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.auxcontrol.ConstPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.TraceUtils;

public class KmlToFlag implements Command {
    @Override
    public String getButtonText() {
        return "insert marks into SEG-Y file";
    }

    @Override
    public Change getChange() {
        return Change.justdraw;
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

                SgyFile sf = model.getSgyFileByTrace(traceIndex);

                FoundPlace rect = new FoundPlace(sf.getTraces().get(
                    sf.getOffset().globalToLocal(traceIndex)), sf.getOffset());

                sf.getAuxElements().add(rect);
                sf.setUnsaved(true);
            } );

        model.getAuxElements().removeIf(p -> p instanceof ConstPlace);

        model.updateAuxElements();

        model.setKmlToFlagAvailable(false);

        AppContext.notifyAll(new WhatChanged(Change.updateButtons));
    }
}
