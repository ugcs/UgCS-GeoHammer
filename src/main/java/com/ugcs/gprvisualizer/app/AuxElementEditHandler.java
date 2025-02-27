package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.thecoldwine.sigrun.common.ext.GprFile;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;
import com.ugcs.gprvisualizer.app.auxcontrol.ClickPlace;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;

public class AuxElementEditHandler extends BaseObjectImpl {

	private static final Logger log = LoggerFactory.getLogger(AuxElementEditHandler.class);

	private BaseObject mouseInput;

	private final Button createMarkButton = ResourceImageHolder.setButtonImage(
			ResourceImageHolder.ADD_MARK, new Button());

	private final Button removeSelectedMarkButton = ResourceImageHolder.setButtonImage(
			ResourceImageHolder.DELETE, new Button());

	private final Button removeAllMarksButton = ResourceImageHolder.setButtonImage(
			ResourceImageHolder.DELETE_ALL, new Button());

	public AuxElementEditHandler(Model model) {
		this.model = model;
		initButtons();
	}

	public List<Node> getRightPanelTools() {
		return Arrays.asList(createMarkButton, removeSelectedMarkButton, removeAllMarksButton);
	}
	
	protected void initButtons() {
		createMarkButton.setTooltip(new Tooltip("Create mark"));
		removeSelectedMarkButton.setTooltip(new Tooltip("Delete selected element"));
		removeAllMarksButton.setTooltip(new Tooltip("Delete all additional elements"));

		createMarkButton.setOnAction(this::createMark);
		removeSelectedMarkButton.setOnAction(this::removeSelectedMark);
		removeAllMarksButton.setOnAction(this::removeAllMarks);
	}

	private void createMark(ActionEvent event) {
		SgyFile file = model.getCurrentFile();
		if (file == null) {
			return;
		}

		ClickPlace selectedTrace = model.getSelectedTrace(file);
		if (selectedTrace == null) {
			return; // no trace selected in a current file
		}

		int traceIndex = file instanceof CsvFile
				? selectedTrace.getTrace().getIndexInFile()
				: selectedTrace.getTrace().getIndexInSet();

		int localTraceIndex = file.getOffset().globalToLocal(traceIndex);
		if (localTraceIndex < 0 || localTraceIndex >= file.getTraces().size()) {
			log.warn("Marker trace outside of the current file");
			return;
		}
		FoundPlace mark = new FoundPlace(
				file.getTraces().get(localTraceIndex),
				file.getOffset(),
				model);
		mark.setSelected(true);

		// clear current selection in file
		selectMark(file, null);

		file.getAuxElements().add(mark);
		file.setUnsaved(true);

		model.clearSelectedTrace(file);
		model.updateAuxElements();
		model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
	}

	private void removeSelectedMark(ActionEvent event) {
		SgyFile file = model.getCurrentFile();
		if (file == null) {
			return;
		}

		BaseObject firstSelected = file.getAuxElements().stream()
				.filter(x -> x instanceof FoundPlace)
				.filter(BaseObject::isSelected)
				.findFirst()
				.orElse(null);

		if (firstSelected instanceof FoundPlace mark) {
			if (file instanceof CsvFile csvFile) {
				model.getChart(csvFile).ifPresent(lineChart ->
						lineChart.removeFlag(mark)
				);
			}

			file.getAuxElements().remove(mark);
			file.setUnsaved(true);

			model.updateAuxElements();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		}
	}

	private void removeAllMarks(ActionEvent event) {
		SgyFile file = model.getCurrentFile();
		if (file == null) {
			return;
		}
		if (!confirmMarksRemoval()) {
			return;
		}

		if (file instanceof CsvFile csvFile) {
			model.getChart(csvFile).ifPresent(SensorLineChart::clearFlags);
		}

		file.getAuxElements().clear();
		file.setUnsaved(true);

		model.updateAuxElements();
		model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
	}

	private boolean confirmMarksRemoval() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Remove all additional elements");
		alert.setContentText("Confirm to clear makup");

		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK;
	}

	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
		BaseObject element = lookupElement(localPoint, profField);
		if (element != null) {
			if (element instanceof FoundPlace mark) {
				selectMark(profField, mark);
			} else {
				// select on a drag period
				element.setSelected(true);
			}
			element.mousePressHandle(localPoint, profField);
		}
		mouseInput = element;
		return mouseInput != null;
	}

	private List<FoundPlace> getMarks(ScrollableData profField) {
		if (profField instanceof SensorLineChart lineChart) {
			return lineChart.getFlags();
		}
		if (profField instanceof GPRChart gprChart) {
			return gprChart.getAuxElements().stream()
					.filter(x -> x instanceof FoundPlace)
					.map(x -> (FoundPlace)x)
					.toList();
		}
		return List.of();
	}

	private void selectMark(ScrollableData profField, FoundPlace mark) {
		if (profField instanceof SensorLineChart lineChart) {
			lineChart.selectFlag(mark);
		}
		if (profField instanceof GPRChart gprChart) {
			List<FoundPlace> marks = getMarks(gprChart);
			marks.forEach(m ->
					m.setSelected(Objects.equals(m, mark)));
		}
	}

	private void selectMark(SgyFile file, FoundPlace mark) {
		if (file instanceof CsvFile csvFile) {
			selectMark(model.getChart(csvFile).orElse(null), mark);
		}
		if (file instanceof GprFile gprFile) {
			selectMark(model.getProfileField(gprFile), mark);
		}
	}

	private BaseObject lookupElement(Point2D localPoint, ScrollableData profField) {
		if (profField instanceof GPRChart gprChart) {
			for (BaseObject element : gprChart.getAuxElements()) {
				if (element.isPointInside(localPoint, profField)) {
					return element;
				}
			}
		}
		return null;
	}

	private boolean isAboveElement(Point2D localPoint, ScrollableData profField) {
		return lookupElement(localPoint, profField) != null;
	}

	@Override
	public boolean mouseMoveHandle(Point2D localPoint, ScrollableData profField) {
		if (mouseInput != null) {
			mouseInput.mouseMoveHandle(localPoint, profField);
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
			return true;
		} else {
			if (isAboveElement(localPoint, profField)) {
				profField.setCursor(Cursor.HAND);
			} else {
				profField.setCursor(Cursor.DEFAULT);
			}			
		}
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point2D localPoint, ScrollableData profField) {
		if (mouseInput != null) {
			mouseInput.mouseReleaseHandle(localPoint, profField);
			if (!(mouseInput instanceof FoundPlace)) {
				mouseInput.setSelected(false);
			}
			mouseInput = null;

			profField.setCursor(Cursor.DEFAULT);
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
			return true;
		}
		return false;
	}
}