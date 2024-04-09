package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.thecoldwine.sigrun.common.ext.MapField;
import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.github.thecoldwine.sigrun.common.ext.VerticalCutPart;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.math.MathUtils;
import com.ugcs.gprvisualizer.math.NumberUtils;

public class RulerTool extends BaseObjectImpl 
	implements BaseObject, MouseHandler {
	
	private static Font fontB = new Font("Verdana", Font.BOLD, 11);
	private static final double MARGIN = 1.0;
		
	private DragAnchor anch1;
	private DragAnchor anch2;
	private VerticalCutPart offset;
	private SgyFile file;
	
	
	public static RulerTool createRulerTool(ProfileField field, SgyFile file) {
		int fvt = field.getFirstVisibleTrace();
		int fileStart = file.getOffset().getStartTrace();
		int startTrace = Math.max(0, fvt - fileStart);
		
		int lvt = field.getLastVisibleTrace();
		
		int finishTrace = Math.min(file.size() - 1, 
				lvt - file.getOffset().getStartTrace());
		int wd = finishTrace - startTrace;
		
		int smpStart = field.getStartSample();
		int smpFinish = Math.min(file.getMaxSamples(), 
				field.getLastVisibleSample(field.getMainRect().height));
		int ht = smpFinish - smpStart; 
		
		RulerTool fp = new RulerTool(file, 
				startTrace + wd / 3, startTrace + wd * 2 / 3, 
				smpStart + ht / 3, smpStart + ht * 2 / 3);
		return fp;
	}
	
	
	class RulerAnchor extends DragAnchor {
		
		public RulerAnchor(Image img, AlignRect alignRect, VerticalCutPart offset) {
			super(img, alignRect, offset);
		}

		protected void realDraw(Graphics2D g2, Rectangle rect) {
		
			g2.setColor(Color.BLACK);
			g2.fillOval(rect.x, rect.y, rect.width, rect.height);

			g2.setColor(Color.YELLOW);
			g2.drawOval(rect.x, rect.y, rect.width, rect.height);
		
		}
	}
	
	public RulerTool(SgyFile file, int s, int f, int smpStart, int smpFinish) {
		this.file = file;
		offset = file.getOffset();
		
		
		anch1 = new RulerAnchor(ResourceImageHolder.IMG_VER_SLIDER, 
				AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				anch1.setTrace(
					NumberUtils.norm(anch1.getTrace(), 0, file.size() - 1));
				anch1.setSample(
						NumberUtils.norm(anch1.getSample(), 
								0, file.getMaxSamples()));

			}
		};
		
		anch2 = new RulerAnchor(ResourceImageHolder.IMG_VER_SLIDER, 
				AlignRect.CENTER, offset) {
			
			public void signal(Object obj) {
				anch2.setTrace(
						NumberUtils.norm(anch2.getTrace(),
								0, file.size() - 1));
				anch2.setSample(
						NumberUtils.norm(anch2.getSample(),
								0, file.getMaxSamples()));
			}
		};		
		
		anch1.setTrace(s);
		anch1.setSample(smpStart);
		anch2.setTrace(f);
		anch2.setSample(smpFinish);
	}
	
	@Override
	public void drawOnCut(Graphics2D g2, ProfileField profField) {
		Point lt = profField.traceSampleToScreen(new TraceSample(
				offset.localToGlobal(anch1.getTrace()), anch1.getSample()));
		Point rb = profField.traceSampleToScreen(new TraceSample(
				offset.localToGlobal(anch2.getTrace()), anch2.getSample()));
		
		g2.setColor(Color.RED);
		g2.drawLine(lt.x, lt.y, rb.x, rb.y);
		
		g2.drawLine(lt.x, lt.y, lt.x, rb.y);
		
		g2.drawLine(lt.x, rb.y, rb.x, rb.y);
		
		g2.setColor(Color.GRAY);
		
		
		
		g2.setFont(fontB);
		int fontHeight = g2.getFontMetrics().getHeight();

		String smpDsp = Math.abs(anch2.getSample() - anch1.getSample()) + " smp";
		
		String trcDsp = Math.abs(anch2.getTrace() - anch1.getTrace()) + " tr";
		
		String distDsp = String.format("%.2f cm", dist());
		
		drawText(g2, lt.x + 3, (lt.y + rb.y) / 2, smpDsp);		
		
		drawText(g2, (lt.x + rb.x) / 2, rb.y - 3, trcDsp);
		
		drawText(g2, (lt.x + rb.x) / 2 + 5 * fontHeight, 
				(lt.y + rb.y) / 2 + 2 * fontHeight, distDsp);
		
	}
	
	private void drawText(Graphics2D g2, int x, int y, String str) {
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(str, g2);

        rect = new Rectangle2D.Double(rect.getX() - MARGIN - 2, 
        		rect.getY() - MARGIN, 
        		rect.getWidth() + 2 * MARGIN + 3, 
        		rect.getHeight() + 2 * MARGIN);
        
        g2.setColor(Color.BLACK);
        
        g2.fillRoundRect(x + (int) rect.getX(),
			y + (int) rect.getY(), 
			(int) rect.getWidth(),
			(int) rect.getHeight(), 
			5, 5);
        
        g2.setColor(Color.YELLOW.darker());
        g2.drawRoundRect(x + (int) rect.getX(),
			y + (int) rect.getY(),
			(int) rect.getWidth(),
			(int) rect.getHeight(), 
			5, 5);

        g2.setColor(Color.MAGENTA);
        g2.drawString(str, x, y);		
	}
	
	private double dist() {
		
		
		int tr1 = anch1.getTrace();
		int tr2 = anch2.getTrace();
		int smp1 = anch1.getSample();
		int smp2 = anch2.getSample();
		
		
		
		double diag = distanceCm(file, tr1, tr2, smp1, smp2);
		
		
		return diag;
	}

	

	public static double distanceVCm(SgyFile file, int tr, double smp1, double smp2) {
		double grndLevel = 0;
		if (file.groundProfile != null) {
			grndLevel = file.groundProfile.deep[tr];
		}

		double h1 = Math.min(smp1, smp2); 
		double h2 = Math.max(smp1, smp2);
		
		double hair = Math.max(0,  Math.min(grndLevel, h2) - h1); 
		double hgrn = h2 - h1 - hair;
		
		double vertDistCm =  file.getSamplesToCmAir() * hair 
				+ file.getSamplesToCmGrn() * hgrn;
		
		
		return vertDistCm;
		
	}
	
	public static double distanceCm(SgyFile file, int tr1, int tr2, double smp1, double smp2) {
		double grndLevel = 0;
		if (file.groundProfile != null) {
			grndLevel = file.groundProfile.deep[(tr1 + tr2) / 2];
		}

		int s = Math.max(0, Math.min(tr1, tr2));
		int f = Math.min(file.size() - 1, Math.max(tr1, tr2));
		
		List<Trace> traces = file.getTraces();
		
		double dst = 0;
		for (int i = s + 1; i <= f; i++) {
			dst += traces.get(i).getPrevDist();
		}
		
		double horDistCm = dst;		
		
		double h1 = Math.min(smp1, smp2); 
		double h2 = Math.max(smp1, smp2);
		
		double hair = Math.max(0,  Math.min(grndLevel, h2) - h1); 
		double hgrn = h2 - h1 - hair;
		
		double vertDistCm =  file.getSamplesToCmAir() * hair 
				+ file.getSamplesToCmGrn() * hgrn;
		
		double diag = Math.sqrt(horDistCm * horDistCm + vertDistCm * vertDistCm);
		return diag;
	}


	
	public static int diagonalToSmp(SgyFile file, int tr, int smp, double c) {
		
		int grn = file.groundProfile.deep[tr];
	
		int i=0;
		double dst = 0;
		while (dst < c) {
			if (i < grn) {
				dst += file.getSamplesToCmAir();
			} else {
				dst += file.getSamplesToCmGrn();
			}
			i++;
		}

		return i;
	}
		
	public static int diagonalToSmp2(SgyFile file, int tr, int smp, double c) {
		
		int grn = file.groundProfile.deep[tr];
		
		//part of air
		
		double fullCm = distanceCm(file, tr, tr, 0, smp);
		double grndCm = distanceCm(file, tr, tr, 0, grn);
		double f = grndCm / fullCm;
		
		f = Math.clamp(f, 0, 1);
		
		double diagAir = c * f;
		double diagGrn = c * (1 - f);
		
		double smpAir = diagAir / file.getSamplesToCmAir();
		double smpGrn = diagGrn / file.getSamplesToCmGrn();
		
		double smpSum = smpAir + smpGrn;
		
		
		
		return (int) smpSum;
	}
	
	@Override
	public boolean mousePressHandle(Point2D point, MapField mapField) {
		return false;
	}

	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField profField) {
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
	
	@Override
	public boolean isPointInside(Point localPoint, ProfileField profField) {
		return false;
	}

	@Override
	public Rectangle getRect(ProfileField profField) {
		return null;
	}

	@Override
	public void signal(Object obj) {
	}

	@Override
	public List<BaseObject> getControls() {
		return Arrays.asList(anch1, anch2);	
	}

	@Override
	public boolean saveTo(JSONObject json) {
		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
		return null;
	}

	@Override
	public boolean isFit(int begin, int end) {
		return true;
	}

}
