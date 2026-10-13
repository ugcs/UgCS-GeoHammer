package com.ugcs.geohammer.chart;

import com.ugcs.geohammer.view.Listeners;
import com.ugcs.geohammer.view.ResourceImageHolder;
import com.ugcs.geohammer.view.Views;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class ChartHeader extends HBox {

    private static final double ICON_HEIGHT = 24;

    private final Label name = new Label();

    private final Button closeButton = new Button();

    private final ToggleButton collapseToggle = new ToggleButton();

    private final Tooltip collapseTooltip = new Tooltip();

    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    public ChartHeader() {
        closeButton.getStyleClass().add("icon");
        closeButton.setGraphic(ResourceImageHolder.getImageView("close.png"));
        closeButton.setTooltip(new Tooltip("Close"));
        consumeClicks(closeButton);

        name.getStyleClass().add("chart-name");

        collapseToggle.getStyleClass().add("flat");
        collapseToggle.setTooltip(collapseTooltip);
        collapseToggle.selectedProperty().bindBidirectional(collapsed);
        consumeClicks(collapseToggle);

        Listeners.onChange(collapsed, this::updateToggles);
        updateToggles(collapsed.get());

        // controls consume their clicks, so this only fires on the free header area
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                collapsed.set(!collapsed.get());
            }
        });

        setSpacing(10);
        setPadding(new Insets(10, 10, 0, 10));
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(closeButton, name, Views.createSpacer(), collapseToggle);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    private void updateToggles(boolean collapsed) {
        String tooltip = collapsed ? "Expand" : "Collapse";
        collapseTooltip.setText(tooltip);
        ResourceImageHolder.setButtonImage(
                collapsed ? ResourceImageHolder.EYE_OFF : ResourceImageHolder.EYE,
                ICON_HEIGHT,
                collapseToggle);
    }

    // header controls must not select the chart
    private static void consumeClicks(Node node) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
    }
}
