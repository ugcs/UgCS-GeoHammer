package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProfileView;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ShapeHolder;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class RemoveFileButton extends BaseObjectImpl implements BaseObject, MouseHandler {

	static int R_HOR = ResourceImageHolder.IMG_CLOSE_FILE.getWidth(null);
	static int R_VER = ResourceImageHolder.IMG_CLOSE_FILE.getHeight(null);

	private int traceInFile;
	private VerticalCutPart offset;	
	private SgyFile sgyFile;
	
	public int getTraceInFile() {
		return traceInFile;
	}
	
	public static RemoveFileButton loadFromJson(JSONObject json, SgyFile sgyFile) {
				
		return null;
	}
	
	public RemoveFileButton(int trace, VerticalCutPart offset, SgyFile sgyFile) {
		this.offset = offset;
			
		this.traceInFile = trace;
		
		this.sgyFile = sgyFile;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		
		if(isPointInside(localPoint, vField)) {
			
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("warning");
			alert.setContentText("Confirm to close file");
			 
			Optional<ButtonType> result = alert.showAndWait();
			 
			if ((result.isPresent()) && (result.get() == ButtonType.OK)) {		
				//todo:
				
				int index = AppContext.model.getFileManager().getFiles().indexOf(sgyFile);
				AppContext.model.getFileManager().getFiles().remove(index);
				
			
				AppContext.model.getFileManager().clearTraces();
				AppContext.model.init();
				AppContext.model.getVField().clear();
			}
			
			//AppContext.model.getField().setSceneCenter(getTrace().getLatLon());			
			//AppContext.notifyAll(new WhatChanged(Change.mapscroll));
			
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point, ProfileField vField) {
		
		return false;
	}
	
	

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		
	}

	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		
		Rectangle rect = getRect(vField);
		
		
		
		g2.translate(rect.x, rect.y);
		
		g2.drawImage(ResourceImageHolder.IMG_CLOSE_FILE, 0, 0, null);
		
		g2.translate(-rect.x, -rect.y);
		
	}
	
	public Rectangle getRect(ProfileField vField) {
		
		int x = vField.traceToScreen(offset.localToGlobal(traceInFile));
				
		Rectangle rect = new Rectangle(x - R_HOR, 0, 
			R_HOR, R_VER);
		return rect;
	}
	
	public Rectangle getRect(MapField hField) {
		
		
		return null;
	}

	private Trace getTrace() {
		return AppContext.model.getFileManager().getTraces().get(offset.localToGlobal(traceInFile));
	}

	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	@Override
	public void signal(Object obj) {
		
		
	}

	@Override
	public List<BaseObject> getControls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveTo(JSONObject json) {
		//json.put("trace", traceInFile);
		return false;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		
		
		return false;
	}

	@Override
	public BaseObject copy(int traceoffset, VerticalCutPart verticalCutPart) {
		RemoveFileButton result = new RemoveFileButton(traceInFile, verticalCutPart, sgyFile); 
		
		return result;
	}

	@Override
	public boolean isFit(int begin, int end) {
		
		return traceInFile >= begin && traceInFile <=end;
	}

}
