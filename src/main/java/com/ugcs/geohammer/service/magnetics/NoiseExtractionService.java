package com.ugcs.geohammer.service.magnetics;

import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.service.gridding.GriddingResult;
import com.ugcs.geohammer.util.Check;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import org.springframework.stereotype.Service;

@Service
public class NoiseExtractionService {

    public GriddingResult extract(GriddingResult source, double primaryHeading, double lineSpacing) {
        Check.notNull(source);
        Check.condition(Double.isFinite(primaryHeading), "A primary-line heading is required");
        Check.condition(lineSpacing > 0 && Double.isFinite(lineSpacing), "A positive primary-line spacing is required");
        float[][] input = source.grid();
        Check.condition(input.length >= 2 && input[0].length >= 2, "The input grid is too small for directional filtering");
        double[] steps = cellSteps(source);
        float[][] noise = directionalHighPass(input, steps[0], steps[1], primaryHeading, 4 * lineSpacing);
        return new GriddingResult(source.seriesName() + "_NOISE", noise, source.minLatLon(), source.maxLatLon(), source.params());
    }

    private static float[][] directionalHighPass(float[][] input, double cellWidth, double cellHeight, double heading,
                                                 double cutoffWavelength) {
        int width = input.length;
        int height = input[0].length;
        float[][] complex = new float[width][2 * height];
        double mean = mean(input);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                complex[x][2 * y] = Float.isNaN(input[x][y]) ? (float) mean : (float) (input[x][y] - mean);
            }
        }
        FloatFFT_2D fft = new FloatFFT_2D(width, height);
        fft.complexForward(complex);
        double headingRadians = Math.toRadians(heading);
        double normalEast = Math.cos(headingRadians);
        double normalNorth = -Math.sin(headingRadians);
        double cutoff = 2 * Math.PI / cutoffWavelength;
        for (int x = 0; x < width; x++) {
            int xIndex = x <= width / 2 ? x : x - width;
            double kEast = xIndex * 2 * Math.PI / (width * cellWidth);
            for (int y = 0; y < height; y++) {
                int yIndex = y <= height / 2 ? y : y - height;
                double kNorth = yIndex * 2 * Math.PI / (height * cellHeight);
                double across = Math.abs(kEast * normalEast + kNorth * normalNorth);
                double magnitude = Math.hypot(kEast, kNorth);
                double directionalWeight = magnitude == 0 ? 0 : Math.pow(across / magnitude, 4);
                double highPass = across == 0 ? 0 : 1 / (1 + Math.pow(cutoff / across, 12));
                double gain = directionalWeight * highPass;
                complex[x][2 * y] *= gain;
                complex[x][2 * y + 1] *= gain;
            }
        }
        fft.complexInverse(complex, true);
        float[][] result = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = Float.isNaN(input[x][y]) ? Float.NaN : complex[x][2 * y];
            }
        }
        return result;
    }

    private static double[] cellSteps(GriddingResult result) {
        int width = result.grid().length;
        int height = result.grid()[0].length;
        LatLon min = result.minLatLon();
        LatLon max = result.maxLatLon();
        double cellWidth = new LatLon(min.getLatDgr(), min.getLonDgr()).getDistance(new LatLon(min.getLatDgr(), max.getLonDgr())) / width;
        double cellHeight = new LatLon(min.getLatDgr(), min.getLonDgr()).getDistance(new LatLon(max.getLatDgr(), min.getLonDgr())) / height;
        return new double[]{cellWidth, cellHeight};
    }

    private static double mean(float[][] values) {
        double sum = 0;
        int count = 0;
        for (float[] column : values) {
            for (float value : column) {
                if (!Float.isNaN(value)) {
                    sum += value;
                    count++;
                }
            }
        }
        return count == 0 ? 0 : sum / count;
    }
}
