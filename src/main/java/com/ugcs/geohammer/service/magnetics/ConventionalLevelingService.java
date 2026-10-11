package com.ugcs.geohammer.service.magnetics;

import com.ugcs.geohammer.format.GeoData;
import com.ugcs.geohammer.model.LatLon;
import com.ugcs.geohammer.util.Check;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConventionalLevelingService {

    public ConventionalLevelingResult level(List<GeoData> data, ConventionalLevelingOptions options) {
        Check.notEmpty(data);
        Check.notNull(options);
        Map<Integer, List<Sample>> lines = collectLines(data, options.inputSeries());
        for (Integer lineId : options.tieLineIds()) {
            Check.condition(lines.containsKey(lineId), "Tie line " + lineId + " is not present in this survey");
        }
        List<ConventionalCrossover> crossovers = findCrossovers(lines, options.tieLineIds());
        Check.condition(!crossovers.isEmpty(), "No survey/tie-line crossovers were found");

        Map<Integer, CorrectionFunction> tieFunctions = new LinkedHashMap<>();
        for (Integer tieLine : options.tieLineIds()) {
            List<ConventionalCrossover> matches = new ArrayList<>();
            for (ConventionalCrossover crossover : crossovers) {
                if (crossover.tieLine() == tieLine) {
                    matches.add(crossover);
                }
            }
            if (!matches.isEmpty()) {
                tieFunctions.put(tieLine, createTieFunction(matches, options));
            }
        }

        Map<Integer, CorrectionFunction> surveyFunctions = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Sample>> entry : lines.entrySet()) {
            if (options.tieLineIds().contains(entry.getKey())) {
                continue;
            }
            List<Point> controls = new ArrayList<>();
            for (ConventionalCrossover crossover : crossovers) {
                if (crossover.surveyLine() == entry.getKey()) {
                    CorrectionFunction tieFunction = tieFunctions.get(crossover.tieLine());
                    if (tieFunction != null) {
                        controls.add(new Point(crossover.surveyDistance(),
                                crossover.tieValue() + tieFunction.valueAt(crossover.tieDistance())
                                        - crossover.surveyValue()));
                    }
                }
            }
            if (!controls.isEmpty()) {
                surveyFunctions.put(entry.getKey(), piecewiseLinear(controls));
            }
        }

        List<@Nullable Number> tieCorrections = emptyResult(data.size());
        List<@Nullable Number> surveyCorrections = emptyResult(data.size());
        List<@Nullable Number> values = emptyResult(data.size());
        for (Map.Entry<Integer, List<Sample>> entry : lines.entrySet()) {
            boolean isTieLine = options.tieLineIds().contains(entry.getKey());
            CorrectionFunction correction = isTieLine ? tieFunctions.get(entry.getKey()) : surveyFunctions.get(entry.getKey());
            for (Sample sample : entry.getValue()) {
                double value = correction == null ? 0 : correction.valueAt(sample.distance());
                if (isTieLine) {
                    tieCorrections.set(sample.row(), value);
                } else {
                    surveyCorrections.set(sample.row(), value);
                }
                values.set(sample.row(), sample.field() + value);
            }
        }
        return new ConventionalLevelingResult(values, tieCorrections, surveyCorrections, crossovers,
                statistics(crossovers, null, null), statistics(crossovers, tieFunctions, surveyFunctions));
    }

    private static Map<Integer, List<Sample>> collectLines(List<GeoData> data, String series) {
        Map<Integer, List<Sample>> lines = new LinkedHashMap<>();
        for (int row = 0; row < data.size(); row++) {
            GeoData value = data.get(row);
            LatLon location = value.getLatLon();
            Number field = value.getNumber(series);
            if (location == null || field == null || !Double.isFinite(field.doubleValue())) {
                continue;
            }
            List<Sample> line = lines.computeIfAbsent(value.getLineOrDefault(0), ignored -> new ArrayList<>());
            double distance = line.isEmpty() ? 0 : line.getLast().distance() + line.getLast().location().getDistance(location);
            line.add(new Sample(row, location, field.doubleValue(), distance));
        }
        lines.values().removeIf(line -> line.size() < 2);
        Check.condition(!lines.isEmpty(), "Survey has no lines with coordinates and magnetic values");
        return lines;
    }

    private static List<ConventionalCrossover> findCrossovers(Map<Integer, List<Sample>> lines, Set<Integer> ties) {
        List<ConventionalCrossover> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Sample>> survey : lines.entrySet()) {
            if (ties.contains(survey.getKey())) {
                continue;
            }
            for (Integer tieId : ties) {
                List<Sample> tie = lines.get(tieId);
                for (int i = 1; i < survey.getValue().size(); i++) {
                    for (int j = 1; j < tie.size(); j++) {
                        ConventionalCrossover crossover = intersect(survey.getKey(), tieId, survey.getValue().get(i - 1),
                                survey.getValue().get(i), tie.get(j - 1), tie.get(j));
                        if (crossover != null) {
                            result.add(crossover);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static @Nullable ConventionalCrossover intersect(int surveyLine, int tieLine, Sample a, Sample b, Sample c, Sample d) {
        double denominator = (b.location().getLonDgr() - a.location().getLonDgr()) * (d.location().getLatDgr() - c.location().getLatDgr())
                - (b.location().getLatDgr() - a.location().getLatDgr()) * (d.location().getLonDgr() - c.location().getLonDgr());
        if (Math.abs(denominator) < 1e-12) {
            return null;
        }
        double t = ((c.location().getLonDgr() - a.location().getLonDgr()) * (d.location().getLatDgr() - c.location().getLatDgr())
                - (c.location().getLatDgr() - a.location().getLatDgr()) * (d.location().getLonDgr() - c.location().getLonDgr())) / denominator;
        double u = ((c.location().getLonDgr() - a.location().getLonDgr()) * (b.location().getLatDgr() - a.location().getLatDgr())
                - (c.location().getLatDgr() - a.location().getLatDgr()) * (b.location().getLonDgr() - a.location().getLonDgr())) / denominator;
        if (t < 0 || t > 1 || u < 0 || u > 1) {
            return null;
        }
        return new ConventionalCrossover(surveyLine, tieLine,
                a.location().getLatDgr() + t * (b.location().getLatDgr() - a.location().getLatDgr()),
                a.location().getLonDgr() + t * (b.location().getLonDgr() - a.location().getLonDgr()),
                a.distance() + t * (b.distance() - a.distance()), c.distance() + u * (d.distance() - c.distance()),
                a.field() + t * (b.field() - a.field()), c.field() + u * (d.field() - c.field()));
    }

    private static CorrectionFunction createTieFunction(List<ConventionalCrossover> crossovers, ConventionalLevelingOptions options) {
        List<Point> controls = new ArrayList<>();
        for (ConventionalCrossover crossover : crossovers) {
            controls.add(new Point(crossover.tieDistance(), crossover.misTie()));
        }
        return switch (options.correctionModel()) {
            case MEDIAN -> constant(median(controls));
            case LEAST_SQUARES -> polynomial(controls, 1);
            case POLYNOMIAL -> polynomial(controls, options.polynomialOrder());
            case SPLINE -> cubicSpline(controls);
        };
    }

    private static CorrectionFunction constant(double value) {
        return ignored -> value;
    }

    private static CorrectionFunction polynomial(List<Point> controls, int requestedOrder) {
        int order = Math.min(requestedOrder, controls.size() - 1);
        if (order <= 0) {
            return constant(mean(controls));
        }
        double center = 0;
        double scale = 0;
        for (Point point : controls) {
            center += point.x();
        }
        center /= controls.size();
        for (Point point : controls) {
            scale = Math.max(scale, Math.abs(point.x() - center));
        }
        if (scale == 0) {
            return constant(mean(controls));
        }
        double[][] matrix = new double[order + 1][order + 2];
        for (Point point : controls) {
            double x = (point.x() - center) / scale;
            double[] powers = new double[order * 2 + 1];
            powers[0] = 1;
            for (int i = 1; i < powers.length; i++) {
                powers[i] = powers[i - 1] * x;
            }
            for (int row = 0; row <= order; row++) {
                for (int column = 0; column <= order; column++) {
                    matrix[row][column] += powers[row + column];
                }
                matrix[row][order + 1] += point.y() * powers[row];
            }
        }
        double[] coefficients = solve(matrix);
        if (coefficients == null) {
            return constant(mean(controls));
        }
        double normalizedCenter = center;
        double normalizedScale = scale;
        return x -> {
            double normalized = (x - normalizedCenter) / normalizedScale;
            double value = 0;
            for (int i = coefficients.length - 1; i >= 0; i--) {
                value = value * normalized + coefficients[i];
            }
            return value;
        };
    }

    private static CorrectionFunction cubicSpline(List<Point> controls) {
        List<Point> points = mergePoints(controls);
        if (points.size() < 2) {
            return constant(points.isEmpty() ? 0 : points.getFirst().y());
        }
        double[] slopes = new double[points.size()];
        slopes[0] = slope(points.get(0), points.get(1));
        slopes[slopes.length - 1] = slope(points.get(points.size() - 2), points.getLast());
        for (int i = 1; i < slopes.length - 1; i++) {
            slopes[i] = slope(points.get(i - 1), points.get(i + 1));
        }
        return x -> splineValue(points, slopes, x);
    }

    private static CorrectionFunction piecewiseLinear(List<Point> controls) {
        List<Point> points = mergePoints(controls);
        if (points.size() == 1) {
            return constant(points.getFirst().y());
        }
        return x -> linearValue(points, x);
    }

    private static List<Point> mergePoints(List<Point> values) {
        List<Point> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.comparingDouble(Point::x));
        List<Point> result = new ArrayList<>();
        int index = 0;
        while (index < sorted.size()) {
            Point point = sorted.get(index);
            double sum = 0;
            int count = 0;
            while (index < sorted.size() && Math.abs(sorted.get(index).x() - point.x()) < 1e-6) {
                sum += sorted.get(index++).y();
                count++;
            }
            result.add(new Point(point.x(), sum / count));
        }
        return result;
    }

    private static double splineValue(List<Point> points, double[] slopes, double x) {
        if (x <= points.getFirst().x()) return points.getFirst().y();
        if (x >= points.getLast().x()) return points.getLast().y();
        int index = segmentIndex(points, x);
        Point left = points.get(index);
        Point right = points.get(index + 1);
        double width = right.x() - left.x();
        double t = (x - left.x()) / width;
        double t2 = t * t;
        double t3 = t2 * t;
        return (2 * t3 - 3 * t2 + 1) * left.y() + (t3 - 2 * t2 + t) * width * slopes[index]
                + (-2 * t3 + 3 * t2) * right.y() + (t3 - t2) * width * slopes[index + 1];
    }

    private static double linearValue(List<Point> points, double x) {
        if (x <= points.getFirst().x()) return points.getFirst().y();
        if (x >= points.getLast().x()) return points.getLast().y();
        Point left = points.get(segmentIndex(points, x));
        Point right = points.get(segmentIndex(points, x) + 1);
        return left.y() + (x - left.x()) * (right.y() - left.y()) / (right.x() - left.x());
    }

    private static int segmentIndex(List<Point> points, double x) {
        for (int i = 0; i < points.size() - 1; i++) {
            if (x <= points.get(i + 1).x()) return i;
        }
        return points.size() - 2;
    }

    private static double slope(Point a, Point b) {
        return (b.y() - a.y()) / (b.x() - a.x());
    }

    private static @Nullable double[] solve(double[][] matrix) {
        int size = matrix.length;
        for (int pivot = 0; pivot < size; pivot++) {
            int best = pivot;
            for (int row = pivot + 1; row < size; row++) {
                if (Math.abs(matrix[row][pivot]) > Math.abs(matrix[best][pivot])) best = row;
            }
            if (Math.abs(matrix[best][pivot]) < 1e-12) return null;
            double[] swap = matrix[pivot]; matrix[pivot] = matrix[best]; matrix[best] = swap;
            double divisor = matrix[pivot][pivot];
            for (int column = pivot; column <= size; column++) matrix[pivot][column] /= divisor;
            for (int row = 0; row < size; row++) {
                if (row == pivot) continue;
                double factor = matrix[row][pivot];
                for (int column = pivot; column <= size; column++) matrix[row][column] -= factor * matrix[pivot][column];
            }
        }
        double[] solution = new double[size];
        for (int row = 0; row < size; row++) solution[row] = matrix[row][size];
        return solution;
    }

    private static ConventionalLevelingStatistics statistics(List<ConventionalCrossover> crossovers,
                                                              @Nullable Map<Integer, CorrectionFunction> tieFunctions,
                                                              @Nullable Map<Integer, CorrectionFunction> surveyFunctions) {
        if (crossovers.isEmpty()) return ConventionalLevelingStatistics.empty();
        List<Double> values = new ArrayList<>();
        for (ConventionalCrossover crossover : crossovers) {
            double error = crossover.misTie();
            if (tieFunctions != null && surveyFunctions != null) {
                CorrectionFunction tie = tieFunctions.get(crossover.tieLine());
                CorrectionFunction survey = surveyFunctions.get(crossover.surveyLine());
                error += survey == null ? 0 : survey.valueAt(crossover.surveyDistance());
                error -= tie == null ? 0 : tie.valueAt(crossover.tieDistance());
            }
            values.add(error);
        }
        double sum = 0;
        double sumSquares = 0;
        double max = 0;
        for (double value : values) { sum += value; sumSquares += value * value; max = Math.max(max, Math.abs(value)); }
        double mean = sum / values.size();
        double deviation = 0;
        for (double value : values) deviation += (value - mean) * (value - mean);
        values.sort(Double::compare);
        return new ConventionalLevelingStatistics(values.size(), mean, medianValues(values), Math.sqrt(deviation / values.size()),
                Math.sqrt(sumSquares / values.size()), max);
    }

    private static double mean(List<Point> points) {
        double sum = 0;
        for (Point point : points) sum += point.y();
        return sum / points.size();
    }

    private static double median(List<Point> points) {
        List<Double> values = new ArrayList<>();
        for (Point point : points) values.add(point.y());
        values.sort(Double::compare);
        return medianValues(values);
    }

    private static double medianValues(List<Double> values) {
        int middle = values.size() / 2;
        return values.size() % 2 == 0 ? (values.get(middle - 1) + values.get(middle)) / 2 : values.get(middle);
    }

    private static List<@Nullable Number> emptyResult(int size) {
        List<@Nullable Number> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) values.add(null);
        return values;
    }

    private interface CorrectionFunction {
        double valueAt(double distance);
    }

    private record Sample(int row, LatLon location, double field, double distance) { }

    private record Point(double x, double y) { }
}
