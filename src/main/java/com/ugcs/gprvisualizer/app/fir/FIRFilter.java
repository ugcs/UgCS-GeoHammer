package com.ugcs.gprvisualizer.app.fir;

import java.util.ArrayList;
import java.util.List;

/**
 * The FIRFilter class represents a Finite Impulse Response (FIR) filter.
 *
 * The filter is designed based on the specified filter order, cutoff frequency, and sample rate.
 * It can be used to filter a single value or a list of values.
 *
 * The filter coefficients are computed using the createFIRCoefficients method, which uses a Hamming window function.
 * The filter implementation uses a circular buffer to store the input values and the coefficients are multiplied with the values in the buffer to get the filtered result.
 * The buffer is updated with the new input value and the oldest value is overwritten when the buffer is full.
 *
 * The FIRFilter class also provides a method to calculate the sampling rate based on a list of timestamps.
 * It uses the average interval between the timestamps to compute the sampling rate.
 *
 */
public class FIRFilter {
    
    private double[] coefficients;
    private double[] buffer;
    private int bufferIndex;

    /**
     * Constructs a new FIR filter with the specified filter order, cutoff frequency, and sample rate.
     *
     * @param filterOrder the filter order
     * @param cutoffFrequency the cutoff frequency in Hertz
     * @param sampleRate the sample rate in Hertz
     */
    public FIRFilter(int filterOrder, double cutoffFrequency, double sampleRate) {
        this.coefficients = createFIRCoefficients(filterOrder, cutoffFrequency, sampleRate);
        this.buffer = new double[filterOrder];
        this.bufferIndex = 0;
    }

    private double[] createFIRCoefficients(int filterOrder, double cutOffFrequency, double sampleRate) {
        double[] coefficients = new double[filterOrder];
        double[] window = createHammingWindow(filterOrder);

        // Normalized cut-off frequency
        double normalizedCutoff = 2 * cutOffFrequency / sampleRate;
        int m = filterOrder / 2;

        for (int i = 0; i < filterOrder; i++) {
            int n = i - m;
            if (n == 0) {
                coefficients[i] = normalizedCutoff;
            } else {
                coefficients[i] = normalizedCutoff * (Math.sin(Math.PI * normalizedCutoff * n) / (Math.PI * normalizedCutoff * n));
            }
            // Apply window function
            coefficients[i] *= window[i];
        }

        return coefficients;
    }

    private double[] createHammingWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (size - 1));
        }
        return window;
    }

    public List<Number> filterList(List<Number> data) {
        List<Number> result = new ArrayList<>();
        for (Number value : data) {
            if (value != null) {
                result.add(filter(value.doubleValue()));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    private double filter(double inputValue) {
        buffer[bufferIndex] = inputValue;
        double result = 0.0;
        int coefIndex = 0;
        for (int i = bufferIndex; i >= 0; i--) {
            result += coefficients[coefIndex] * buffer[i];
            coefIndex++;
        }
        for (int i = buffer.length - 1; i > bufferIndex; i--) {
            result += coefficients[coefIndex] * buffer[i];
            coefIndex++;
        }
        bufferIndex++;
        if (bufferIndex == buffer.length) {
            bufferIndex = 0;
        }
        return result;
    }

    public static double calculateSamplingRate(List<Long> timestamps) {        
        double totalInterval = 0;
        for (int i = 1; i < timestamps.size(); i++) {
            totalInterval += (timestamps.get(i) - timestamps.get(i - 1));
        }
        double averageInterval = totalInterval / (timestamps.size() - 1);
        return 1000.0 / averageInterval; // Conversion from milliseconds to Hertz
    }
}