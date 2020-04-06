package com.ugcs.gprvisualizer.app;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.thecoldwine.sigrun.common.ext.AreaType;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AuxElementEditHandler implements MouseHandler, SmthChangeListener {

	private Model model;
	private ProfileView profileView;
	private ProfileField field;
	
	private BaseObject selected;
	private MouseHandler mouseInput;
	
	private Button addBtn = new Button("", ResourceImageHolder.getImageView("addRect.png"));
	private Button addHypBtn = new Button("", ResourceImageHolder.getImageView("addHyp.png"));	
	private Button addSurfaceBtn = new Button("", ResourceImageHolder.getImageView("addSurf.png"));
	private Button addFoundBtn = new Button("", ResourceImageHolder.getImageView("addFlag.png"));
	
	private Button delBtn = new Button("", ResourceImageHolder.getImageView("delete-20.png"));
	private Button clearBtn = new Button("", ResourceImageHolder.getImageView("delete-all-20.png"));
	
	public AuxElementEditHandler(ProfileView cleverView) {
		this.profileView = cleverView;
		this.model = this.profileView.model;
		field = cleverView.getField();
		
		initButtons();
		
		AppContext.smthListener.add(this);
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		
		boolean processed = false;
		if(model.getControls() != null) {
			processed = processPress(model.getControls(), localPoint, vField);
		}
		
		if(!processed && getSelected() != null) {
			processed = getSelected().mousePressHandle(localPoint, vField);
			if(processed) {
				mouseInput = getSelected();
			}
		}
		
		if(!processed) {
			processed = processPress1(profileView.model.getAuxElements(), localPoint, vField);
		}
		
		if(!processed) {
			//deselect
			mouseInput = null;
			setSelected(null);
			model.setControls(null);
		}
		
		if(processed) {
			profileView.repaintEvent();
		}

		return processed;
	}
	
	public List<Node> getRightPanelTools() {
		return Arrays.asList(addBtn, addHypBtn, addSurfaceBtn, addFoundBtn, getSpacer(), delBtn, clearBtn);	
	}
	
	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}
	
	public Node getRight() {
		return new VBox();
	}
	
	protected void initButtons(){
		
		addBtn.setTooltip(new Tooltip("Create rectangle with mask"));
		addHypBtn.setTooltip(new Tooltip("Create parametric hyperbola"));
		addSurfaceBtn.setTooltip(new Tooltip("Create surface rectangle with mask"));
		addFoundBtn.setTooltip(new Tooltip("Create mark"));
		delBtn.setTooltip(new Tooltip("Delete selected element"));
		clearBtn.setTooltip(new Tooltip("Delete all additional elements"));
		
		clearBtn.setOnAction(e -> {		
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("warning");
			String s = "Confirm to clear makup";
			alert.setContentText(s);
			 
			Optional<ButtonType> result = alert.showAndWait();
			 
			if ((result.isPresent()) && (result.get() == ButtonType.OK)) {		
				clearAuxElements();
			}
		});
		
		delBtn.setOnAction(e -> {		
			
			if(getSelected() != null) {
				for(SgyFile sgyFile : model.getFileManager().getFiles()) {
					if(sgyFile.getAuxElements().contains(getSelected())){
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
			
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
		});
		
		addBtn.setOnAction(e -> {				
				
				
				SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
				
				if(sf == null) {
					return;
				}
				
				AuxRect rect = new AuxRect(field.getSelectedTrace(), field.getStartSample()+30, sf.getOffset());
				
				sf.getAuxElements().add(rect);
				sf.setUnsaved(true);
				model.updateAuxElements();
				
				selectControl(rect);
				
				profileView.repaintEvent();
			}
		);
		
		addHypBtn.setOnAction(e -> {				
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			
			if(sf == null) {
				return;
			}

			Hyperbola rect = new Hyperbola(field.getSelectedTrace(), field.getStartSample()+30, sf.getOffset());
				
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);
			
			model.updateAuxElements();			
			
			selectControl(rect);
			
			profileView.repaintEvent();
		}
	);
		
		addSurfaceBtn.setOnAction(e -> {				
			
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			if(sf == null) {
				return;
			}
				
			AuxRect rect = createSurfaceRect(sf);
				
			
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);
			
			model.updateAuxElements();
			
			profileView.repaintEvent();
		});
		
		addFoundBtn.setOnAction(e -> {				
			
			int trace;
			if(model.getControls() != null && !model.getControls().isEmpty() && model.getControls().get(0).getGlobalTrace() >= 0) { 
				trace = model.getControls().get(0).getGlobalTrace();
			}else {
				trace = field.getSelectedTrace();
			}
			SgyFile sf = model.getSgyFileByTrace(trace);
			
			if(sf == null) {
				return;
			}
				
			FoundPlace rect = new FoundPlace(sf.getOffset().globalToLocal(trace), sf.getOffset());
			
			sf.getAuxElements().add(rect);
			sf.setUnsaved(true);
			
			model.updateAuxElements();

			selectControl(rect);
			
			
			updateViews();
		});
		
	}
	
	protected void updateViews() {
		//profileView.repaintEvent();
		AppContext.notifyAll(new WhatChanged(Change.justdraw));		
	}

	private void clearAuxElements() {
		for(SgyFile sgyFile : model.getFileManager().getFiles()) {
			sgyFile.getAuxElements().clear();
			
			sgyFile.setUnsaved(true);
		}				
			
		mouseInput = null;
		setSelected(null);
		model.setControls(null);

		
		model.updateAuxElements();
		
		profileView.repaintEvent();
		
		AppContext.notifyAll(new WhatChanged(Change.justdraw));
	}

	private AuxRect createSurfaceRect(SgyFile sf) {
		Trace tr1 = sf.getTraces().get(0);
		Trace tr2 = sf.getTraces().get(sf.getTraces().size()-1);

		int minGrnd = tr1.maxindex2;
		int maxGrnd = tr1.maxindex2;
		for(Trace trace : sf.getTraces()){
			minGrnd = Math.min(minGrnd, trace.maxindex2);
			maxGrnd = Math.max(maxGrnd, trace.maxindex2);
		}
		int topMarg = 10;
		int botMarg = 15;

		int topStart = minGrnd-topMarg;
		int botFinish = maxGrnd+botMarg;
		
		AuxRect rect = new AuxRect(
			tr1.indexInSet, tr2.indexInSet,
			topStart, botFinish, 
			sf.getOffset());
		
		rect.setType(AreaType.Surface);

		int width = tr2.indexInSet - tr1.indexInSet +1;
		
		int[] topCut = new int[width];
		int[] botCut = new int[width];
		
		for(int i=0; i<width; i++) {
			Trace t = sf.getTraces().get(i);
			topCut[i] = Math.max(0, t.maxindex2-topMarg-topStart);
			botCut[i] = Math.min(botFinish-topStart, t.maxindex2+botMarg-topStart);
		
		}
		
		rect.setTopCut(topCut);
		rect.setBotCut(botCut);
		rect.updateMaskImg();
		return rect;
	}

	private boolean processPress(List<BaseObject> controls2, Point localPoint, ProfileField vField) {
		for(BaseObject o : controls2) {
			if(o.isPointInside(localPoint, vField)) {
				
				o.mousePressHandle(localPoint, vField);
				mouseInput = o;
				
				return true;
			}
		}
		
		return false;
	}

	private boolean processPress1(List<BaseObject> controls2, Point localPoint, ProfileField vField) {
		for(BaseObject o : controls2) {
			if(o.mousePressHandle(localPoint, vField)) {
				
				selectControl(o);
				
				return true;
			}
		}
		
		return false;
	}

	public void selectControl(BaseObject o) {
		setSelected(o);
		model.setControls(null);
		List<BaseObject> c = getSelected().getControls();
		if(c != null) {
			model.setControls(c);
		}
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {

		if(mouseInput != null) {			
			mouseInput.mouseReleaseHandle(localPoint, vField);
			mouseInput = null;
			
			profileView.imageView.setCursor(Cursor.DEFAULT);
			
			profileView.repaintEvent();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point localPoint, ProfileField vField) {

		if(mouseInput != null) {			
			mouseInput.mouseMoveHandle(localPoint, vField);
			
			profileView.repaintEvent();
			
			return true;
		}else{
			if(aboveControl(localPoint, vField)) {
				profileView.imageView.setCursor(Cursor.MOVE);
			}else if(aboveElement(localPoint, vField)) {
				profileView.imageView.setCursor(Cursor.HAND);
			}else{
				profileView.imageView.setCursor(Cursor.DEFAULT);
			}			
		}		
		
		
		return false;
	}

	private boolean aboveControl(Point localPoint, ProfileField vField) {
		if(model.getControls() == null) {
			return false;
		}
		
		for(BaseObject bo : model.getControls()) {
			if(bo.isPointInside(localPoint, vField)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean aboveElement(Point localPoint, ProfileField vField) {
		if(model.getAuxElements() == null) {
			return false;
		}
		
		for(BaseObject bo : model.getAuxElements()) {
			if(bo.isPointInside(localPoint, vField)) {
				return true;
			}
		}
		
		return false;
	}

	private BaseObject getSelected() {
		return selected;
	}

	private void setSelected(BaseObject newselected) {
		if(this.selected != null) {
			this.selected.setSelected(false);
		}
		
		if(newselected != null) {
			newselected.setSelected(true);
		}
		
		this.selected = newselected;
	}

	@Override
	public void somethingChanged(WhatChanged changed) {
		if(changed.isFileopened() || changed.isTraceCut()) {
			
			mouseInput = null;
			setSelected(null);
			model.setControls(null);			
		}		
	}
}
