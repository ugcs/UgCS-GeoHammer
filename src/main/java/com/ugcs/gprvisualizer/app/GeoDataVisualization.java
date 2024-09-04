package com.ugcs.gprvisualizer.app;

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class GeoDataVisualization extends Application {

    private static final double MIN_LAT = -90.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LON = -180.0;
    private static final double MAX_LON = 180.0;

    private static final int GRID_ROWS = 400;
    private static final int GRID_COLS = 400;

    private double[][] grid = new double[GRID_ROWS][GRID_COLS];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        initDataBase();
        //initializeRandomData();

        drawDenseHeatMap(gc, canvas.getWidth(), canvas.getHeight());

        primaryStage.setScene(new Scene(new StackPane(canvas)));
        primaryStage.show();
    }

    private void initDataBase() {
        // Инициализация данных (пример данных)
        Random random = new Random();
        var r1 = random.nextDouble(); // 0.1;
        var r2 = random.nextDouble(); // 0.1;

        for (int i = 0; i < GRID_ROWS; i++) {
            if (i % 100 == 0) {
                r1 = random.nextDouble(); // 0.1;
                System.out.println("i = " + i + " r1 = " + r1);
            }
            for (int j = 0; j < GRID_COLS; j++) {
                if (i % 100 == 0) {
                    r2 = random.nextDouble(); // 0.1;
                    System.out.println("j = " + i + " r2 = " + r2);
                }
                grid[i][j] = Math.sin(i * r1) * Math.cos(j * r2); // пример заполнения
            }
        }
    }

    private void initializeRandomData() {
        Random random = new Random();
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = -1 + 2 * random.nextDouble(); // случайное значение от -1 до 1
            }
        }
    }

    private void drawDenseHeatMap(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        PixelWriter pixelWriter = gc.getPixelWriter();

        // Размер каждой точки (чем больше размер, тем плотнее заполнение)
        double pointWidth = canvasWidth / GRID_COLS;
        double pointHeight = canvasHeight / GRID_ROWS;

        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                double lat = MIN_LAT + i * (MAX_LAT - MIN_LAT) / (GRID_ROWS - 1);
                double lon = MIN_LON + j * (MAX_LON - MIN_LON) / (GRID_COLS - 1);

                Point2D point = geoToScreen(lat, lon, canvasWidth, canvasHeight);
                double value = grid[i][j];
                Color color = getColorForValue(value);

                gc.setFill(color);
                gc.fillRect(point.getX(), point.getY(), pointWidth, pointHeight);
            }
        }
    }

    private Color getColorForValue(double value) {
        double normalizedValue = (value + 1) / 2; // нормализация значений от 0 до 1
        return Color.hsb(normalizedValue * 240, 1.0, 1.0); // От синего (0 градусов) к красному (240 градусов)
    }

    private Point2D geoToScreen(double lat, double lon, double canvasWidth, double canvasHeight) {
        double x = (lon - MIN_LON) / (MAX_LON - MIN_LON) * canvasWidth;
        double y = canvasHeight - (lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * canvasHeight;
        return new Point2D(x, y);
    }

    private static class Point2D {
        private final double x;
        private final double y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}