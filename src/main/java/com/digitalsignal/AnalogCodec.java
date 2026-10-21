package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

/** PCM and delta-modulation utilities for sampled analog signals. */
public final class AnalogCodec {
    private AnalogCodec() {}

    public record PcmResult(List<Double> samples, String bits, int bitsPerSample,
                            double min, double max) {}
    public record DmResult(List<Double> samples, String bits, double stepSize,
                           double initialLevel) {}
    public record DmDecoded(List<Double> staircase, String bits) {}

    /**
     * Uniform PCM: quantizes each input sample into 2^bitsPerSample levels and
     * emits a fixed-width unsigned binary code. The sample list is copied.
     */
    public static PcmResult encodePcm(List<Double> samples, int bitsPerSample) {
        validateSamples(samples);
        if (bitsPerSample < 2 || bitsPerSample > 16)
            throw new IllegalArgumentException("PCM bits per sample must be between 2 and 16.");
        double min = samples.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double max = samples.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        int levels = 1 << bitsPerSample;
        StringBuilder bits = new StringBuilder(samples.size() * bitsPerSample);
        List<Double> quantized = new ArrayList<>(samples.size());
        for (double sample : samples) {
            int code;
            if (max == min) code = 0;
            else code = (int)Math.round((sample - min) / (max - min) * (levels - 1));
            code = Math.max(0, Math.min(levels - 1, code));
            appendFixedWidth(bits, code, bitsPerSample);
            double reconstructed = max == min ? min : min + (max - min) * code / (levels - 1);
            quantized.add(reconstructed);
        }
        return new PcmResult(List.copyOf(quantized), bits.toString(), bitsPerSample, min, max);
    }

    /** Decode fixed-width PCM code words using the quantizer range from encoding. */
    public static List<Double> decodePcm(String bits, int bitsPerSample, double min, double max) {
        validateBits(bits);
        if (bitsPerSample < 2 || bitsPerSample > 16 || bits.length() % bitsPerSample != 0)
            throw new IllegalArgumentException("PCM bit stream length must be divisible by bits per sample.");
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max)
            throw new IllegalArgumentException("Invalid PCM quantization range.");
        int levels = 1 << bitsPerSample;
        List<Double> result = new ArrayList<>(bits.length()/bitsPerSample);
        for (int i=0;i<bits.length();i+=bitsPerSample) {
            int code = Integer.parseInt(bits.substring(i,i+bitsPerSample),2);
            result.add(max == min ? min : min + (max-min)*code/(levels-1));
        }
        return List.copyOf(result);
    }

    /**
     * First-order delta modulation. The first bit represents the first sample's
     * direction relative to initialLevel; each following bit compares the
     * current input sample with the previous reconstructed level.
     */
    public static DmResult encodeDelta(List<Double> samples, double stepSize, double initialLevel) {
        validateSamples(samples);
        if (!Double.isFinite(stepSize) || stepSize <= 0)
            throw new IllegalArgumentException("Delta step size must be positive and finite.");
        if (!Double.isFinite(initialLevel))
            throw new IllegalArgumentException("Initial level must be finite.");
        StringBuilder bits = new StringBuilder(samples.size());
        List<Double> reconstructed = new ArrayList<>(samples.size());
        double level = initialLevel;
        for (double sample : samples) {
            if (sample >= level) {
                bits.append('1');
                level += stepSize;
            } else {
                bits.append('0');
                level -= stepSize;
            }
            reconstructed.add(level);
        }
        return new DmResult(List.copyOf(reconstructed), bits.toString(), stepSize, initialLevel);
    }

    /** Decode a DM bitstream into its staircase approximation. */
    public static DmDecoded decodeDelta(String bits, double stepSize, double initialLevel) {
        validateBits(bits);
        if (!Double.isFinite(stepSize) || stepSize <= 0 || !Double.isFinite(initialLevel))
            throw new IllegalArgumentException("Step size must be positive and initial level finite.");
        List<Double> staircase = new ArrayList<>(bits.length());
        double level = initialLevel;
        for (char bit : bits.toCharArray()) {
            level += bit == '1' ? stepSize : -stepSize;
            staircase.add(level);
        }
        return new DmDecoded(List.copyOf(staircase), bits);
    }

    public static List<Double> parseSamples(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Enter comma-separated analog samples.");
        String[] parts = text.trim().split("[,\\s]+");
        List<Double> values = new ArrayList<>(parts.length);
        try {
            for (String part : parts) {
                double value = Double.parseDouble(part);
                if (!Double.isFinite(value)) throw new NumberFormatException();
                values.add(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Samples must be finite numbers separated by commas or spaces.");
        }
        validateSamples(values);
        return List.copyOf(values);
    }

    private static void appendFixedWidth(StringBuilder b, int value, int width) {
        for (int bit=width-1;bit>=0;bit--) b.append(((value >>> bit)&1)==1?'1':'0');
    }
    private static void validateBits(String bits) {
        if (bits==null || bits.isEmpty() || !bits.matches("[01]+"))
            throw new IllegalArgumentException("Bit stream must contain only 0s and 1s.");
    }
    private static void validateSamples(List<Double> samples) {
        if (samples==null || samples.isEmpty()) throw new IllegalArgumentException("Provide at least one analog sample.");
        for (Double v:samples)
            if(v==null || !Double.isFinite(v)) throw new IllegalArgumentException("Analog samples must be finite numbers.");
    }
}
