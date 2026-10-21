package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class DifferentialManchesterStrategy
        implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        List<Double> signal = new ArrayList<>(bits.length() * 2);
        double previous = 1.0;

        for (char bit : bits.toCharArray()) {
            if (bit == '0') {
                previous = -previous;
            }

            double first = previous;
            double second = -first;

            signal.add(first);
            signal.add(second);

            previous = second;
        }

        return signal;
    }

    @Override
    public String decode(List<Double> signal) {
        validate(signal);
        StringBuilder bits = new StringBuilder(signal.size() / 2);
        double previousSecond = 1.0;

        for (int i = 0; i < signal.size(); i += 2) {
            double first = signal.get(i);
            double second = signal.get(i + 1);

            if (Math.abs(first) != 1.0
                    || Math.abs(second) != 1.0
                    || first == second) {
                throw new IllegalArgumentException(
                        "Invalid Differential Manchester signal at bit "
                                + (i / 2)
                );
            }

            bits.append(first == previousSecond ? '1' : '0');
            previousSecond = second;
        }

        return bits.toString();
    }

    private static void validate(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid signal length.");
        }
    }
}
