package com.github.thecoldwine.sigrun.common.ext;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ugcs.gprvisualizer.app.auxcontrol.AlignRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.DragAnchor;
import com.ugcs.gprvisualizer.app.auxcontrol.ToggleButton;

import javafx.scene.control.ChoiceDialog;

public class AuxRect implements BaseObject {

	public static final String TYPES[] = { 
			"hyperbola", 
			"air", 
			"ground",  
            "ground line"}; 
	
	VerticalCutField vField;
	
	int traceStart;
	int traceFinish;
	int sampleStart;
	int sampleFinish;
	
	BufferedImage img;

	DragAnchor top;
	DragAnchor bottom;
	DragAnchor left;
	DragAnchor right;
	ToggleButton lock;
	ToggleButton selectType;
	
	int TRACE_W = 40;
	int SAMPLE_W = 20;
	int[] topCut;
	int[] botCut;
	boolean sideTop = true;
	boolean locked = false;
	String type = TYPES[0];
	
	public void saveTo(JSONObject json) {
		json.put("traceStart", traceStart);
		json.put("traceFinish", traceFinish);
		json.put("sampleStart", sampleStart);
		json.put("sampleFinish", sampleFinish);
		json.put("type", type);
		json.put("locked", locked);
		
		JSONArray arr = new JSONArray();
		for(int i : topCut) {
			arr.add(i);
		}		
		json.put("topCut", arr);
		
		
		JSONArray arr2 = new JSONArray();
		for(int i : botCut) {
			arr2.add(i);
		}		
		json.put("botCut", arr2);
	}
	
	public AuxRect(int traceCenter, int sampleCenter, VerticalCutField vField) {
		this.vField = vField;
		
		traceStart = traceCenter - TRACE_W;
		traceFinish = traceCenter + TRACE_W;

		sampleStart = sampleCenter - SAMPLE_W;
		sampleFinish = sampleCenter + SAMPLE_W;
		
		initDragAnchors(vField);		
	}
	
	
	public AuxRect(JSONObject ob, VerticalCutField vField) {

		this.vField = vField;
		
		traceStart = (int)(long)(Long)ob.get("traceStart");
		traceFinish = (int)(long)(Long)ob.get("traceFinish");
		sampleStart = (int)(long)(Long)ob.get("sampleStart");
		sampleFinish = (int)(long)(Long)ob.get("sampleFinish");
		
		initDragAnchors(vField);
		System.out.println("traceStart " + traceStart + "   traceFinish " + traceFinish);
	}
	

	private void initDragAnchors(VerticalCutField vField) {
		selectType = new ToggleButton(vField, traceStart, sampleStart, 
				ResourceImageHolder.IMG_CHOOSE,
				ResourceImageHolder.IMG_CHOOSE,
				new AlignRect(-1, -1)) {

			public void signal(Object obj) {
		        ChoiceDialog<String> dialog = new ChoiceDialog<String>(type, TYPES); 
		        Optional<String> result = dialog.showAndWait();
		        result.ifPresent(book -> {
		            type = book;
		        });				
			}			
			public int getTrace() {
				return (left.getTrace()) ;
			}
			public int getSample() {
				return (top.getSample()) ;
			}
		};
		
		lock = new ToggleButton(vField, traceFinish, sampleStart, 
				ResourceImageHolder.IMG_LOCK,
				ResourceImageHolder.IMG_UNLOCK,
				new AlignRect(-1, -1)) {

			public void signal(Object obj) {
				locked = (Boolean)obj;
				
				left.setVisible(!locked); 
				top.setVisible(!locked);
				right.setVisible(!locked);
				bottom.setVisible(!locked);
				
			}			
			public int getTrace() {
				return (right.getTrace()) ;
			}
			public int getSample() {
				return (top.getSample()) ;
			}
			
		};
		
		top = new DragAnchor(vField, traceStart, sampleStart, ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER) {
			public void signal(Object obj) {
				sampleStart = top.getSample();
				
				updateMaskImg();
			}
			
			public int getTrace() {
				return (left.getTrace() + right.getTrace()) / 2;
			}
		};
		bottom = new DragAnchor(vField, traceStart, sampleFinish, ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER) {
			public void signal(Object obj) {
				sampleFinish = bottom.getSample();
				
				updateMaskImg();
			}
			public int getTrace() {
				return (left.getTrace() + right.getTrace()) / 2;
			}
		};
		left = new DragAnchor(vField, traceStart, sampleStart, ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER) {
			public void signal(Object obj) {
				traceStart = left.getTrace();
				
				updateMaskImg();
			}
			public int getSample() {
				return (top.getSample() + bottom.getSample()) / 2;
			}
		};
		right = new DragAnchor(vField, traceFinish, sampleStart, ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER) {
			public void signal(Object obj) {
				traceFinish = right.getTrace();
				
				updateMaskImg();
			}
			public int getSample() {
				return (top.getSample() + bottom.getSample()) / 2;
			}
		};
	}

	private void updateMaskImg() {
		
		int width = traceFinish - traceStart;
		int height = sampleFinish - sampleStart;
		
		if(img == null || width != img.getWidth() || height != img.getHeight()) {
		
			topCut = new int[width];
			botCut = new int[width];
			Arrays.fill(botCut, height);
			
			img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			
			Graphics2D g2 = (Graphics2D)img.getGraphics();
			g2.drawLine(0,0,width, height);
		}
	}
	
	public List<BaseObject> getControls(){
		
		return //locked ? 
			//Arrays.asList(lock) :
			Arrays.asList(left, top, right, bottom, lock, selectType); 
				
	}

	@Override
	public boolean mousePressHandle(Point localPoint) {

		if(isPointInside(localPoint)){
			
		
			TraceSample ts = vField.screenToTraceSample(localPoint);
			int x = ts.getTrace() - traceStart;
			int y = ts.getSample() - sampleStart;
			if(x>=0 && x < topCut.length) {
				
				int top_dst = Math.abs(topCut[x] - y);
				int bot_dst = Math.abs(botCut[x] - y);

				sideTop = top_dst < bot_dst;

			}
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint) {

		return false;
	}

	@Override
	public boolean mouseMoveHandle(Point point) {

		if(img == null) {
			return false;
		}
		
		TraceSample ts = vField.screenToTraceSample(point);
		
		int x = ts.getTrace() - traceStart;
		int y = ts.getSample() - sampleStart;
		if(x>=0 && x < topCut.length) {
			
			if(sideTop) {
				topCut[x] = y;
			}else{
				botCut[x] = y;
			}
		
			Graphics2D g2 = (Graphics2D)img.getGraphics();
			
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
			g2.setColor(new Color(0, 0, 0, 0));
			g2.drawLine(x, img.getHeight(), x, 0);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			

			
			g2.setColor(new Color(255, 0, 0, 70));
			g2.drawLine(x, 0, x, topCut[x]);
			g2.drawLine(x, img.getHeight(), x, botCut[x]);

			//for(int i=0; i< topCut.length; i++) {
				//g2.drawLine(i, 0, i, topCut[i]);
				//g2.drawLine(i, img.getHeight(), i, botCut[i]);
			//}
			//int r = 5;
			//g2.fillOval(x-r, y-r, r*2, r*2);
			
		}
		
		return true;
	}

	@Override
	public void drawOnMap(Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}

	private Rectangle getRect() {
		
		Point lt = vField.traceSampleToScreen(new TraceSample(traceStart, sampleStart));
		Point rb = vField.traceSampleToScreen(new TraceSample(traceFinish, sampleFinish));
		return new Rectangle(lt.x, lt.y, rb.x - lt.x, rb.y - lt.y);
		
	}
	
	@Override
	public void drawOnCut(Graphics2D g2) {
		Rectangle rect = getRect(); 
	
		if(img != null) {
			g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
			
		}
		
		g2.setColor(Color.RED);
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		g2.setColor(Color.WHITE);
		g2.drawString(type, rect.x, rect.y-5);
	}

	@Override
	public boolean isPointInside(Point localPoint) {
		
		Rectangle rect = getRect();
		
		return rect.contains(localPoint);
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}
	
}
