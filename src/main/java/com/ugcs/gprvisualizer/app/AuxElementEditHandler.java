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
import com.github.thecoldwine.sigrun.common.ext.VerticalCutField;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxElement;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.Hyperbola;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AuxElementEditHandler implements MouseHandler {

	private Model model;
	private CleverImageView cleverView;
	
	//private AuxElement selectedAuxElement;
	private VerticalCutField field;
	private boolean moved = false;
	
	//private List<BaseObject> controls = null;
	private BaseObject selected;
	private MouseHandler mouseInput;
	
//	tb.getItems().add(new Button("", ResourceImageHolder.getImageView("addHyp.png")));
//	tb.getItems().add(new Button("", ResourceImageHolder.getImageView("addRect.png")));
//	tb.getItems().add(new Button("", ResourceImageHolder.getImageView("addFlag.png")));
//	tb.getItems().add(new Button("", ResourceImageHolder.getImageView("addSurf.png")));
	
	private Button addBtn = new Button("", ResourceImageHolder.getImageView("addRect.png"));
	private Button addHypBtn = new Button("", ResourceImageHolder.getImageView("addHyp.png"));	
	private Button addSurfaceBtn = new Button("", ResourceImageHolder.getImageView("addSurf.png"));
	private Button addFoundBtn = new Button("", ResourceImageHolder.getImageView("addFlag.png"));
	
	private Button delBtn = new Button("", ResourceImageHolder.getImageView("delete-20.png"));
	private Button clearBtn = new Button("", ResourceImageHolder.getImageView("delete-all-20.png"));
	
	public AuxElementEditHandler(CleverImageView cleverView) {
		this.cleverView = cleverView;
		this.model = this.cleverView.model;
		field = cleverView.getField();
		
		initButtons();
	}

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {
		
		boolean processed = false;
		if(model.getControls() != null) {
			processed = processPress(model.getControls(), localPoint, vField);
		}
		
		if(!processed && selected != null) {
			processed = selected.mousePressHandle(localPoint, vField);
			if(processed) {
				mouseInput = selected;
			}
		}
		
		if(!processed) {
			processed = processPress1(cleverView.model.getAuxElements(), localPoint, vField);
		}
		
		if(!processed) {
			//deselect
			mouseInput = null;
			selected = null;
			model.setControls(null);
		}
		
		moved = false;

		if(processed) {
			cleverView.repaintEvent();
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
		//new VBox(
			//	new HBox( addBtn, addHypBtn, addSurfaceBtn, addFoundBtn),
				//new HBox( delBtn, clearBtn));
	}
	
	protected void initButtons(){
		
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
			
			if(selected != null) {
				for(SgyFile sgyFile : model.getFileManager().getFiles()) {
					sgyFile.getAuxElements().remove(selected);
				}				
				
				mouseInput = null;
				selected = null;
				model.setControls(null);
			}
			
			model.updateAuxElements();
			
			cleverView.repaintEvent();
			
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
		});
		
		addBtn.setOnAction(e -> {				
				
				
				SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
				
				if(sf != null) {
					AuxRect rect = new AuxRect(field.getSelectedTrace(), field.getStartSample()+30, sf.getOffset());
					
					sf.getAuxElements().add(rect);
					model.updateAuxElements();
				}
				cleverView.repaintEvent();
			}
		);
		
		addHypBtn.setOnAction(e -> {				
			
			
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			
			if(sf != null) {
				Hyperbola rect = new Hyperbola(field.getSelectedTrace(), field.getStartSample()+30, sf.getOffset());
				
				sf.getAuxElements().add(rect);
				model.updateAuxElements();
			}
			cleverView.repaintEvent();
		}
	);
		
		addSurfaceBtn.setOnAction(e -> {				
			
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			if(sf == null) {
				return;
			}
				
			AuxRect rect = createSurfaceRect(sf);
				
			
			sf.getAuxElements().add(rect);
			model.updateAuxElements();
			
			cleverView.repaintEvent();
		});
		
		addFoundBtn.setOnAction(e -> {				
			
			SgyFile sf = model.getSgyFileByTrace(field.getSelectedTrace());
			
			//Trace tr = model.getFileManager().getTraces().get(field.getSelectedTrace());
			
			FoundPlace rect = new FoundPlace(sf.getOffset().globalToLocal(field.getSelectedTrace()), sf.getOffset());
				
			if(sf != null) {
				sf.getAuxElements().add(rect);
				model.updateAuxElements();
			}
			cleverView.repaintEvent();
		});
		
	}

	private void clearAuxElements() {
		for(SgyFile sgyFile : model.getFileManager().getFiles()) {
			sgyFile.getAuxElements().clear();
		}				
			
		mouseInput = null;
		selected = null;
		model.setControls(null);

		
		model.updateAuxElements();
		
		cleverView.repaintEvent();
		
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
		System.out.println("width " + width);
		int[] topCut = new int[width];
		int[] botCut = new int[width];
		
		for(int i=0; i<width; i++) {
			Trace t = sf.getTraces().get(i);
			topCut[i] = Math.max(0, t.maxindex2-topMarg-topStart);
			botCut[i] = Math.min(botFinish-topStart, t.maxindex2+botMarg-topStart);
			
			System.out.println(" " + topCut[i] + " " + botCut[i] );
		}
		
		rect.setTopCut(topCut);
		rect.setBotCut(botCut);
		rect.updateMaskImg();
		return rect;
	}

	private boolean processPress(List<BaseObject> controls2, Point localPoint, VerticalCutField vField) {
		for(BaseObject o : controls2) {
			if(o.isPointInside(localPoint, vField)) {
				
				o.mousePressHandle(localPoint, vField);
				mouseInput = o;
				
				return true;
			}
		}
		
		return false;
	}

	private boolean processPress1(List<BaseObject> controls2, Point localPoint, VerticalCutField vField) {
		for(BaseObject o : controls2) {
			if(o.mousePressHandle(localPoint, vField)) {
				
				selected  = o;
				model.setControls(null);
				List<BaseObject> c = selected.getControls();
				if(c != null) {
					model.setControls(c);
				}
				
				return true;
			}
		}
		
		return false;
	}
	/**
				

	 */

	@Override
	public boolean mouseReleaseHandle(Point localPoint, VerticalCutField vField) {

		if(mouseInput != null) {			
			mouseInput.mouseReleaseHandle(localPoint, vField);
			mouseInput = null;
			
			cleverView.imageView.setCursor(Cursor.DEFAULT);
			
			cleverView.repaintEvent();
			return true;
		}
		
		
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point localPoint, VerticalCutField vField) {

		if(mouseInput != null) {			
			mouseInput.mouseMoveHandle(localPoint, vField);
			
			cleverView.repaintEvent();
			
			return true;
		}else{
			if(aboveControl(localPoint, vField)) {
				cleverView.imageView.setCursor(Cursor.MOVE);
			}else if(aboveElement(localPoint, vField)) {
				cleverView.imageView.setCursor(Cursor.HAND);
			}else{
				cleverView.imageView.setCursor(Cursor.DEFAULT);
			}			
		}		
		
		
		return false;
	}

	private boolean aboveControl(Point localPoint, VerticalCutField vField) {
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
	
	private boolean aboveElement(Point localPoint, VerticalCutField vField) {
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


}
