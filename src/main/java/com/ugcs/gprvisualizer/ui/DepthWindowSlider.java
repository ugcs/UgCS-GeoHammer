package com.ugcs.gprvisualizer.ui;

import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.beans.value.ChangeListener;

public class DepthWindowSlider extends BaseSlider{

	public DepthWindowSlider(Settings settings, ChangeListener<Number> listenerExt) {
		super(settings, listenerExt);
		
		name = "window height";
		units = "samples";
	}

	@Override
	public int updateModel() {
		
		settings.hpage = (int)slider.getValue();
		return settings.hpage;
	}

	@Override
	public void updateUI() {
		slider.setMin(1);
		slider.setMax(100);
		slider.setValue(settings.hpage);
	}
}
