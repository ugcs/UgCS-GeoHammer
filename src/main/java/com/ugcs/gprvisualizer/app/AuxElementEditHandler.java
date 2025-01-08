package com.ugcs.gprvisualizer.app;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;
import com.ugcs.gprvisualizer.app.auxcontrol.ClickPlace;
import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.CsvFile;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
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
import javafx.scene.layout.VBox;

@Component
public class AuxElementEditHandler extends BaseObjectImpl implements InitializingBean {

	@Autowired
	private Model model;

	private BaseObject selected;
	private BaseObject mouseInput;

	private final Button addFoundBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ADD_MARK, new Button());
	private final Button delBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.DELETE, new Button());
	private final Button clearBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.DELETE_ALL, new Button());

	@Override
	public void afterPropertiesSet() throws Exception {
		initButtons();
	}

	//@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
		
		boolean processed = false;
		if (model.getControls() != null) {
			processed = processPress(model.getControls(), localPoint, profField);
		}
		
		if (!processed && selected != null) {
			processed = selected.mousePressHandle(localPoint, profField);
			if (processed) {
				mouseInput = selected;
			}
		}
		
		if (!processed) {
			processed = processPress1(model.getAuxElements(),
					localPoint, profField);
		}
		
		if (!processed) {
			//deselect
			mouseInput = null;
			setSelected(null);
			model.setControls(null);
		}
		
		if (processed) {
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		}

		return processed;
	}
	
	public List<Node> getRightPanelTools() {
		return Arrays.asList(addFoundBtn, 
				delBtn, clearBtn);
	}
	
	protected void initButtons() {
		addFoundBtn.setTooltip(new Tooltip("Create mark"));
		delBtn.setTooltip(new Tooltip("Delete selected element"));
		clearBtn.setTooltip(new Tooltip("Delete all additional elements"));
		
		clearBtn.setOnAction(e -> {		
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Remove all additional elements");
			String s = "Confirm to clear makup";
			alert.setContentText(s);
			 
			Optional<ButtonType> result = alert.showAndWait();
			 
			if ((result.isPresent()) && (result.get() == ButtonType.OK)) {		
				clearAuxElements();
			}
		});
		
		delBtn.setOnAction(e -> {

			model.getFileManager().getCsvFiles().forEach( f -> {
				model.getChart((CsvFile) f).get().clearFlags();
				var selected = f.getAuxElements().stream().filter(bo -> bo.isSelected()).findFirst();
				if (selected.isPresent()) {
					this.selected = selected.get();
					f.getAuxElements().remove(selected.get());
					f.setUnsaved(true);
				}
			});

			if (selected != null) {
				for (SgyFile sgyFile : model.getFileManager().getGprFiles()) {
					if (sgyFile.getAuxElements().contains(selected)) {
						sgyFile.getAuxElements().remove(selected);
						sgyFile.setUnsaved(true);
					}					
				}

				mouseInput = null;
				setSelected(null);
				model.setControls(null);
			}
			
			model.updateAuxElements();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		});
		
		addFoundBtn.setOnAction(e -> {
			int trace = 0;
			SgyFile sf = null;
			if (model.getControls() != null 
					&& !model.getControls().isEmpty()) {

					var tr = ((ClickPlace) model.getControls().get(0)).getTrace();
					sf = tr.getFile();
					trace = sf instanceof CsvFile ? tr.getIndexInFile():tr.getIndexInSet();
				
			}
			// TODO: add flag to the middle trace
			//else {
			//	trace = model.getProfileField().getMiddleTrace();
			//	sf = model.getSgyFileByTrace(trace);
			//}

			if (sf == null) {
				return;
			}
				
			FoundPlace rect = new FoundPlace(
					sf.getTraces().get(sf.getOffset().globalToLocal(trace)), sf.getOffset(), model);
			
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);

			selectControl(rect);
			model.updateAuxElements();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
		});
		
	}

	private void clearAuxElements() {

		for (SgyFile file : model.getFileManager().getCsvFiles()) {
			model.getChart((CsvFile) file).get().clearFlags();
			file.getAuxElements().clear();			
			file.setUnsaved(true);
		}

		for (SgyFile sgyFile : model.getFileManager().getGprFiles()) {
			sgyFile.getAuxElements().clear();			
			sgyFile.setUnsaved(true);
		}
			
		mouseInput = null;
		setSelected(null);
		model.setControls(null);

		model.updateAuxElements();
		model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
	}

	private boolean processPress(List<BaseObject> controls2, 
			Point2D localPoint, ScrollableData profField) {
		
		for (BaseObject o : controls2) {
			if (o.isPointInside(localPoint, profField)) {
				
				o.mousePressHandle(localPoint, profField);
				mouseInput = o;
				
				return true;
			}
		}
		
		return false;
	}

	private boolean processPress1(List<BaseObject> controls2, 
			Point2D localPoint, ScrollableData profField) {
		for (BaseObject o : controls2) {
			if (o.mousePressHandle(localPoint, profField)) {
				selectControl(o);
				mouseInput = selected;
				return true;
			}
		}
		return false;
	}

	public void selectControl(BaseObject o) {
		setSelected(o);
		model.setControls(null);
		List<BaseObject> c = selected.getControls();
		if (c != null) {
			model.setControls(c);
		}
	}

	@Override
	public boolean mouseReleaseHandle(Point2D localPoint, ScrollableData profField) {
		if (mouseInput != null) {			
			mouseInput.mouseReleaseHandle(localPoint, profField);
			mouseInput = null;

			profField.setCursor(Cursor.DEFAULT);
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point2D localPoint, ScrollableData profField) {

		if (mouseInput != null) {			
			mouseInput.mouseMoveHandle(localPoint, profField);
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.justdraw));
			return true;
		} else {
			if (aboveControl(localPoint, profField)) {
				profField.setCursor(Cursor.MOVE);
			} else if (aboveElement(localPoint, profField)) {
				profField.setCursor(Cursor.HAND);
			} else {
				profField.setCursor(Cursor.DEFAULT);
			}			
		}
		return false;
	}

	private boolean aboveControl(Point2D localPoint, ScrollableData profField) {
		if (model.getControls() == null) {
			return false;
		}
		
		for (BaseObject bo : model.getControls()) {
			if (bo.isPointInside(localPoint, profField)) {
				return true;
			}
		}
		return false;
	}

	private boolean aboveElement(Point2D localPoint, ScrollableData profField) {
		if (model.getAuxElements() == null) {
			return false;
		}
		
		for (BaseObject bo : model.getAuxElements()) {
			if (bo.isPointInside(localPoint, profField)) {
				return true;
			}
		}
		
		return false;
	}

	private void setSelected(BaseObject selected) {
		if (this.selected != null) {
			this.selected.setSelected(false);
		}
		
		if (selected != null) {
			selected.setSelected(true);
		}
		
		this.selected = selected;
	}

	@EventListener
	private void somethingChanged(WhatChanged changed) {
		if (changed.isTraceCut()) {
			mouseInput = null;
			setSelected(null);
			model.setControls(null);			
		}
	}

	@EventListener
	private void fileOpened(FileOpenedEvent fileOpenedEvent) {
		mouseInput = null;
		setSelected(null);
		model.setControls(null);
	}
}