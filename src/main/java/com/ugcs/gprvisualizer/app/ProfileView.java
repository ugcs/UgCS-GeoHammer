package com.ugcs.gprvisualizer.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.ClickPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.RulerTool;
import com.ugcs.gprvisualizer.app.commands.CommandRegistry;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.PrismDrawer;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.math.HorizontalProfile;
import com.ugcs.gprvisualizer.math.HoughDiscretizer;
import com.ugcs.gprvisualizer.math.HoughScan;
import com.ugcs.gprvisualizer.math.HyperFinder;
import com.ugcs.gprvisualizer.math.ScanProfile;
import com.ugcs.gprvisualizer.ui.BaseSlider;
import com.ugcs.gprvisualizer.ui.SliderFactory;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Component
public class ProfileView implements SmthChangeListener {
	public static Stroke AMP_STROKE = new BasicStroke(1.0f);
	public static Stroke LEVEL_STROKE = new BasicStroke(2.0f);

	private static final float[] dash1 = {5.0f};
	private static final BasicStroke dashed = 
			new BasicStroke(1.0f, 
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
					10.0f, dash1, 0.0f);

	
	@Autowired
	protected Model model;
	
	@Autowired
	protected Broadcast broadcast;

	@Autowired
	private AuxElementEditHandler auxEditHandler;

	@Autowired
	private Navigator navigator;


	protected PrismDrawer prismDrawer;	
	
	protected ImageView imageView = new ImageView();
	protected VBox vbox = new VBox();
	protected Pane topPane = new Pane();

	protected BufferedImage img;
	protected Image image;
	protected int width;
	protected int height;

	protected double contrast = 50;

	private ContrastSlider contrastSlider;
	private HyperbolaSlider hyperbolaSlider;
	private HyperGoodSizeSlider hyperGoodSizeSlider;
	private MiddleAmplitudeSlider middleAmplitudeSlider;

	private ToggleButton auxModeBtn = new ToggleButton("aux");
	ToolBar toolBar = new ToolBar();
	
	private Button zoomInBtn = new Button("", 
			ResourceImageHolder.getImageView("zoom-in_20.png"));
	
	private Button zoomOutBtn = new Button("", 
			ResourceImageHolder.getImageView("zoom-out_20.png"));
	
	private ToggleButton showGreenLineBtn = new ToggleButton("", 
			ResourceImageHolder.getImageView("level.png"));

	private MouseHandler selectedMouseHandler;
	private MouseHandler scrollHandler;
	
	private HyperFinder hyperFinder;
	public ProfileScroll profileScroll;
	
	static Font fontB = new Font("Verdana", Font.BOLD, 8);
	static Font fontP = new Font("Verdana", Font.PLAIN, 8);

	private ChangeListener<Number> sliderListener 
		= new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, 
				Number oldValue, Number newValue) {
			repaintEvent();
		}
	};
	private ChangeListener<Number> aspectSliderListener 
		= new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, 
				Number oldValue, Number newValue) {
			updateScroll();
			repaintEvent();
		}
	};

	public ProfileView() {

	}
	
	@PostConstruct
	public void init() {
		zoomInBtn.setTooltip(new Tooltip("Zoom in flight profile"));
		zoomOutBtn.setTooltip(new Tooltip("Zoom out flight profile"));

		hyperFinder = new HyperFinder(model);
		prismDrawer = new PrismDrawer(model);
		contrastSlider = new ContrastSlider(model.getSettings(), 
				sliderListener);
		
		hyperbolaSlider = new HyperbolaSlider(model.getSettings(), 
				aspectSliderListener);
		
		hyperGoodSizeSlider = new HyperGoodSizeSlider(model.getSettings(), 
				sliderListener);
		
		middleAmplitudeSlider = new MiddleAmplitudeSlider(model.getSettings(), 
				sliderListener);
		
		initImageView();

		profileScroll = new ProfileScroll(model);
		
		profileScroll.setChangeListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, 
					Number oldVal, Number newVal) {
				repaintEvent();
			}
		});

		scrollHandler = new CleverViewScrollHandler(this);

		prepareToolbar();

		vbox.getChildren().addAll(toolBar, profileScroll, imageView);

		profileScroll.widthProperty().bind(topPane.widthProperty());

		zoomInBtn.setOnAction(e -> {
			zoom(1, width / 2, height / 2, false);

		});
		zoomOutBtn.setOnAction(e -> {
			zoom(-1, width / 2, height / 2, false);
		});

		showGreenLineBtn.setTooltip(new Tooltip("Show/hide anomaly probability chart"));
		showGreenLineBtn.setSelected(model.getSettings().showGreenLine);
		showGreenLineBtn.setOnAction(e -> {
			model.getSettings().showGreenLine = showGreenLineBtn.isSelected();
			//AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
			
			broadcast.notifyAll(new WhatChanged(Change.justdraw));
		});

		
	}

	public void prepareToolbar() {
		toolBar.setDisable(true);
		toolBar.getItems().addAll(auxEditHandler.getRightPanelTools());
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().addAll(navigator.getToolNodes());

		toolBar.getItems().add(getSpacer());
		toolBar.getItems().add(zoomInBtn);
		toolBar.getItems().add(zoomOutBtn);
		toolBar.getItems().add(getSpacer());
		toolBar.getItems().add(showGreenLineBtn);

		toolBar.getItems().add(CommandRegistry.createButton("Ruler", e -> {

			SgyFile file = model.getSgyFileByTrace(getField().getSelectedTrace());

			RulerTool fp = RulerTool.createRulerTool(getField(), file);

			fp.setSelected(true);

			List<BaseObject> lst = new ArrayList<>();
			lst.addAll(fp.getControls());
			lst.add(fp);
			model.setControls(lst);

			repaintEvent();

		}));
	}

	protected BufferedImage draw(int width, int height) {
		if (width <= 0 || height <= 0 || !model.isActive()) {
			return null;
		}

		ProfileField field = new ProfileField(getField());

		BufferedImage bi;
		if (img != null && img.getWidth() == width && img.getHeight() == height) {
			bi = img;
		} else {
			bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		

		Graphics2D g2 = (Graphics2D) bi.getGraphics();

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHints(rh);

		clearBitmap(bi, g2, field);

		new VerticalRulerDrawer(model).draw(g2, field);

		int[] buffer = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
		
		prismDrawer.draw(width, field, g2, buffer, getRealContrast());

		g2.translate(field.getMainRect().x + field.getMainRect().width / 2, 0);

		if (!controller.isEnquiued()) {
			// skip if another recalculation coming
			drawAuxGraphics1(field, g2);
		}

		drawAuxElements(field, g2);

		if (!controller.isEnquiued()) {
			// skip if another recalculation coming

			g2.setClip(field.getClipTopMainRect().x, 
					field.getClipTopMainRect().y, 
					field.getClipTopMainRect().width,
					field.getClipTopMainRect().height);

			drawHyperliveView(field, g2);

			drawFileNames(height, field, g2);
		}

		//
		g2.dispose();
		///
		return bi;
	}

	public void drawHyperliveView(ProfileField field, Graphics2D g2) {
		if (model.getSettings().getHyperliveview().booleanValue()) {
			hyperFinder.drawHyperbolaLine(g2, field);
		}
	}

	public void drawAuxGraphics1(ProfileField field, Graphics2D g2) {
		int startTrace = field.getFirstVisibleTrace();
		int finishTrace = field.getLastVisibleTrace();

		Rectangle r = field.getClipMainRect();
		g2.setClip(r.x, r.y, r.width, r.height);

		drawFileProfiles(field, g2, startTrace, finishTrace);

		drawAmplitudeMapLevels(field, g2);
	}

	public void drawFileProfiles(ProfileField field, Graphics2D g2, 
			int startTrace, int finishTrace) {

		int f1 = model.getFileManager().getFiles().indexOf(
				model.getSgyFileByTrace(startTrace));
		
		int f2 = model.getFileManager().getFiles().indexOf(
				model.getSgyFileByTrace(finishTrace));

		for (int i = f1; i <= f2; i++) {
			SgyFile f = model.getFileManager().getFiles().get(i);

			if (f.profiles != null) {
				// pf
				g2.setColor(new Color(50, 200, 250));
				g2.setStroke(AMP_STROKE);
				for (HorizontalProfile pf : f.profiles) {
					drawHorizontalProfile(field, g2, 
							f.getOffset().getStartTrace(), pf, 0);
				}
			}

			// ground
			if (f.groundProfile != null) {
				g2.setColor(new Color(210, 105, 30));
				g2.setStroke(LEVEL_STROKE);
				drawHorizontalProfile(field, g2, 
						f.getOffset().getStartTrace(), f.groundProfile,
						shiftGround.intValue());
			}

			if (model.getSettings().showGreenLine && f.algoScan != null) {

				g2.setColor(Color.GREEN);
				g2.setStroke(AMP_STROKE);

				drawScanProfile(field, g2, 
						f.getOffset().getStartTrace(), f.algoScan);
			}

		}
	}

	public double getRealContrast() {
		double contr = Math.pow(1.08, 140 - contrast);
		return contr;
	}

	private void drawAmplitudeMapLevels(ProfileField field, Graphics2D g2) {
		g2.setColor(Color.MAGENTA);
		g2.setStroke(dashed);

		int y = field.traceSampleToScreen(new TraceSample(0, model.getSettings().layer)).y;
		g2.drawLine(-width / 2, y, width / 2, y);

		int bottomSelectedSmp = model.getSettings().layer + model.getSettings().hpage;
		int y2 = field.traceSampleToScreen(new TraceSample(
				0, bottomSelectedSmp)).y;
		
		g2.drawLine(-width / 2, y2, width / 2, y2);

	}

	private void drawAuxElements(ProfileField field, Graphics2D g2) {

		boolean full = !controller.isEnquiued();

		for (BaseObject bo : model.getAuxElements()) {
			if (full || bo.isSelected()) {
				bo.drawOnCut(g2, field);
			}
		}
		if (model.getControls() != null) {
			for (BaseObject bo : model.getControls()) {
				bo.drawOnCut(g2, field);
			}
		}
	}

	private void clearBitmap(BufferedImage bi, Graphics2D g2, ProfileField field) {

		Rectangle mainRectRect = field.getMainRect();
		Rectangle topRuleRect = field.getTopRuleRect();
		Rectangle leftRuleRect = field.getLeftRuleRect();

		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(mainRectRect.x, mainRectRect.y, 
				mainRectRect.width, mainRectRect.height);

		g2.setPaint(new Color(45, 60, 100));
		g2.fillRect(topRuleRect.x, topRuleRect.y, 
				topRuleRect.width, topRuleRect.height);
		g2.setPaint(Color.white);
		g2.drawLine(topRuleRect.x, 
				topRuleRect.y + topRuleRect.height, 
				topRuleRect.x + topRuleRect.width,
				topRuleRect.y + topRuleRect.height);

		g2.setPaint(new Color(45, 60, 100));
		g2.fillRect(leftRuleRect.x, leftRuleRect.y, 
				leftRuleRect.width, leftRuleRect.height);
		g2.setPaint(Color.white);
		g2.drawLine(leftRuleRect.x + leftRuleRect.width, 
				leftRuleRect.y, 
				leftRuleRect.x + leftRuleRect.width,
				leftRuleRect.y + leftRuleRect.height);

	}

	private void drawHorizontalProfile(ProfileField field, Graphics2D g2, 
			int startTraceIndex, HorizontalProfile pf,
			int voffset) {

		g2.setColor(pf.color);
		Point p1 = field.traceSampleToScreenCenter(new TraceSample(
				startTraceIndex, pf.deep[0] + voffset));
		int max2 = 0;

		for (int i = 1; i < pf.deep.length; i++) {

			max2 = Math.max(max2, pf.deep[i] + voffset);

			Point p2 = field.traceSampleToScreenCenter(new TraceSample(
					startTraceIndex + i, max2));
			
			if (p2.x - p1.x > 2) {
				g2.drawLine(p1.x, p1.y, p2.x, p2.y);
				p1 = p2;
				max2 = 0;
			}
		}
	}

	private void drawScanProfile(ProfileField field, Graphics2D g2, 
			int startTraceIndex, ScanProfile pf) {

		Point p1 = field.traceSampleToScreenCenter(new TraceSample(
				startTraceIndex, 0));
		int max2 = 0;
		int offsety = field.getMainRect().y;

		for (int i = 1; i < pf.intensity.length; i++) {

			max2 = Math.max(max2, (int) pf.intensity[i]);

			Point p2 = field.traceSampleToScreenCenter(new TraceSample(
					startTraceIndex + i, 0));
			
			p2.y = max2;

			if (p2.x - p1.x > 2) {

				g2.drawLine(p1.x, offsety + p1.y, p2.x, offsety + p2.y);

				p1 = p2;
				max2 = 0;
			}

		}
	}

	private void drawFileNames(int height, ProfileField field, Graphics2D g2) {

		SgyFile currentFile = model.getSgyFileByTrace(
				model.getVField().getSelectedTrace());

		int selectedX1 = 0;
		int selectedX2 = 0;
		Point p = null;
		Point p2 = null;

		int leftMargin = -getField().getMainRect().width / 2;

		g2.setStroke(AMP_STROKE);
		for (SgyFile fl : model.getFileManager().getFiles()) {

			p = field.traceSampleToScreen(new TraceSample(
					fl.getTraces().get(0).indexInSet, 0));
			
			int lastTraceIndex = fl.getTraces().size() - 1;
			p2 = field.traceSampleToScreen(new TraceSample(
					fl.getTraces().get(lastTraceIndex).indexInSet, 0));

			if (currentFile == fl) {
				g2.setColor(Color.YELLOW);
				g2.setFont(fontB);

				selectedX1 = p.x;
				selectedX2 = p2.x;
			} else {
				g2.setColor(Color.WHITE);
				g2.setFont(fontP);
			}
			/// separator
			if (p.x > -getField().getMainRect().width / 2) {
				g2.drawLine(p.x, 0, p.x, height);
			}

			p.x = Math.max(p.x, leftMargin);

			int iconImageWidth = ResourceImageHolder.IMG_CLOSE_FILE.getWidth(null);
			g2.setClip(p.x, 0, p2.x - p.x - iconImageWidth, 20);
			String fileName = (fl.isUnsaved() ? "*" : "") + fl.getFile().getName();
			g2.drawString(fileName, p.x + 7, 11);
			g2.setClip(null);
		}

		if (p2 != null) {
			g2.drawLine(p2.x, 0, p2.x, height);
		}

		if (currentFile != null) {
			g2.setColor(Color.YELLOW);
			g2.setStroke(LEVEL_STROKE);
			if (selectedX1 >= leftMargin) {
				g2.drawLine(selectedX1, 0, selectedX1, height);
			}
			g2.drawLine(selectedX2, 0, selectedX2, height);
		}
	}


	public Node getCenter() {

		ChangeListener<Number> sp2SizeListener = (observable, oldValue, newValue) -> {
			this.setSize((int) (topPane.getWidth()), (int) (topPane.getHeight()));
		};
		topPane.widthProperty().addListener(sp2SizeListener);
		topPane.heightProperty().addListener(sp2SizeListener);

		topPane.getChildren().add(vbox);

		return topPane;
	}

	public List<Node> getRight() {

		return Arrays.asList(
				contrastSlider.produce());
	}

	MutableInt shiftGround = new MutableInt(0);

	public List<Node> getRightSearch() {

		return Arrays.asList(hyperbolaSlider.produce(), 
				hyperGoodSizeSlider.produce(), 
				middleAmplitudeSlider.produce(),

				SliderFactory.create("shift ground", shiftGround, 0, 100, 
						new ChangeListener<Number>() {

					@Override
					public void changed(
						ObservableValue<? extends Number> observable,
						Number oldValue,
						Number newValue) {
						
						repaintEvent();

					}
				}, 20),
				
				SliderFactory.create("printHoughAindex", 
						model.getSettings().printHoughAindex, 
						0, HoughDiscretizer.DISCRET_SIZE - 1, 
						new ChangeListener<Number>() {
					@Override
					public void changed(
						ObservableValue<? extends Number> observable, 
						Number oldValue,
						Number newValue) {
						
						repaintEvent();
					}
				}, 5)
				);
	}

	protected void initImageView() {
		imageView.setOnScroll(event -> {
			int ch = (event.getDeltaY() > 0 ? 1 : -1);

			double ex = event.getSceneX();
			double ey = event.getSceneY();

			zoom(ch, ex, ey, event.isControlDown());
		});

		imageView.setOnMousePressed(mousePressHandler);
		imageView.setOnMouseReleased(mouseReleaseHandler);
		imageView.setOnMouseMoved(mouseMoveHandler);
		imageView.setOnMouseClicked(mouseClickHandler);
		imageView.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
		imageView.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseMoveHandler);
		imageView.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, dragReleaseHandler);
	}

	private void zoom(int ch, double ex, double ey, boolean justHorizont) {
		Point t = getLocalCoords(ex, ey);

		TraceSample ts = getField().screenToTraceSample(t);

		if (justHorizont) {

			double realAspect = getField().getAspectReal()
					* (ch > 0 ? ProfileField.ASPECT_A : 
						1 / ProfileField.ASPECT_A);

			getField().setAspectReal(realAspect);

		} else {
			getField().setZoom(getField().getZoom() + ch);
		}

		////

		Point t2 = getLocalCoords(ex, ey);
		TraceSample ts2 = getField().screenToTraceSample(t2);

		getField().setSelectedTrace(getField().getSelectedTrace()
				- (ts2.getTrace() - ts.getTrace()));

		int starts = getField().getStartSample() - (ts2.getSample() - ts.getSample());
		getField().setStartSample(starts);

		updateScroll();
		repaintEvent();

	}

	protected EventHandler<MouseEvent> dragDetectedHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent mouseEvent) {

			imageView.startFullDrag();

			imageView.setCursor(Cursor.CLOSED_HAND);
		}
	};

	protected EventHandler<MouseDragEvent> dragReleaseHandler = 
			new EventHandler<MouseDragEvent>() {
		@Override
		public void handle(MouseDragEvent event) {

			Point p = getLocalCoords(event);

			if (selectedMouseHandler != null) {
				selectedMouseHandler.mouseReleaseHandle(p, getField());
				selectedMouseHandler = null;
			}

			imageView.setCursor(Cursor.DEFAULT);

			event.consume();
		}
	};

	protected EventHandler<MouseEvent> mouseMoveHandler = 
			new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			Point p = getLocalCoords(event);

			if (model.getSettings().getHyperliveview().booleanValue()) {
				TraceSample ts = getField().screenToTraceSample(p);
				hyperFinder.setPoint(ts);
				repaintEvent();
			} 
			
			{

				if (selectedMouseHandler != null) {

					selectedMouseHandler.mouseMoveHandle(p, getField());
				} else {
					if (!auxEditHandler.mouseMoveHandle(p, getField())) {
						//do nothing
					}
				}
			}
		}
	};

	private Point getLocalCoords(MouseEvent event) {

		return getLocalCoords(event.getSceneX(), event.getSceneY());

	}

	protected Point getLocalCoords(double x, double y) {
		javafx.geometry.Point2D sceneCoords = new javafx.geometry.Point2D(x, y);
		javafx.geometry.Point2D imgCoord = imageView.sceneToLocal(sceneCoords);
		Point p = new Point((int) (imgCoord.getX() - getField().getMainRect().x
				- getField().getMainRect().width / 2),
				(int) (imgCoord.getY()));
		return p;
	}

	protected EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {

			if (event.getClickCount() == 2) {
				// add tmp flag
				Point p = getLocalCoords(event);

				int trace = getField().screenToTraceSample(p).getTrace();

				if (trace >= 0 && trace < model.getTracesCount()) {

					// select in MapView
					model.getField().setSceneCenter(
							model.getFileManager().getTraces()
							.get(trace).getLatLon());

					createTempPlace(model, trace);

					broadcast.notifyAll(new WhatChanged(Change.mapscroll));
				}
			}
		}
	};

	public static void createTempPlace(Model model, int trace) {

		ClickPlace fp = new ClickPlace(trace);
		fp.setSelected(true);
		model.setControls(Arrays.asList(fp));
	}

	protected EventHandler<MouseEvent> mousePressHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {

			Point p = getLocalCoords(event);
			if (auxEditHandler.mousePressHandle(p, getField())) {
				selectedMouseHandler = auxEditHandler;
			} else if (scrollHandler.mousePressHandle(p, getField())) {
				selectedMouseHandler = scrollHandler;
			} else {
				selectedMouseHandler = null;
			}

			imageView.setCursor(Cursor.CLOSED_HAND);
		}
	};

	protected EventHandler<MouseEvent> mouseReleaseHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {

			Point p = getLocalCoords(event);

			if (selectedMouseHandler != null) {
				selectedMouseHandler.mouseReleaseHandle(p, getField());

				selectedMouseHandler = null;
			}
		}
	};

	protected void repaintEvent() {
		if (!model.isLoading()) {
			controller.render();
		}
	}

	protected void repaint() {

		img = draw(width, height);
		if (img != null) {
			image = SwingFXUtils.toFXImage(img, null);
		} else {
			image = null;
		}
		updateWindow();
	}

	protected void updateWindow() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				imageView.setImage(image);
			}
		});
	}

	@Override
	public void somethingChanged(WhatChanged changed) {

		if (changed.isFileopened()) {
			profileScroll.setVisible(model.isActive());
			toolBar.setDisable(!model.isActive());
		}

		repaintEvent();
		updateScroll();
	}

	private void updateScroll() {
		if (!model.isActive()) {
			return;
		}

		profileScroll.recalc();
	}

	private RecalculationController controller = 
			new RecalculationController(new Consumer<Void>() {

		@Override
		public void accept(Void level) {

			repaint();

		}

	});

	public class ContrastSlider extends BaseSlider {

		public ContrastSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Contrast";
			units = "";
			tickUnits = 25;
		}

		public void updateUI() {
			slider.setMax(100);
			slider.setMin(0);
			slider.setValue(contrast);
		}

		public int updateModel() {
			contrast = (int) slider.getValue();
			return (int) contrast;
		}
	}

	public class AspectSlider extends BaseSlider {

		public AspectSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Aspect";
			units = "";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(30);
			slider.setMin(-30);
			slider.setValue(getField().getAspect());
		}

		public int updateModel() {
			getField().setAspect((int) slider.getValue());
			return (int) getField().getAspect();
		}
	}

	public class HyperbolaSlider extends BaseSlider {

		public HyperbolaSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Hyperbola";
			units = "";
			tickUnits = 200;
		}

		public void updateUI() {
			slider.setMax(400);
			slider.setMin(2);
			slider.setValue(settings.hyperkfc);
		}

		public int updateModel() {
			settings.hyperkfc = (int) slider.getValue();
			return (int) settings.hyperkfc;
		}
	}

	public class HyperGoodSizeSlider extends BaseSlider {

		public HyperGoodSizeSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Sensitivity";
			units = "%";
			tickUnits = 10;
		}

		public void updateUI() {
			
			slider.setMajorTickUnit(1);
			slider.setShowTickLabels(true);
			slider.setSnapToTicks(true);			
			//slider.set
			slider.setMax(100);
			slider.setMin(0);
			slider.setValue(settings.hyperSensitivity.intValue());
		}

		public int updateModel() {
			settings.hyperSensitivity.setValue((int) slider.getValue());
			return (int) settings.hyperSensitivity.intValue();
		}
	}

	public class MiddleAmplitudeSlider extends BaseSlider {

		public MiddleAmplitudeSlider(Settings settings,
				ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Middle amp";
			units = "";
			tickUnits = 5200;
		}

		public void updateUI() {
			slider.setMax(91000);
			slider.setMin(-91000);
			slider.setValue(settings.hypermiddleamp);
		}

		public int updateModel() {
			settings.hypermiddleamp = (int) slider.getValue();
			return (int) settings.hypermiddleamp;
		}
	}

	public void setSize(int width, int height) {

		this.width = width;
		this.height = height;
		getField().setViewDimension(new Dimension(this.width, this.height));

		repaintEvent();
	}

	MouseHandler getMouseHandler() {
		if (auxModeBtn.isSelected()) {
			return auxEditHandler;
		} else {
			return scrollHandler;
		}
	}

	void setScrollHandler(MouseHandler scrollHandler) {
		this.scrollHandler = scrollHandler;
	}

	protected ProfileField getField() {
		return model.getVField();
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}

	public AuxElementEditHandler getAuxEditHandler() {
		return auxEditHandler;
	}

}
