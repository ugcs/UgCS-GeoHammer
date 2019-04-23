package com.ugcs.gprvisualizer.ui;

import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.beans.value.ChangeListener;

public class GainBottomSlider extends BaseSlider{

	public GainBottomSlider(Settings settings, ChangeListener<Number> listenerExt) {
		super(settings, listenerExt);
		name = "gain bottom";
		units = "%";
		tickUnits = 200;
	}

	@Override
	public int updateModel() {
		
		settings.bottomscale = (int)slider.getValue();
		return settings.bottomscale;
	}

	@Override
	public void updateUI() {
		slider.setDisable(settings.autogain);
		slider.setMin(1);
		slider.setMax(1000);
		slider.setValue(settings.bottomscale);
	}

}
