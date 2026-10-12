package com.ugcs.geohammer.map.layer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.ugcs.geohammer.map.RenderQueue;
import com.ugcs.geohammer.map.provider.GoogleTileProvider;
import com.ugcs.geohammer.map.provider.HereMapProvider;
import com.ugcs.geohammer.map.provider.MacrostratTileProvider;
import com.ugcs.geohammer.map.provider.MacrostratGeologyService;
import com.ugcs.geohammer.map.provider.OsmTileProvider;
import com.ugcs.geohammer.map.provider.Usgs3depTileProvider;
import com.ugcs.geohammer.map.provider.XyzMapProvider;
import com.ugcs.geohammer.model.ActivationPolicy;
import com.ugcs.geohammer.model.ToolNode;
import com.ugcs.geohammer.model.event.FileOpenedEvent;
import com.ugcs.geohammer.model.event.WhatChanged;
import com.ugcs.geohammer.BuildInfo;
import com.ugcs.geohammer.Settings;
import javafx.geometry.Point2D;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.model.MapField;
import com.ugcs.geohammer.view.ResourceImageHolder;
import com.ugcs.geohammer.view.Dialogs;
import com.ugcs.geohammer.view.status.Status;
import com.ugcs.geohammer.model.Model;

import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.util.concurrent.CompletableFuture;

@Component
public class SatelliteMap extends BaseLayer implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(SatelliteMap.class);

	private final Model model;
	private final Status status;
	private final Settings settings;
	private final BuildInfo buildInfo;
	private final MacrostratGeologyService geologyService;

	private int lastTileZoom = -1;

	public SatelliteMap(Model model, Status status, Settings settings, BuildInfo buildInfo,
						MacrostratGeologyService geologyService) {
		this.model = model;
		this.status = status;
		this.settings = settings;
		this.buildInfo = buildInfo;
		this.geologyService = geologyService;
		identifyGeologyButton.setTooltip(new Tooltip("Identify geology at the next map click"));
		identifyGeologyButton.setDisable(true);
		identifyGeologyButton.setOnAction(event -> status.showMessage(
				identifyGeologyButton.isSelected() ? "Click the map to identify geology" : "", "Geology"));
	}

	private LatLon click;

	private RenderQueue recalcQueue;

	@Override
	public void afterPropertiesSet() throws Exception {
		recalcQueue = new RenderQueue(model) {
			public void draw(BufferedImage image, MapField field) {
				if (field.getMapProvider() != null) {
					this.setRenderImage(field.getMapProvider().loadimg(field));
				}
			}

			public void onReady() {
				getRepaintListener().repaint();
			}

			@Override
			protected void actualizeRenderImage() {
			}
		};

		optionsMenuBtn.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4, menuItem5, menuItem6);
		ToggleGroup toggleGroup = new ToggleGroup();
		menuItem1.setToggleGroup(toggleGroup);
		menuItem2.setToggleGroup(toggleGroup);
		menuItem3.setToggleGroup(toggleGroup);
        menuItem4.setToggleGroup(toggleGroup);
        menuItem5.setToggleGroup(toggleGroup);
		menuItem6.setToggleGroup(toggleGroup);

		menuItem1.setOnAction(e -> {
			closeCurrentProvider();
			model.getMapField().setMapProvider(new XyzMapProvider(new GoogleTileProvider()));
			setActive(model.getMapField().getMapProvider() != null);
			clearTiles();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
		});

		menuItem2.setOnAction(e -> {
			closeCurrentProvider();
			model.getMapField().setMapProvider(new HereMapProvider(settings.getString("maps", "here_api_key")));
			setActive(model.getMapField().getMapProvider() != null);
			clearTiles();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
		});

        menuItem3.setOnAction(e -> {
			closeCurrentProvider();
			model.getMapField().setMapProvider(new XyzMapProvider(new OsmTileProvider(buildInfo.getBuildVersion())));
			setActive(model.getMapField().getMapProvider() != null);
			clearTiles();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
        });

        menuItem4.setOnAction(e -> {
            closeCurrentProvider();
            model.getMapField().setMapProvider(new XyzMapProvider(new MacrostratTileProvider(buildInfo.getBuildVersion())));
            setActive(model.getMapField().getMapProvider() != null);
            clearTiles();
            model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
        });

		menuItem5.setOnAction(e -> {
			closeCurrentProvider();
			model.getMapField().setMapProvider(new XyzMapProvider(new Usgs3depTileProvider(buildInfo.getBuildVersion())));
			setActive(model.getMapField().getMapProvider() != null);
			clearTiles();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
		});

		menuItem2.setSelected(true);
		menuItem2.fire();

		menuItem6.setOnAction(e -> {
			closeCurrentProvider();
			model.getMapField().setMapProvider(null);
			setActive(model.getMapField().getMapProvider() != null);
			clearTiles();
			model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapzoom));
			updateIdentifyAvailability();
		});
	}	

	RadioMenuItem menuItem1 = new RadioMenuItem("google maps");
	RadioMenuItem menuItem2 = new RadioMenuItem("here.com");
	RadioMenuItem menuItem3 = new RadioMenuItem("open street map");
	RadioMenuItem menuItem4 = new RadioMenuItem("geologic map (Macrostrat)");
	RadioMenuItem menuItem5 = new RadioMenuItem("USGS 3DEP multidirectional hillshade");
	RadioMenuItem menuItem6 = new RadioMenuItem("turn off");

	private final ToggleButton identifyGeologyButton = new ToggleButton("Identify geology");


	private final MenuButton optionsMenuBtn = ResourceImageHolder.setButtonImage(ResourceImageHolder.MAP, new MenuButton());

	@Override
	public void setSize(Dimension size) {
		recalcQueue.setRenderSize(size);
	}

	@Override
	public void draw(Graphics2D g2, MapField currentField) {
		RenderQueue.Frame front = recalcQueue.getLastFrame();

		if (front != null && isActive()) {
			recalcQueue.drawWithTransform(g2, currentField, front);
		}		

		if (click != null) {
			Point2D p = currentField.latLonToScreen(click);

			g2.drawOval((int) p.getX() - 3, (int) p.getY() - 3, 7, 7);
		}
	}

	@EventListener
	private void somethingChanged(WhatChanged changed) {
		if (changed.isZoom()) {
			if (model.isActive()) {
				submitTiles();
			} else {
				clearTiles();
			}
		}		
	}

	@EventListener
	private void fileOpened(FileOpenedEvent event) {
		if (model.isActive()) {
			submitTiles();
		} else {
			clearTiles();
		}
	}

	private void submitTiles() {
		int intZoom = (int)model.getMapField().getZoom();
		if (intZoom != lastTileZoom) {
			recalcQueue.submit();
			lastTileZoom = intZoom;
		}
	}

	private void clearTiles() {
		recalcQueue.clear();
		lastTileZoom = -1;
	}

	MapField dragField = null;

	@Override
	public boolean mousePressed(Point2D point) {

		if (!model.getFileManager().isActive()) {
			return false;
		}

		click = model.getMapField().screenTolatLon(point);
		if (identifyGeologyButton.isSelected() && isGeologicMapSelected()) {
			identifyGeologyButton.setSelected(false);
			identify(click);
			return true;
		}

		dragField = new MapField(model.getMapField());

		status.showMessage(click.toString(), "Coordinates");

		getRepaintListener().repaint();

		return true;
	}

	private void identify(LatLon location) {
		status.showMessage("Querying Macrostrat geology…", "Geology");
		CompletableFuture.supplyAsync(() -> geologyService.identify(location)).whenComplete((units, error) -> {
			if (error != null) {
				Dialogs.showError("Identify geology", "Unable to identify geology at this location.");
				return;
			}
			if (units.isEmpty()) {
				Dialogs.showInformation("Identify geology", "Macrostrat has no mapped geology at this location.");
				return;
			}
			Dialogs.showInformation("Geology at " + location, formatGeology(units));
		});
	}

	private static String formatGeology(List<MacrostratGeologyService.GeologyUnit> units) {
		StringBuilder text = new StringBuilder();
		for (MacrostratGeologyService.GeologyUnit unit : units) {
			if (!text.isEmpty()) text.append("\n\n");
			text.append(unit.name().isBlank() ? "Unnamed unit" : unit.name());
			if (!unit.interval().isBlank()) text.append("\nAge: ").append(unit.interval());
			if (unit.topAge() != null && unit.bottomAge() != null) {
				text.append(" (%.0f–%.0f Ma)".formatted(unit.topAge(), unit.bottomAge()));
			}
			if (!unit.lithology().isBlank()) text.append("\nLithology: ").append(unit.lithology());
			if (!unit.description().isBlank()) text.append("\n").append(unit.description());
		}
		return text.toString();
	}

	private boolean isGeologicMapSelected() {
		return model.getMapField().getMapProvider() instanceof XyzMapProvider provider
				&& provider.usesProvider(MacrostratTileProvider.class);
	}

	private void updateIdentifyAvailability() {
		boolean enabled = isGeologicMapSelected();
		identifyGeologyButton.setDisable(!enabled);
		if (!enabled) identifyGeologyButton.setSelected(false);
	}

	@Override
	public boolean mouseRelease(Point2D point) {

		dragField = null;
		click = null;

		model.publishEvent(new WhatChanged(this, WhatChanged.Change.mapscroll));

		return true;
	}

	@Override
	public boolean mouseMove(Point2D point) {

		if (dragField == null) {
			return false;
		}

		LatLon newCenter = dragField.screenTolatLon(point);		
		double lat = dragField.getSceneCenter().getLatDgr() 
				+ click.getLatDgr() - newCenter.getLatDgr();
		double lon = dragField.getSceneCenter().getLonDgr() 
				+ click.getLonDgr() - newCenter.getLonDgr();

		model.getMapField().setSceneCenter(new LatLon(lat, lon));

		getRepaintListener().repaint();

		return true;
	}

	private void closeCurrentProvider() {
		if (model.getMapField().getMapProvider() instanceof Closeable mapProvider) {
			try {
				mapProvider.close();
			} catch (IOException e) {
				log.warn("Failed to close map provider", e);
			}
		}
	}

	@Override
	public List<ToolNode> getToolNodes() {
		HBox cnt = new HBox(optionsMenuBtn, identifyGeologyButton);
		return List.of(
				new ToolNode(cnt, ActivationPolicy.fileSelected())
		);
	}

}
