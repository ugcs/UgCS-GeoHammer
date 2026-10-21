package com.ugcs.geohammer.map.layer;

import java.awt.Graphics2D;

import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.model.SelectedTrace;
import com.ugcs.geohammer.model.SelectionType;
import com.ugcs.geohammer.model.element.BaseObject;
import com.ugcs.geohammer.model.element.ClickPlace;
import com.ugcs.geohammer.model.element.FoundPlace;
import com.ugcs.geohammer.model.Model;

import javafx.geometry.Point2D;

public class FoundTracesLayer implements Layer {

	private final Model model;

	public FoundTracesLayer(Model model) {
		this.model = model;
	}

	@Override
	public void draw(Graphics2D g2, MapField fixedField) {
		for (BaseObject bo : model.getAuxElements()) {
			if (isCollapsed(bo)) {
				continue;
			}
			bo.drawOnMap(g2, fixedField);
		}

		for (SelectedTrace selected : model.getSelectedTraces()) {
			if (selected.selectionType() != SelectionType.USER) {
				continue;
			}
			if (model.isCollapsed(selected.trace().getFile())) {
				continue;
			}
			ClickPlace clickPlace = new ClickPlace(selected);
			clickPlace.drawOnMap(g2, fixedField);
		}
	}

	@Override
	public boolean mousePressed(Point2D point) {
		for(BaseObject bo : model.getAuxElements()) {
			if (isCollapsed(bo)) {
				continue;
			}
			if(bo.mousePressHandle(point, model.getMapField())) {
				return true;
			}			
		}
		return false;
	}

	private boolean isCollapsed(BaseObject element) {
		return element instanceof FoundPlace flag
				&& model.isCollapsed(flag.getTrace().getFile());
	}
}
