package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class NrzIStrategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        List<Double> signal = new ArrayList<>(bits.length() * 2);
        if (bits.isEmpty()) return signal;

        double level = bits.charAt(0) == '1' ? 1.0 : -1.0;

        for (int i = 0; i < bits.length(); i++) {
            if (i > 0 && bits.charAt(i) == '1') {
                level = -level;
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
        double previous = 0;

        for (int i = 0; i < signal.size(); i += 2) {
            double a = signal.get(i);
            double b = signal.get(i + 1);

            if (a != b || Math.abs(a) != 1.0) {
                throw new IllegalArgumentException("Invalid NRZ-I signal.");
            }

            if (i == 0) {
                bits.append(a > 0 ? '1' : '0');
            } else {
                bits.append(a != previous ? '1' : '0');
            }

            previous = a;
        }

        return bits.toString();
    }

    private static void validate(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid signal length.");
        }
    }
}
