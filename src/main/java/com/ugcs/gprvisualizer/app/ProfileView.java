package com.ugcs.gprvisualizer.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.function.Consumer;

import com.ugcs.gprvisualizer.event.FileOpenedEvent;
import com.ugcs.gprvisualizer.event.FileSelectedEvent;
import com.ugcs.gprvisualizer.event.WhatChanged;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.ProfileField;
import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.github.thecoldwine.sigrun.common.ext.TraceSample;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.draw.PrismDrawer;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.gpr.RecalculationController;
import com.ugcs.gprvisualizer.gpr.Settings;
import com.ugcs.gprvisualizer.math.HorizontalProfile;
import com.ugcs.gprvisualizer.math.ScanProfile;
import com.ugcs.gprvisualizer.ui.BaseSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Component
public class ProfileView implements InitializingBean, FileDataContainer {
	public static Stroke AMP_STROKE = new BasicStroke(1.0f);
	public static Stroke LEVEL_STROKE = new BasicStroke(2.0f);

	private static final float[] dash1 = {5.0f};
	private static final BasicStroke dashed = 
			new BasicStroke(1.0f, 
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
					10.0f, dash1, 0.0f);

	private final Model model;

	@Autowired
	private AuxElementEditHandler auxEditHandler;

	@Autowired
	private Navigator navigator;

	@Autowired
	private Saver saver;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	private PrismDrawer prismDrawer;
	
	private final ImageView imageView = new ImageView();
	private final VBox vbox = new VBox();
	private final ProfileScroll profileScroll;


	private BufferedImage img;
	private Image image;
	private double width;
	private double height;

	private double contrast = 50;

	private ContrastSlider contrastSlider;

	private ToggleButton auxModeBtn = new ToggleButton("aux");
	ToolBar toolBar = new ToolBar();
	
	private final Button zoomInBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ZOOM_IN, new Button());

	private Button zoomOutBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.ZOOM_OUT, new Button());

	private BaseObject selectedMouseHandler;
	private BaseObject scrollHandler;

	static Font fontB = new Font("Verdana", Font.BOLD, 8);
	static Font fontP = new Font("Verdana", Font.PLAIN, 8);

	private ChangeListener<Number> sliderListener 
		= new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, 
				Number oldValue, Number newValue) {
			if (Math.abs(newValue.intValue() - oldValue.intValue()) > 3) {
				repaintEvent();
			}
		}
	};

	/*private ChangeListener<Number> aspectSliderListener
		= new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable, 
				Number oldValue, Number newValue) {
			updateScroll();
			repaintEvent();
		}
	};*/

	public ProfileView(Model model) {
		this.model = model;

		profileScroll = new ProfileScroll(model, model.getProfileField());
		//hyperFinder = new HyperFinder(model);
		prismDrawer = new PrismDrawer(model);

		contrastSlider = new ContrastSlider(model.getSettings(), 
				sliderListener);
		
		//hyperbolaSlider = new HyperbolaSlider(model.getSettings(), 
		//		aspectSliderListener);
		
		//hyperGoodSizeSlider = new HyperGoodSizeSlider(model.getSettings(), 
		//		sliderListener);
		
		//middleAmplitudeSlider = new MiddleAmplitudeSlider(model.getSettings(), 
		//		sliderListener);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {

		initImageView();
		
		profileScroll.setChangeListener(new ChangeListener<Number>() {
			//TODO: fix with change listener
			Number currentValue;

			public void changed(ObservableValue<? extends Number> ov, 
					Number oldVal, Number newVal) {
				if (currentValue == null) { currentValue = newVal;}
				if (currentValue != null && newVal != null && Math.abs(newVal.intValue() - currentValue.intValue()) > 3) {
					currentValue = newVal;
					repaintEvent();
				}
			}
		});

		scrollHandler = new CleverViewScrollHandler(this);
		vbox.getChildren().addAll(imageView);

		prepareToolbar();
		zoomInBtn.setTooltip(new Tooltip("Zoom in flight profile"));
		zoomOutBtn.setTooltip(new Tooltip("Zoom out flight profile"));
		zoomInBtn.setOnAction(e -> {
			zoom(1, width / 2, height / 2, false);
		});
		zoomOutBtn.setOnAction(e -> {
			zoom(-1, width / 2, height / 2, false);
		});
	}

	private void prepareToolbar() {
		toolBar.setDisable(true);

		toolBar.getItems().addAll(saver.getToolNodes());
		toolBar.getItems().add(getSpacer());
		
		toolBar.getItems().addAll(auxEditHandler.getRightPanelTools());
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().addAll(navigator.getToolNodes());
		toolBar.getItems().add(getSpacer());

		toolBar.getItems().add(zoomInBtn);
		toolBar.getItems().add(zoomOutBtn);
		toolBar.getItems().add(getSpacer());
	}

	protected BufferedImage draw(int width, int height) {
		if (width <= 0 || height <= 0 || !model.isActive() || model.getGprTracesCount() == 0) {
			return null;
		}

		ProfileField field = getField();//new ProfileField(getField());

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

			//drawHyperliveView(field, g2);

			drawFileNames(height, field, g2);
		}

		//
		g2.dispose();
		///
		return bi;
	}

	private void drawAuxGraphics1(ProfileField field, Graphics2D g2) {
		int startTrace = field.getFirstVisibleTrace();
		int finishTrace = field.getLastVisibleTrace();

		Rectangle r = field.getClipMainRect();
		g2.setClip(r.x, r.y, r.width, r.height);

		drawFileProfiles(field, g2, startTrace, finishTrace);

		drawAmplitudeMapLevels(field, g2);
	}

	private void drawFileProfiles(ProfileField field, Graphics2D graphicsContext,
			int startTrace, int finishTrace) {

		int f1 = model.getFileManager().getGprFiles().indexOf(
				model.getSgyFileByTrace(startTrace));
		
		int f2 = model.getFileManager().getGprFiles().indexOf(
				model.getSgyFileByTrace(finishTrace));

		for (int i = f1; i <= f2; i++) {
			SgyFile currentFile = model.getFileManager().getGprFiles().get(i);
			
			if (currentFile.profiles != null) {
				// pf
				graphicsContext.setColor(new Color(50, 200, 250));
				graphicsContext.setStroke(AMP_STROKE);
				for (HorizontalProfile pf : currentFile.profiles) {
					drawHorizontalProfile(field, graphicsContext,
							currentFile.getOffset().getStartTrace(), pf, 0);
				}
			}

			// ground
			if (currentFile.getGroundProfile() != null) {
				graphicsContext.setColor(new Color(210, 105, 30));
				graphicsContext.setStroke(LEVEL_STROKE);
				drawHorizontalProfile(field, graphicsContext,
						currentFile.getOffset().getStartTrace(), currentFile.getGroundProfile(),
						shiftGround.intValue());
			}

			if (model.getSettings().showGreenLine && currentFile.algoScan != null) {

				graphicsContext.setColor(Color.GREEN);
				graphicsContext.setStroke(AMP_STROKE);

				drawScanProfile(field, graphicsContext,
						currentFile.getOffset().getStartTrace(), currentFile.algoScan);
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

		int y = (int) field.traceSampleToScreen(new TraceSample(0, model.getSettings().getLayer())).getY();
		g2.drawLine((int) -width / 2, y, (int) width / 2, y);

		int bottomSelectedSmp = model.getSettings().getLayer() + model.getSettings().hpage;
		int y2 = (int) field.traceSampleToScreen(new TraceSample(
				0, bottomSelectedSmp)).getY();
		
		g2.drawLine((int) -width / 2, y2, (int) width / 2, y2);
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
		Point2D p1 = field.traceSampleToScreenCenter(new TraceSample(
				startTraceIndex, pf.deep[0] + voffset));
		int max2 = 0;

		for (int i = 1; i < pf.deep.length; i++) {

			max2 = Math.max(max2, pf.deep[i] + voffset);

			Point2D p2 = field.traceSampleToScreenCenter(new TraceSample(
					startTraceIndex + i, max2));

			if (p2.getX() - p1.getX() > 0 || Math.abs(p2.getY() - p1.getY()) > 0) {
				g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
				p1 = p2;
				max2 = 0;
			}
		}
	}

	private void drawScanProfile(ProfileField field, Graphics2D g2, 
			int startTraceIndex, ScanProfile pf) {

		Point2D p1 = field.traceSampleToScreenCenter(new TraceSample(
				startTraceIndex, 0));
		int max2 = 0;
		int offsety = field.getMainRect().y;

		for (int i = 1; i < pf.intensity.length; i++) {

			max2 = Math.max(max2, (int) pf.intensity[i]);

			Point2D p2 = field.traceSampleToScreenCenter(new TraceSample(
					startTraceIndex + i, 0));
			
			//p2.y = max2;

			if (p2.getX() - p1.getX() > 2) {

				g2.drawLine((int) p1.getX(), (int) (offsety + p1.getY()), (int) p2.getX(), offsety + max2);

				p1 = p2;
				max2 = 0;
			}

		}
	}

	private void drawFileNames(int height, ProfileField field, Graphics2D g2) {

		SgyFile currentFile = model.getSgyFileByTrace(
				model.getProfileField().getMiddleTrace());

		int selectedX1 = 0;
		int selectedX2 = 0;
		Point2D p = null;
		Point2D p2 = null;

		int leftMargin = -getField().getMainRect().width / 2;

		g2.setStroke(AMP_STROKE);
		for (SgyFile fl : model.getFileManager().getGprFiles()) {

			p = field.traceSampleToScreen(new TraceSample(
					fl.getTraces().get(0).getIndexInSet(), 0));
			
			int lastTraceIndex = fl.getTraces().size() - 1;
			p2 = field.traceSampleToScreen(new TraceSample(
					fl.getTraces().get(lastTraceIndex).getIndexInSet(), 0));

			if (currentFile == fl) {
				g2.setColor(Color.YELLOW);
				g2.setFont(fontB);

				selectedX1 = (int) p.getX();
				selectedX2 = (int) p2.getX();
			} else {
				g2.setColor(Color.WHITE);
				g2.setFont(fontP);
			}
			/// separator
			if (p.getX() > -getField().getMainRect().width / 2) {
				g2.drawLine((int) p.getX(), 0, (int) p.getX(), height);
			}

			p = new Point2D(Math.max(p.getX(), leftMargin), p.getY());

			int iconImageWidth = ResourceImageHolder.IMG_CLOSE_FILE.getWidth(null);
			g2.setClip((int) p.getX(), 0, (int) (p2.getX() - p.getX() - iconImageWidth), 20);
			String fileName = (fl.isUnsaved() ? "*" : "") + fl.getFile().getName();
			g2.drawString(fileName, (int) p.getX() + 7, 11);
			g2.setClip(null);
		}

		if (p2 != null) {
			g2.drawLine((int) p2.getX(), 0, (int) p2.getX(), height);
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

	//center
	public Node getCenter() {

		VBox center = new VBox();
        ScrollPane centerScrollPane = new ScrollPane();
		centerScrollPane.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        centerScrollPane.setFitToWidth(true);
		centerScrollPane.setFitToHeight(true);
        centerScrollPane.setContent(model.getChartsContainer());

		center.getChildren().addAll(toolBar, profileScroll, centerScrollPane);

		vbox.setOnMouseClicked(event -> {
			select();
		});

		ChangeListener<Number> sp2SizeListener = (observable, oldValue, newValue) -> {
			if (Math.abs(newValue.intValue() - oldValue.intValue()) > 3) {
				this.setSize(center.getWidth() - 21, Math.max(400, vbox.getHeight()) - 4);
			}
		};

		center.widthProperty().addListener(sp2SizeListener);
		vbox.heightProperty().addListener(sp2SizeListener);

		profileScroll.widthProperty().bind(vbox.widthProperty());

		return center;
	}

	private void select() {
		model.selectAndScrollToChart(this);
	}

	@Override
	public Node getRootNode() {
		return vbox;//topPane;
	}

	@Override
	public void selectFile() {
		eventPublisher.publishEvent(new FileSelectedEvent(this, model.getFileManager().getGprFiles()));
	}

	public List<Node> getRight() {
		var contrastNode = contrastSlider.produce();
		//contrastNode.setDisable(!model.isActive());
		return List.of(contrastNode);
	}

	private MutableInt shiftGround = new MutableInt(0);

	private Node printHoughSlider;
	
	public List<Node> getRightSearch() {

		
		//Slider s;
		/*List<Node> lst = Arrays.asList(hyperbolaSlider.produce(), 
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
				
				printHoughSlider = SliderFactory.create("printHoughAindex", 
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
				}, 5),
				
				SliderFactory.create("verticalShift", 
						model.getSettings().printHoughVertShift, 
						-100, 250, 
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
		
		
		return lst;*/
		return List.of();
	}

	private void initImageView() {
		imageView.setOnScroll(event -> {
			int ch = (event.getDeltaY() > 0 ? 1 : -1);

			double ex = event.getSceneX();
			double ey = event.getSceneY();

			zoom(ch, ex, ey, event.isControlDown());

			event.consume(); // don't scroll the page
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
		Point2D t = getLocalCoords(ex, ey);

		TraceSample ts = getField().screenToTraceSample(t);

		if (justHorizont) {

			double realAspect = getField().getRealAspect()
					* (ch > 0 ? ProfileField.ASPECT_A : 
						1 / ProfileField.ASPECT_A);

			getField().setRealAspect(realAspect);

		} else {
			getField().setZoom(getField().getZoom() + ch);
		}

		////

		Point2D t2 = getLocalCoords(ex, ey);
		TraceSample ts2 = getField().screenToTraceSample(t2);

		getField().setMiddleTrace(getField().getMiddleTrace()
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

			Point2D p = getLocalCoords(event);

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

			Point2D p = getLocalCoords(event);

			//if (model.getSettings().getHyperliveview().booleanValue()) {
			//	TraceSample ts = getField().screenToTraceSample(p);
			//	hyperFinder.setPoint(ts);
			//	repaintEvent();
			//}

			if (selectedMouseHandler != null) {
				selectedMouseHandler.mouseMoveHandle(p, getField());
			} else {
				if (!auxEditHandler.mouseMoveHandle(p, getField())) {
					//do nothing
				}
			}
		}
	};

	private Point2D getLocalCoords(MouseEvent event) {
		return getLocalCoords(event.getSceneX(), event.getSceneY());
	}

	protected Point2D getLocalCoords(double x, double y) {
		Point2D sceneCoords = new Point2D(x, y);
		Point2D imgCoord = imageView.sceneToLocal(sceneCoords);
		Point2D p = new Point2D(imgCoord.getX() - getField().getMainRect().x
				- getField().getMainRect().width / 2,
				imgCoord.getY());
		return p;
	}

	protected EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {

			if (event.getClickCount() == 2) {
				// add tmp flag
				Point2D p = getLocalCoords(event);

				int traceIndex = getField().screenToTraceSample(p).getTrace();

				if (traceIndex >= 0 && traceIndex < model.getGprTracesCount()) {

					Trace trace = model.getGprTraces()
							.get(traceIndex);

					// select in MapView
					model.getMapField().setSceneCenter(
							trace.getLatLon());

					model.createClickPlace(trace.getFile(), trace);

					eventPublisher.publishEvent(new WhatChanged(this, WhatChanged.Change.mapscroll));
				}
			}
		}
	};

	protected EventHandler<MouseEvent> mousePressHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {

			Point2D p = getLocalCoords(event);
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
			new EventHandler<>() {
		@Override
		public void handle(MouseEvent event) {

			Point2D p = getLocalCoords(event);

			if (selectedMouseHandler != null) {
				selectedMouseHandler.mouseReleaseHandle(p, getField());

				selectedMouseHandler = null;
			}
		}
	};

	private void repaintEvent() {
		if (!model.isLoading() && model.getGprTracesCount() > 0) {
			controller.render();
		}
	}

	protected void repaint() {

		img = draw((int) width, (int) height);
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

	@EventListener(classes = WhatChanged.class)
	private void somethingChanged(WhatChanged changed) {
		repaintEvent();
		updateScroll();
	}

	@EventListener
	private void fileOpened(FileOpenedEvent event) {
		profileScroll.setVisible(model.isActive() && model.getGprTracesCount() > 0);
		//topPane.setVisible(model.isActive() && model.getGprTracesCount() > 0);
		vbox.setVisible(model.isActive() && model.getGprTracesCount() > 0);

		if (!vbox.isVisible()) {
			model.getChartsContainer().getChildren().remove(vbox);
		} else {
			if (!model.getChartsContainer().getChildren().contains(vbox)) {
				model.getChartsContainer().getChildren().add(vbox);
				vbox.setPrefHeight(Math.max(400, vbox.getScene().getHeight()));
				vbox.setMinHeight(Math.max(400, vbox.getScene().getHeight() / 2));
			}
		}

		toolBar.setDisable(!model.isActive());
		//hyperbolaSlider.updateUI();
	}

	private void updateScroll() {
		if (!model.isActive() || model.getGprTracesCount() == 0) {
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

	public ImageView getImageView() {
		return imageView;
	}

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

	/*public class HyperbolaSlider extends BaseSlider {

		public HyperbolaSlider(Settings settings, ChangeListener<Number> listenerExt) {
			super(settings, listenerExt);
			name = "Hyp.size";
			units = "";
			tickUnits = 10;
		}

		public void updateUI() {
			slider.setMax(70);
			slider.setMin(10);
			slider.setValue(settings.hyperkfc);
		}

		public int updateModel() {
			settings.hyperkfc = (int) slider.getValue();
			return (int) settings.hyperkfc;
		}
	}*/

	/*public class HyperGoodSizeSlider extends BaseSlider {

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
	}*/

	/*public class MiddleAmplitudeSlider extends BaseSlider {

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
	}*/

	public void setSize(double width, double height) {

		this.width = width;
		this.height = height;
		getField().setViewDimension(new Dimension((int) this.width, (int) this.height));
		System.out.println("setSize: " + width + "x" + height);
		repaintEvent();
	}

	BaseObject getMouseHandler() {
		if (auxModeBtn.isSelected()) {
			return auxEditHandler;
		} else {
			return scrollHandler;
		}
	}

	void setScrollHandler(BaseObject scrollHandler) {
		this.scrollHandler = scrollHandler;
	}

	protected ProfileField getField() {
		return model.getProfileField();
	}

	private Region getSpacer() {
		Region r3 = new Region();
		r3.setPrefWidth(7);
		return r3;
	}

	public Node getPrintHoughSlider() {
		return printHoughSlider;
	}

	public ProfileScroll getProfileScroll() {
		return profileScroll;
	}
	
}
