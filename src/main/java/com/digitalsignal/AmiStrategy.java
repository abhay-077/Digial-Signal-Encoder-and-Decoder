package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class AmiStrategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        List<Double> signal = new ArrayList<>(bits.length() * 2);
        double previous = -1.0;

        for (char bit : bits.toCharArray()) {
            double level = 0.0;

            if (bit == '1') {
                level = -previous;
                previous = level;
            }

            signal.add(level);
            signal.add(level);
        }

        return signal;
    }

    @Override
    public String decode(List<Double> signal) {
        validate(signal);
        StringBuilder bits = new StringBuilder(signal.size() / 2);
        double previous = 0.0;

        for (int i = 0; i < signal.size(); i += 2) {
            double a = signal.get(i);
            double b = signal.get(i + 1);

            if (a != b) {
                throw new IllegalArgumentException("Invalid AMI pulse.");
            }

            if (a != 0.0 && Math.abs(a) != 1.0) {
                throw new IllegalArgumentException("Invalid AMI level.");
            }

            if (a != 0.0 && a == previous) {
                throw new IllegalArgumentException(
                        "AMI polarity violation at bit " + (i / 2));
            }

            bits.append(a == 0.0 ? '0' : '1');

            if (a != 0.0) {
                previous = a;
            }
        }

        return bits.toString();
    }

    private static void validate(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid signal length.");
        }
    }
}
