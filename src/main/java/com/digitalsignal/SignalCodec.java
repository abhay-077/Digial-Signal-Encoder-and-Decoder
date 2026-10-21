package com.digitalsignal;

import java.util.List;

public final class SignalCodec {

    public static final List<String> SCHEMES = List.of(
            "NRZ-L", "NRZ-I", "Manchester", "Differential Manchester",
            "AMI", "B8ZS", "HDB3", "RZ"
    );

    private SignalCodec() {}

    public static List<Double> encode(String bits, String type) {
        validateBits(bits);
        return strategy(type).encode(bits);
    }

    public static String decode(List<Double> signal, String type) {
        validateSignal(signal);
        return strategy(type).decode(signal);
    }

    private static LineCodingStrategy strategy(String type) {
        if (type == null)
            throw new IllegalArgumentException("Encoding scheme cannot be null.");
        return LineCodingRegistry.find(type).orElseThrow(
                () -> new IllegalArgumentException("Unsupported encoding: " + type));
    }

    private static void validateBits(String bits) {
        if (bits == null || bits.isEmpty() || !bits.matches("[01]+"))
            throw new IllegalArgumentException("Bit stream must contain only 0 and 1.");
    }

    private static void validateSignal(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0)
            throw new IllegalArgumentException("Signal must contain two samples per bit.");

        for (Double value : signal) {
            if (value == null || !Double.isFinite(value)
                    || (value != -1.0 && value != 0.0 && value != 1.0))
                throw new IllegalArgumentException("Signal samples must be -1, 0, or +1.");
        }
    }
}
