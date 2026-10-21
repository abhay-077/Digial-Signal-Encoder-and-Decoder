package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class RzStrategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        List<Double> signal = new ArrayList<>(bits.length() * 2);

        for (char bit : bits.toCharArray()) {
            signal.add(bit == '1' ? 1.0 : -1.0);
            signal.add(0.0);
        }

        return signal;
    }

    @Override
    public String decode(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid RZ signal length.");
        }

        StringBuilder bits = new StringBuilder(signal.size() / 2);

        for (int i = 0; i < signal.size(); i += 2) {
            double first = signal.get(i);
            double second = signal.get(i + 1);

            if (Math.abs(first) != 1.0 || second != 0.0) {
                throw new IllegalArgumentException(
                        "Invalid RZ pulse at bit " + (i / 2));
            }

            bits.append(first > 0 ? '1' : '0');
        }

        return bits.toString();
    }
}
