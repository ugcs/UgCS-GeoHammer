package com.ugcs.gprvisualizer.gpr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObjectImpl;

public class LeftRulerController {
	public static Stroke STROKE = new BasicStroke(1.0f);
	
	public interface Converter {
		Pair<Integer, Integer> convert(int s, int f);
		
		int back(int unt);
		
		String getUnit();
	}
	
	
	private Model model;
	private Converter[] list = {
			new SamplConverter(),
			new NanosecConverter()
	};
	private int index = 0;
	
	public LeftRulerController(Model model) {
		this.model = model;
	}
	
	public Converter getConverter() {
		return list[index];
	}
	
	public void nextConverter() {
		index = (index + 1) % list.length;
	}
	
	
	class SamplConverter implements Converter {

		@Override
		public Pair<Integer, Integer> convert(int s, int f) {
			return Pair.of(s, f);
		}
		
		public int back(int unt) {
			return unt;
		}

		@Override
		public String getUnit() {
			return "smpl";
		}		
	}
	
	class NanosecConverter implements Converter {

		@Override
		public Pair<Integer, Integer> convert(int s, int f) {
			
			SgyFile fl = model.getFileManager().getGprFiles().get(0);
			
			
			return Pair.of(
					fl.getSampleInterval() * s / 1000, 
					fl.getSampleInterval() * f / 1000);
		}
		
		public int back(int unt) {
			SgyFile fl = model.getFileManager().getGprFiles().get(0);
			return unt * 1000 / fl.getSampleInterval();
		}
		

		@Override
		public String getUnit() {
			return "  ns";
		}		
	}
	
	
	public BaseObjectImpl tb = new BaseObjectImpl() {

		@Override
		public void drawOnCut(Graphics2D g2, ProfileField profField) {
			
			setClip(g2, profField.getClipInfoRect());
			
			Rectangle  r = getRect(profField);
			
			g2.setStroke(STROKE);
			g2.setColor(Color.YELLOW);
			g2.drawRoundRect(r.x, r.y, r.width, r.height, 7, 7);			
			
			g2.setColor(Color.white);
			String text = getConverter().getUnit();
			int width = g2.getFontMetrics().stringWidth(text);
			g2.drawString(text, r.x + r.width - width - 4, r.y + r.height - 5);
			
		}

		@Override
		public boolean isPointInside(Point localPoint, ProfileField profField) {
			return false;
		}

		//@Override
		private Rectangle getRect(ProfileField profField) {
			Rectangle  r = profField.getInfoRect();
			return new Rectangle(profField.visibleStart + r.x + 5, r.y + r.height - 25, 
					r.width - 10, 20);
		}

		@Override
		public void signal(Object obj) {
			
		}

		@Override
		public List<BaseObject> getControls() {
			return null;
		}

		@Override
		public boolean saveTo(JSONObject json) {
			return false;
		}

		@Override
		public boolean mousePressHandle(Point localPoint, ProfileField profField) {
			if (getRect(profField).contains(localPoint)) {
				
				nextConverter();
				return true;
			}
			
			return false;
		}

		@Override
		public boolean mousePressHandle(Point2D point, MapField field) {
			return false;
		}

		@Override
		public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
			return null;
		}

		@Override
		public boolean isFit(int begin, int end) {
			return false;
		}

		@Override
		public boolean mouseReleaseHandle(Point localPoint, ProfileField profField) {
			return false;
		}

		@Override
		public boolean mouseMoveHandle(Point point, ProfileField profField) {
			return false;
		}
		
		@Override
		public void drawOnMap(Graphics2D g2, MapField mapField) {
							
		}


	};

}
