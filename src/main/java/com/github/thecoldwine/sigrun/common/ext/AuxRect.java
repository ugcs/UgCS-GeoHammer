package com.github.thecoldwine.sigrun.common.ext;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableInt;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ugcs.gprvisualizer.app.auxcontrol.AlignRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.DragAnchor;
import com.ugcs.gprvisualizer.app.auxcontrol.ToggleButton;
import com.ugcs.gprvisualizer.math.NumberUtils;

import javafx.scene.control.ChoiceDialog;

public class AuxRect implements BaseObject {

//	public static final String TYPES[] = { 
//			"hyperbola", 
//			"air", 
//			"ground",  
//            "ground line"}; 
	
	public static final Color maskColor = new Color(50, 0, 255, 70);
	
	//VerticalCutField vField;
	
	MutableInt traceStart = new MutableInt();
	MutableInt traceFinish = new MutableInt();
	MutableInt sampleStart = new MutableInt();
	MutableInt sampleFinish = new MutableInt();
	VerticalCutPart offset;
	
	BufferedImage img;

	DragAnchor top;
	DragAnchor bottom;
	DragAnchor left;
	DragAnchor right;
	ToggleButton lock;
	ToggleButton selectType;
	
	static int TRACE_W = 40;
	static int SAMPLE_W = 20;
	int[] topCut;
	int[] botCut;
	boolean sideTop = true;
	boolean locked = false;
	private AreaType type = AreaType.Hyperbola;
	
	public void saveTo(JSONObject json) {
		json.put("traceStart", traceStart);
		json.put("traceFinish", traceFinish);
		json.put("sampleStart", sampleStart);
		json.put("sampleFinish", sampleFinish);
		json.put("type", getType().toString());
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

	public void setSampleStart(int sampleStart) {
		this.sampleStart.setValue(sampleStart);
		//this.sampleStart = sampleStart;
	}

	public void setSampleFinish(int sampleFinish) {
		this.sampleFinish.setValue(sampleFinish);
		//this.sampleFinish = sampleFinish;
	}
	public int getSampleStart() {
		return this.sampleStart.getValue();

	}

	public int getSampleFinish() {
		return this.sampleFinish.getValue();
	}
	
	public void setTraceStart(int traceStart) {
		this.traceStart.setValue(traceStart);
	}

	public int getTraceStartLocal() {
		return this.traceStart.getValue();
	}

	public int getTraceStartGlobal() {
		return offset.localToGlobal(this.traceStart.getValue());
	}

	public void setTraceFinish(int traceStart) {
		this.traceFinish.setValue(traceStart);
	}

	public int getTraceFinishLocal() {
		return this.traceFinish.getValue();
	}
	
	public int getTraceFinishGlobal() {
		return offset.localToGlobal(this.traceFinish.getValue());
	}
	
	public AuxRect(
			int traceStart,
			int traceFinish,
			int sampleStart,
			int sampleFinish,
			VerticalCutPart offset) {
		//this.vField = vField;
		this.offset = offset;
		
		setTraceStart(offset.globalToLocal(traceStart));
		setTraceFinish(offset.globalToLocal(traceFinish));
		setSampleStart(sampleStart);
		setSampleFinish(sampleFinish);
		
		initDragAnchors();	
		clearCut();
		updateMaskImg();
	}
	
	public AuxRect(int traceCenter, int sampleCenter, VerticalCutPart offset) {
		this(		
			traceCenter - TRACE_W,
			traceCenter + TRACE_W,
			sampleCenter - SAMPLE_W,
			sampleCenter + SAMPLE_W,
			offset);
		
	}
	
	
	public AuxRect(JSONObject json, VerticalCutPart offset) {	
		this.offset = offset;
		
		setTraceStart((int)(long)(Long)json.get("traceStart"));
		setTraceFinish((int)(long)(Long)json.get("traceFinish"));
		setSampleStart((int)(long)(Long)json.get("sampleStart"));
		setSampleFinish((int)(long)(Long)json.get("sampleFinish"));
		
		setType(AreaType.valueOf((String)json.get("type")));
		locked = (Boolean)json.get("locked");
		
		JSONArray ar = (JSONArray)json.get("topCut");

		topCut = new int[ar.size()];
		for(int i=0; i<ar.size(); i++) {
			
			topCut[i] = (int)(long)(Long)ar.get(i);
		}

		ar = (JSONArray)json.get("botCut");
		botCut = new int[ar.size()];
		for(int i=0; i<ar.size(); i++) {
			botCut[i] = (int)(long)(Long)ar.get(i);
		}
		
		initDragAnchors();
		updateMaskImg();
		
	}
	

	private void initDragAnchors() {
		selectType = new ToggleButton(traceStart, sampleStart, 
				ResourceImageHolder.IMG_CHOOSE,
				ResourceImageHolder.IMG_CHOOSE,
				new AlignRect(-1, -1), offset) {

			public void signal(Object obj) {
		        ChoiceDialog<AreaType> dialog = new ChoiceDialog<AreaType>(getType(), AreaType.values()); 
		        Optional<AreaType> result = dialog.showAndWait();
		        result.ifPresent(book -> {
		            setType(book);
		        });				
			}			
			public int getTrace() {
				return (left.getTrace()) ;
			}
			public int getSample() {
				return (top.getSample()) ;
			}
		};
		
		lock = new ToggleButton(traceFinish, sampleStart, 
				ResourceImageHolder.IMG_LOCK,
				ResourceImageHolder.IMG_UNLOCK,
				new AlignRect(-1, -1), offset) {

			public void signal(Object obj) {
				locked = (Boolean)obj;
				
				left.setVisible(!locked); 
				top.setVisible(!locked);
				right.setVisible(!locked);
				bottom.setVisible(!locked);
				
			}			
//			public int getTrace() {
//				return (right.getTrace()) ;
//			}
			public int getSample() {
				return (top.getSample()) ;
			}
			
		};
		
		top = new DragAnchor(new MutableInt(), sampleStart, ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				//sampleStart = top.getSample();
				
				
				
				sampleStart.setValue(
						NumberUtils.norm(sampleStart.getValue(), 0, sampleFinish.getValue()-2));
						
						//Math.min(sampleStart.getValue(), sampleFinish.getValue()-2));
				
				clearCut();
				updateMaskImg();
			}
			
			public int getTrace() {
				return (left.getTrace() + right.getTrace()) / 2;
			}
		};
		
		bottom = new DragAnchor(new MutableInt(), sampleFinish, ResourceImageHolder.IMG_VER_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				//sampleFinish = bottom.getSample();
				sampleFinish.setValue( 
					NumberUtils.norm(sampleFinish.getValue(), sampleStart.getValue()+2, offset.getMaxSamples()));
				
				clearCut();
				updateMaskImg();
			}
			public int getTrace() {
				return (left.getTrace() + right.getTrace()) / 2;
			}
		};
		left = new DragAnchor(traceStart, new MutableInt(), ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				//traceStart = left.getTrace();
				traceStart.setValue( 
						NumberUtils.norm(traceStart.getValue(), 0, traceFinish.getValue()-2));
				
				clearCut();
				updateMaskImg();
			}
			public int getSample() {
				return (top.getSample() + bottom.getSample()) / 2;
			}
		};
		
		right = new DragAnchor(traceFinish, new MutableInt(), ResourceImageHolder.IMG_HOR_SLIDER, AlignRect.CENTER, offset) {
			public void signal(Object obj) {
				//traceFinish = right.getTrace();
				traceFinish.setValue( 
						NumberUtils.norm(traceFinish.getValue(), traceStart.getValue()+2, offset.getTraces()));
						//Math.max(traceFinish.getValue(), ));
				
				clearCut();
				updateMaskImg();
			}
			public int getSample() {
				return (top.getSample() + bottom.getSample()) / 2;
			}
		};
	}

	private void updateMaskImg() {
		
		int width = getTraceFinishLocal() - getTraceStartLocal();
		int height = getSampleHeight();
		
		if(img == null || width != img.getWidth() || height != img.getHeight()) {
		
			img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_4BYTE_ABGR);
			
			Graphics2D g2 = (Graphics2D)img.getGraphics();
			//g2.drawLine(0,0,width, height);
			
			for(int x=0; x<topCut.length; x++) {
				
				g2.setColor(maskColor);
				g2.drawLine(x, 0, x, topCut[x]);
				g2.drawLine(x, img.getHeight(), x, botCut[x]);
				
			}
		}
	}

	private void clearCut() {
		
		int width = Math.max(1, getTraceFinishLocal() - getTraceStartLocal());
		int height = getSampleHeight();
		//if(topCut == null || topCut.length != width) {
			topCut = new int[width];
		//}
		//if(botCut == null || botCut.length != width) {
			botCut = new int[width];
			Arrays.fill(botCut, height);
		//}
	}
	
	public List<BaseObject> getControls(){
		
		return //locked ? 
			//Arrays.asList(lock) :
			Arrays.asList(left, top, right, bottom, lock, selectType); 
				
	}

	@Override
	public boolean mousePressHandle(Point localPoint, VerticalCutField vField) {

		if(isPointInside(localPoint, vField)){
			
		
			TraceSample ts = vField.screenToTraceSample(localPoint, offset);
			int x = ts.getTrace() - getTraceStartLocal();
			int y = ts.getSample() - getSampleStart();
			if(x>=0 && x < topCut.length) {
				
				int top_dst = Math.abs(topCut[x] - y);
				int bot_dst = Math.abs(botCut[x] - y);

				sideTop = top_dst < bot_dst;
				lastX = -1;
			}
			return true;
		}
		
		return false;
	}

	@Override
	public boolean mouseReleaseHandle(Point localPoint, VerticalCutField vField) {

		return false;
	}

	int lastX=-1;
	@Override
	public boolean mouseMoveHandle(Point point, VerticalCutField vField) {

		if(img == null) {
			return false;
		}
		
		TraceSample ts = vField.screenToTraceSample(point, offset);
		
		if(locked) {		
			drawCutOnImg(ts);
		}else {
			
			int half_width = (getTraceFinishLocal() - getTraceStartLocal())/2;
			int half_height = getSampleHeight()/2;
			
			int tr = NumberUtils.norm(ts.getTrace(), half_width, offset.getTraces()-half_width);
			int sm = NumberUtils.norm(ts.getSample(), half_height, offset.getMaxSamples()-half_height);
			
			setTraceStart(tr - half_width);
			setTraceFinish(tr + half_width);
			setSampleStart(sm - half_height);
			setSampleFinish(sm + half_height);
			
		}
		
		return true;
	}

	private void drawCutOnImg(TraceSample ts) {
		int x = ts.getTrace() - getTraceStartLocal();
		int y = ts.getSample() - getSampleStart();
		
		int height = getSampleHeight();
		y = Math.max(Math.min(y, height), 0);
		
		if(x>=0 && x < topCut.length) {
			if(lastX == -1 || Math.abs(lastX-x) > 5) {
				lastX = x;
			}
			
			for(int i=Math.min(lastX, x); i<=Math.max(lastX, x); i++) {
				
				drawColumn(i, y);
				
			}
			
			lastX = x;
		}
	}

	private int getSampleHeight() {
		return getSampleFinish() - getSampleStart();
	}

	private void drawColumn(int x, int y) {
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
		
		g2.setColor(maskColor);
		g2.drawLine(x, 0, x, topCut[x]);
		g2.drawLine(x, img.getHeight(), x, botCut[x]);
	}

	@Override
	public void drawOnMap(Graphics2D g2, Field hField) {
		// TODO Auto-generated method stub
		
	}

	public Rectangle getRect(VerticalCutField vField) {
		
		Point lt = vField.traceSampleToScreen(new TraceSample(getTraceStartGlobal(), getSampleStart()));
		Point rb = vField.traceSampleToScreen(new TraceSample(getTraceFinishGlobal(), getSampleFinish()));
		return new Rectangle(lt.x, lt.y, rb.x - lt.x, rb.y - lt.y);
		
	}
	
	@Override
	public void drawOnCut(Graphics2D g2, VerticalCutField vField) {
		Rectangle rect = getRect(vField); 
	
		if(img != null) {
			g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
			
		}
		
		g2.setColor(Color.RED);
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		g2.setColor(Color.WHITE);
		g2.drawString(getType().getName(), rect.x, rect.y-5);
	}

	@Override
	public boolean isPointInside(Point localPoint, VerticalCutField vField) {
		
		Rectangle rect = getRect(vField);
		
		return rect.contains(localPoint);
	}

	@Override
	public void signal(Object obj) {
		// TODO Auto-generated method stub
		
	}

	public AreaType getType() {
		return type;
	}

	public void setType(AreaType type) {
		this.type = type;
	}

	@Override
	public boolean mousePressHandle(Point2D point, Field field) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
