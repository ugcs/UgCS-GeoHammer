package com.ugcs.gprvisualizer.app.commands;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.auxcontrol.ConstPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.event.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.utils.TraceUtils;

public class CancelKmlToFlag implements Command {
    @Override
    public String getButtonText() {
        return "X";
    }

    @Override
    public WhatChanged.Change getChange() {
        return WhatChanged.Change.justdraw;
    }

    @Override
    public void execute(SgyFile file, ProgressListener listener) {

        Model model = AppContext.model;
        model.getAuxElements().removeIf(p -> p instanceof ConstPlace);
        model.updateAuxElements();
        model.setKmlToFlagAvailable(false);

        model.publishEvent(new WhatChanged(this, WhatChanged.Change.updateButtons));
    }
}
