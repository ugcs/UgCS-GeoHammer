package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;

import com.ugcs.gprvisualizer.app.GPRChart;
import com.ugcs.gprvisualizer.app.ScrollableData;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.events.FileClosedEvent;
import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.gpr.Model;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class RemoveFileButton extends BaseObjectImpl {

	static int R_HOR = ResourceImageHolder.IMG_CLOSE_FILE.getWidth(null);
	static int R_VER = ResourceImageHolder.IMG_CLOSE_FILE.getHeight(null);

	private int traceInFile;
	private VerticalCutPart offset;	
	private SgyFile sgyFile;
	
	public RemoveFileButton(int trace, VerticalCutPart offset, SgyFile sgyFile, Model model) {
		this.offset = offset;
		this.traceInFile = trace;
		this.sgyFile = sgyFile;
		this.model = model;
	}

	@Override
	public boolean mousePressHandle(Point2D localPoint, ScrollableData profField) {
		
		if (isPointInside(localPoint, profField)) {			
			 
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Close file");
			alert.setContentText(
					(sgyFile.isUnsaved() ? "File is not saved!\n" : "") 
					+ "Confirm to close file");
			
			 
			Optional<ButtonType> result = alert.showAndWait();
			 
			if ((result.isPresent()) && (result.get() == ButtonType.OK)) {		
				
				//int index = AppContext.model.getFileManager()
				//		.getFiles().indexOf(sgyFile);
				model.getFileManager().removeFile(sgyFile); //.getFiles().remove(index);
				
				model.init();
				model.initField();
				model.getProfileField(sgyFile).clear();

				model.publishEvent(new FileClosedEvent(this, List.of(sgyFile.getFile()), sgyFile));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public BaseObject copy(int traceoffset, VerticalCutPart verticalCutPart) {
		RemoveFileButton result = new RemoveFileButton(
				traceInFile, verticalCutPart, sgyFile, model);
		return result;
	}

	@Override
	public boolean isFit(int begin, int end) {
		return traceInFile >= begin && traceInFile <= end;
	}

	@Override
	public void drawOnCut(Graphics2D g2, ScrollableData scrollableData) {
		if (scrollableData instanceof GPRChart gprChart) {
			var profField = gprChart.getField();
			setClip(g2, profField.getClipTopMainRect());

			Rectangle rect = getRect(scrollableData);

			g2.translate(rect.x, rect.y);

			g2.drawImage(ResourceImageHolder.IMG_CLOSE_FILE, 0, 0, null);

			g2.translate(-rect.x, -rect.y);
		}
	}
	
	private Rectangle getRect(ScrollableData profField) {
		
		int x = profField.traceToScreen(offset.localToGlobal(traceInFile));
				
		Rectangle rect = new Rectangle(x - R_HOR, 0, 
			R_HOR, R_VER);
		return rect;
	}
	
	//public Rectangle getRect(MapField mapField) {
	//	return null;
	//}

	/*private Trace getTrace() {
		return AppContext.model.getFileManager().getTraces()
				.get(offset.localToGlobal(traceInFile));
	}*/

	@Override
	public boolean isPointInside(Point2D localPoint, ScrollableData profField) {
		Rectangle rect = getRect(profField);
		return rect.contains(localPoint.getX(), localPoint.getY());
	}

}
