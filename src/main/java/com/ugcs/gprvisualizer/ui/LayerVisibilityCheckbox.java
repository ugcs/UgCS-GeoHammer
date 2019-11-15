package com.ugcs.gprvisualizer.ui;

import com.ugcs.gprvisualizer.gpr.Settings;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;

public class LayerVisibilityCheckbox extends BaseCheckBox{

	public LayerVisibilityCheckbox(ChangeListener<Boolean> listenerExt) {
		super(listenerExt, "Show amplitudemap");
		
		pos = Pos.CENTER_LEFT;
	}

	@Override
	public void updateUI() {
		
		checkBox.setSelected(true);
	}

	@Override
	public boolean updateModel() {
		
		return false;
	}

}
