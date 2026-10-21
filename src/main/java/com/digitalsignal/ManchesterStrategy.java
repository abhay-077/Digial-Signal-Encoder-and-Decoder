package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class ManchesterStrategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        List<Double> signal = new ArrayList<>(bits.length() * 2);

        for (char bit : bits.toCharArray()) {
            signal.add(bit == '0' ? 1.0 : -1.0);
            signal.add(bit == '0' ? -1.0 : 1.0);
        }

        return signal;
    }

    @Override
    public String decode(List<Double> signal) {
        validate(signal);
        StringBuilder bits = new StringBuilder(signal.size() / 2);

        for (int i = 0; i < signal.size(); i += 2) {
            double a = signal.get(i);
            double b = signal.get(i + 1);

            if (Math.abs(a) != 1.0 || Math.abs(b) != 1.0 || a == b) {
                throw new IllegalArgumentException(
                        "Invalid Manchester signal at bit " + (i / 2)
                );
            }

            bits.append(a > 0 ? '0' : '1');
        }

        return bits.toString();
    }

    private static void validate(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid signal length.");
        }
    }
}
