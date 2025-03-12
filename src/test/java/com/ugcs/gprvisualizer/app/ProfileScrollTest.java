package com.ugcs.gprvisualizer.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import com.ugcs.gprvisualizer.gpr.Model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.testfx.util.WaitForAsyncUtils.*;
import static org.testfx.util.WaitForAsyncUtils.asyncFx;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ProfileScrollTest extends FxRobot {

    @Mock
    private Model model;

    @Mock
    private ScrollableData scrollableData;

    private ProfileScroll profileScroll;
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        profileScroll = new ProfileScroll(model, scrollableData);
        profileScroll.setChangeListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //do nothing
            }
        });

        // Set up the initial scene
        VBox root = new VBox();
        root.getChildren().add(profileScroll);
        Scene scene = new Scene(root, 800, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testInitialization() {
        assertNotNull(profileScroll, "ProfileScroll should be initialized");
        assertTrue(profileScroll.isResizable(), "ProfileScroll should be resizable");
    }

    @Test
    void testResizing() {
        double expectedWidth = 800.0;
        double expectedHeight = 24.0;

        // Wait for JavaFX to process layout
        waitForFxEvents();

        assertEquals(expectedWidth, profileScroll.getWidth(), 
            "ProfileScroll width should match the expected width");
        assertEquals(expectedHeight, profileScroll.getHeight(), 
            "ProfileScroll height should match the expected height");
    }

    @Test
    void testMinMaxWidth() {
        double minWidth = profileScroll.minWidth(-1);
        double maxWidth = profileScroll.maxWidth(-1);

        assertEquals(50.0, minWidth, "Minimum width should be 50");
        assertEquals(Double.MAX_VALUE, maxWidth, "Maximum width should be Double.MAX_VALUE");
    }

    @Test
    void testCenterBarInitialization() {
        waitForFxEvents();

        assertNotNull(profileScroll.getCenterBar(), "Center bar should be initialized");
        assertTrue(profileScroll.getCenterBar().getWidth() > 0, 
            "Center bar should have positive width");
    }

    @Test
    void testLeftAndRightBars() {
        waitForFxEvents();

        assertNotNull(profileScroll.getLeftBar(), "Left bar should be initialized");
        assertNotNull(profileScroll.getRightBar(), "Right bar should be initialized");
        assertEquals(profileScroll.getHeight(), profileScroll.getLeftBar().getHeight(),
            "Left bar height should match ProfileScroll height");
        assertEquals(profileScroll.getHeight(), profileScroll.getRightBar().getHeight(),
            "Right bar height should match ProfileScroll height");
    }

    @Test
    void testMouseDragOnCenterBar() {
        // Setup mock behavior
        when(scrollableData.getTracesCount()).thenReturn(1000);
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(100);
        when(scrollableData.getMiddleTrace()).thenReturn(500);

        // Force initial layout
        profileScroll.recalc();
        waitForFxEvents();

        clickOn(profileScroll);
        waitForFxEvents();

        drag(profileScroll).dropBy(50, 0);
        waitForFxEvents();

        // Verify scrollable data was updated
        verify(scrollableData, atLeastOnce()).setMiddleTrace(anyInt());
    }

    @Test
    void testMouseDragOnLeftBar() {
        // Setup mock behavior
        when(scrollableData.getTracesCount()).thenReturn(1000);
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(100);
        when(scrollableData.getMiddleTrace()).thenReturn(500);

        // Force initial layout
        profileScroll.recalc();
        waitForFxEvents();

        // Get left bar bounds
        Rectangle leftBar = profileScroll.getLeftBar();
        Bounds boundsInScene = leftBar.localToScene(leftBar.getBoundsInLocal());

        Window window = stage.getScene().getWindow();
        double sceneX = window.getX() + boundsInScene.getMinX() + (boundsInScene.getWidth() / 2);
        double sceneY = window.getY() + boundsInScene.getMinY() + (boundsInScene.getHeight() / 2);
        Point2D leftPoint = new Point2D(sceneX, sceneY);

        // Move to left bar
        moveTo(leftPoint);

        // Press and hold
        press(MouseButton.PRIMARY);

        // Drag left by 50 pixels
        moveBy(-50, 0);

        // Drag right by 30 pixels
        moveBy(30, 0);

        // Release
        release(MouseButton.PRIMARY);
        waitForFxEvents();

        // Verify scrollable data was updated
        verify(scrollableData, atLeastOnce()).setMiddleTrace(anyInt());
    }

    @Test
    void testMouseDragOnRightBar() {
        // Setup mock behavior
        when(scrollableData.getTracesCount()).thenReturn(1000);
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(100);
        when(scrollableData.getMiddleTrace()).thenReturn(500);

        // Force initial layout
        profileScroll.recalc();
        waitForFxEvents();

        // Get right bar bounds
        Rectangle rightBar = profileScroll.getRightBar();
        Bounds boundsInScene = rightBar.localToScene(rightBar.getBoundsInLocal());

        Window window = stage.getScene().getWindow();
        double sceneX = window.getX() + boundsInScene.getMinX() + (boundsInScene.getWidth() / 2);
        double sceneY = window.getY() + boundsInScene.getMinY() + (boundsInScene.getHeight() / 2);
        Point2D rightPoint = new Point2D(sceneX, sceneY);

        // Move to right bar
        moveTo(rightPoint);

        // Press and hold
        press(MouseButton.PRIMARY);

        // Drag left by 30 pixels
        moveBy(-30, 0);

        // Drag right by 50 pixels
        moveBy(50, 0);

        // Release
        release(MouseButton.PRIMARY);

        waitForFxEvents();

        // Verify scrollable data was updated
        verify(scrollableData, atLeastOnce()).setMiddleTrace(anyInt());
    }

    @Test
    void testScrollPositionUpdate() {
        waitForFxEvents();

        // Mock scrollable data responses
        when(scrollableData.getTracesCount()).thenReturn(1000);
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(100);
        when(scrollableData.getMiddleTrace()).thenReturn(500);

        // Trigger recalc (this would normally happen after scroll)
        profileScroll.recalc();

        waitForFxEvents();

        // Verify center bar position is updated
        assertTrue(profileScroll.getCenterBar().getX() > 0, 
            "Center bar should be positioned based on middle trace");
        verify(scrollableData, atLeastOnce()).getMiddleTrace();
        verify(scrollableData, atLeastOnce()).getTracesCount();
    }

    @Test
    void testScrollBoundaries() {
        // Setup mock behavior with lenient stubs
        lenient().when(scrollableData.getTracesCount()).thenReturn(100);
        lenient().when(scrollableData.getVisibleNumberOfTrace()).thenReturn(50);
        lenient().when(scrollableData.getMiddleTrace()).thenReturn(25);

        // Force initial layout
        profileScroll.recalc();
        waitForFxEvents();

        // Get center bar bounds
        Rectangle centerBar = profileScroll.getCenterBar();
        Point2D centerPoint = new Point2D(
            profileScroll.localToScene(centerBar.getX() + centerBar.getWidth() / 2, centerBar.getHeight() / 2).getX(),
            profileScroll.localToScene(centerBar.getX() + centerBar.getWidth() / 2, centerBar.getHeight() / 2).getY()
        );

        // Move to center bar
        moveTo(centerPoint);
        // Press and hold
        press(MouseButton.PRIMARY);
        // Try to drag far left (beyond boundaries)
        moveBy(-1000, 0);
        // Release
        release(MouseButton.PRIMARY);

        waitForFxEvents();

        // Verify we can't scroll beyond the start
        verify(scrollableData, never()).setMiddleTrace(intThat(i -> i < 0));
        verify(scrollableData, atLeastOnce()).getTracesCount();
    }

    @Test
    void testFieldRecalculation() throws Exception {
        // Mock initial state
        when(scrollableData.getTracesCount()).thenReturn(1000);
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(100);
        when(scrollableData.getMiddleTrace()).thenReturn(500);

        // Force initial layout
        profileScroll.recalc();
        waitForFxEvents();

        // Get initial center bar position
        Rectangle initialCenterBar = profileScroll.getCenterBar();
        double initialWidth = initialCenterBar.getWidth();

        // Change visible trace count
        when(scrollableData.getVisibleNumberOfTrace()).thenReturn(200);

        // Trigger recalculation
        Future<Void> recalcFuture = asyncFx(() -> {
            profileScroll.recalc();
            return null;
        });
        recalcFuture.get();
        waitForFxEvents();

        // Get updated center bar
        Rectangle updatedCenterBar = profileScroll.getCenterBar();
        double updatedWidth = updatedCenterBar.getWidth();

        // Verify the center bar width has changed
        assertNotEquals(initialWidth, updatedWidth,
            "Center bar width should change when visible trace count changes");

        // Verify the scrollable data methods were called
        verify(scrollableData, atLeast(2)).getVisibleNumberOfTrace();
        verify(scrollableData, atLeast(2)).getTracesCount();
        verify(scrollableData, atLeast(2)).getMiddleTrace();
    }
}
