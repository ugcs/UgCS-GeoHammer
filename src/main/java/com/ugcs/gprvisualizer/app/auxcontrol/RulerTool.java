package com.ugcs.gprvisualizer.app.auxcontrol;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.MouseHandler;
import com.ugcs.gprvisualizer.math.NumberUtils;

public class RulerTool extends BaseObjectImpl implements BaseObject, MouseHandler {
	
	static Font fontB = new Font("Verdana", Font.BOLD, 11);
	
	DragAnchor anch1; 
	DragAnchor anch2;
	VerticalCutPart offset;
	SgyFile file;
	
	public RulerTool(SgyFile file) {
		this.file = file;
		offset = file.getOffset();
		
		
		anch1 = new DragAnchor(ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				anch1.setTrace(
					NumberUtils.norm(anch1.getTrace() , 0, file.size()-1));

			}
		};
		
		anch2 = new DragAnchor(ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				anch2.setTrace(
						NumberUtils.norm(anch2.getTrace() , 0, file.size()-1));
				
			}
		};		
		
		anch1.setTrace(file.size()/3);
		anch1.setSample(10);
		anch2.setTrace(file.size()*2/3);
		anch2.setSample(100);
		
	}

	
	@Override
	public void drawOnCut(Graphics2D g2, ProfileField vField) {
		
		
		
		
		//Rectangle rect = getRect(vField);
		Point lt = vField.traceSampleToScreen(new TraceSample(
				offset.localToGlobal(anch1.getTrace()), anch1.getSample()));
		Point rb = vField.traceSampleToScreen(new TraceSample(
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
		
		drawText(g2, lt.x+3, (lt.y+rb.y)/2, smpDsp);		
		
		drawText(g2, (lt.x+rb.x)/2, rb.y-3, trcDsp);
		
		drawText(g2, (lt.x+rb.x)/2 + 5*fontHeight, (lt.y+rb.y)/2 + 2*fontHeight, distDsp);
		
	}
	
	private void drawText(Graphics2D g2, int x, int y, String str) {
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(str, g2);

        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x,
                   y - fm.getAscent(),
                   (int) rect.getWidth(),
                   (int) rect.getHeight(), 
                   5, 5);
        
        g2.setColor(Color.YELLOW.darker());
        g2.drawRoundRect(x,
                y - fm.getAscent(),
                (int) rect.getWidth(),
                (int) rect.getHeight(), 
                5, 5);

        g2.setColor(Color.MAGENTA);
        g2.drawString(str, x, y);		
	}
	
	private double dist() {
		int s = Math.min(anch1.getTrace(), anch2.getTrace());
		int f = Math.max(anch1.getTrace(), anch2.getTrace());
		
		
		List<Trace> traces = file.getTraces();
		
		double dst = 0;
		for(int i=s+1; i<=f; i++) {
			dst += traces.get(i).getPrevDist();
		}
		
		double h_dst_cm = dst*100;
		
		
		double h = (double)Math.abs(anch1.getSample() - anch1.getSample());
		double grndLevel = 0;
		if(file.groundProfile != null) {
			grndLevel = file.groundProfile.deep[(anch2.getTrace() + anch1.getTrace())/2];
		}
		
		double h1 = Math.min(anch1.getSample(), anch2.getSample()); 
		double h2 = Math.max(anch1.getSample(), anch2.getSample());
		
		double hair = Math.max(0,  Math.min(grndLevel, h2) - h1); 
		double hgrn = h2-h1 - hair;
		
		
		double v_dst_cm =  file.getSamplesToCmAir() * hair + file.getSamplesToCmGrn() * hgrn;
		
		double diag = Math.sqrt(h_dst_cm*h_dst_cm + v_dst_cm*v_dst_cm);
		
		
		return diag;
	}

	
	@Override
	public boolean mousePressHandle(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawOnMap(Graphics2D g2, MapField hField) {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public boolean isPointInside(Point localPoint, ProfileField vField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rectangle getRect(ProfileField vField) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BaseObject> getControls() {
		return Arrays.asList(anch1, anch2);	
	}

	@Override
	public boolean saveTo(JSONObject json) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mousePressHandle(Point2D point, MapField field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseObject copy(int offset, VerticalCutPart verticalCutPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFit(int begin, int end) {
		// TODO Auto-generated method stub
		return true;
	}

}
