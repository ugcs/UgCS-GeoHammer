package com.ugcs.gprvisualizer.app;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.Hyperbola;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Component
public class AuxElementEditHandler implements MouseHandler, SmthChangeListener {

	@Autowired
	private Model model;
	
	@Autowired
	private ProfileView profileView;
	
	@Autowired
	private Broadcast broadcast;
	
	private ProfileField field;	
	private BaseObject selected;
	private MouseHandler mouseInput;
	
	private Button addBtn = new Button("", 
			ResourceImageHolder.getImageView("addRect.png"));
	
	private Button addHypBtn = new Button("", 
			ResourceImageHolder.getImageView("addHyp.png"));
	
	private Button addSurfaceBtn = new Button("", 
			ResourceImageHolder.getImageView("addSurf.png"));
	
	private Button addFoundBtn = new Button("", 
			ResourceImageHolder.getImageView("addFlag.png"));
	
	private Button delBtn = new Button("", 
			ResourceImageHolder.getImageView("delete-20.png"));
	
	private Button clearBtn = new Button("", 
			ResourceImageHolder.getImageView("delete-all-20.png"));
	
	public AuxElementEditHandler() {
		
	}

	@PostConstruct
	public void init() {
		field = profileView.getField();
		
		initButtons();		
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField profField) {
		
		boolean processed = false;
		if (model.getControls() != null) {
			processed = processPress(model.getControls(), localPoint, profField);
		}
		
		if (!processed && getSelected() != null) {
			processed = getSelected().mousePressHandle(localPoint, profField);
			if (processed) {
				mouseInput = getSelected();
			}
		}
		
		if (!processed) {
			processed = processPress1(profileView.model.getAuxElements(), 
					localPoint, profField);
		}
		
		if (!processed) {
			//deselect
			mouseInput = null;
			setSelected(null);
			model.setControls(null);
		}
		
		if (processed) {
			profileView.repaintEvent();
		}

		return processed;
	}
	
	public List<Node> getRightPanelTools() {
		return Arrays.asList(addBtn, addHypBtn, addFoundBtn, 
				getSpacer(), delBtn, clearBtn);	
	}
	
	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}
	
	public Node getRight() {
		return new VBox();
	}
	
	protected void initButtons() {
		
		addBtn.setTooltip(new Tooltip("Create rectangle with mask"));
		addHypBtn.setTooltip(new Tooltip("Create parametric hyperbola"));
		addSurfaceBtn.setTooltip(new Tooltip("Create surface rectangle with mask"));
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
			
			if (getSelected() != null) {
				for (SgyFile sgyFile : model.getFileManager().getFiles()) {
					if (sgyFile.getAuxElements().contains(getSelected())) {
						sgyFile.getAuxElements().remove(getSelected());
						sgyFile.setUnsaved(true);
					}					
				}				
				
				mouseInput = null;
				setSelected(null);
				model.setControls(null);
			}
			
			model.updateAuxElements();
			
			profileView.repaintEvent();
			
			broadcast.notifyAll(new WhatChanged(Change.justdraw));
		});
		
		addBtn.setOnAction(e -> {				
				SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
				
				if (sf == null) {
					return;
				}
				
				AuxRect rect = new AuxRect(field.getSelectedTrace(), 
						field.getStartSample() + 30, sf.getOffset());
				
				sf.getAuxElements().add(rect);
				sf.setUnsaved(true);
				model.updateAuxElements();
				
				selectControl(rect);
				
				profileView.repaintEvent();
			}
		);
		
		addHypBtn.setOnAction(e -> {				
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			
			if (sf == null) {
				return;
			}

			Hyperbola rect = new Hyperbola(field.getSelectedTrace(), 
					field.getStartSample() + 30, sf.getOffset());
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);
			
			model.updateAuxElements();			
			
			selectControl(rect);
			
			profileView.repaintEvent();
		}
	);
		
		addFoundBtn.setOnAction(e -> {				
			
			int trace;
			if (model.getControls() != null 
					&& !model.getControls().isEmpty() 
					&& model.getControls().get(0).getGlobalTrace() >= 0) {
				
				trace = model.getControls().get(0).getGlobalTrace();
				
			} else {
				trace = field.getSelectedTrace();
			}
			SgyFile sf = model.getSgyFileByTrace(trace);
			
			if (sf == null) {
				return;
			}
				
			FoundPlace rect = new FoundPlace(
					sf.getOffset().globalToLocal(trace), sf.getOffset());
			
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);
			
			model.updateAuxElements();
			selectControl(rect);
			updateViews();
		});
		
	}
	
	protected void updateViews() {
		broadcast.notifyAll(new WhatChanged(Change.justdraw));		
	}

	private void clearAuxElements() {
		for (SgyFile sgyFile : model.getFileManager().getFiles()) {
			sgyFile.getAuxElements().clear();
			
			sgyFile.setUnsaved(true);
		}				
			
		mouseInput = null;
		setSelected(null);
		model.setControls(null);

		
		model.updateAuxElements();
		
		profileView.repaintEvent();
		
		broadcast.notifyAll(new WhatChanged(Change.justdraw));
	}

	private boolean processPress(List<BaseObject> controls2, 
			Point localPoint, ProfileField profField) {
		
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
			Point localPoint, ProfileField profField) {
		for (BaseObject o : controls2) {
			if (o.mousePressHandle(localPoint, profField)) {
				
				selectControl(o);
				
				mouseInput = getSelected();
				
				return true;
			}
		}
		
		return false;
	}

	public void selectControl(BaseObject o) {
		setSelected(o);
		model.setControls(null);
		List<BaseObject> c = getSelected().getControls();
		if (c != null) {
			model.setControls(c);
		}
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField profField) {

		if (mouseInput != null) {			
			mouseInput.mouseReleaseHandle(localPoint, profField);
			mouseInput = null;
			
			profileView.imageView.setCursor(Cursor.DEFAULT);
			
			profileView.repaintEvent();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point localPoint, ProfileField profField) {

		if (mouseInput != null) {			
			mouseInput.mouseMoveHandle(localPoint, profField);
			
			profileView.repaintEvent();
			
			return true;
		} else {
			if (aboveControl(localPoint, profField)) {
				profileView.imageView.setCursor(Cursor.MOVE);
			} else if (aboveElement(localPoint, profField)) {
				profileView.imageView.setCursor(Cursor.HAND);
			} else {
				profileView.imageView.setCursor(Cursor.DEFAULT);
			}			
		}		
		
		return false;
	}

	private boolean aboveControl(Point localPoint, ProfileField profField) {
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
	
	private boolean aboveElement(Point localPoint, ProfileField profField) {
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

	public BaseObject getSelected() {
		return selected;
	}

	private void setSelected(BaseObject newselected) {
		if (this.selected != null) {
			this.selected.setSelected(false);
		}
		
		if (newselected != null) {
			newselected.setSelected(true);
		}
		
		this.selected = newselected;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if (changed.isFileopened() || changed.isTraceCut()) {
			
			mouseInput = null;
			setSelected(null);
			model.setControls(null);			
		}		
	}
}
