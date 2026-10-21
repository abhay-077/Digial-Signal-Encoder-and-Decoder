package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class B8zsStrategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        validateBits(bits);
        List<Double> pulses = new ArrayList<>(bits.length());
        double last = -1.0;
        int i = 0;

        while (i < bits.length()) {
            if (i + 8 <= bits.length()
                    && bits.startsWith("00000000", i)) {
                double v = last;
                double b = -last;
                double[] pattern = {0, 0, 0, v, -v, 0, -v, v};
                for (double p : pattern) pulses.add(p);
                last = v;
                i += 8;
            } else {
                double p = 0;
                if (bits.charAt(i++) == '1') {
                    p = -last;
                    last = p;
                }
                pulses.add(p);
            }
        }
        return expand(pulses);
    }

    @Override
    public String decode(List<Double> signal) {
        double[] p = collapse(signal);
        StringBuilder bits = new StringBuilder(p.length);
        double last = -1.0;
        int i = 0;

        while (i < p.length) {
            if (i + 8 <= p.length
                    && p[i] == 0 && p[i + 1] == 0 && p[i + 2] == 0
                    && p[i + 3] == last && p[i + 4] == -last
                    && p[i + 5] == 0 && p[i + 6] == -last
                    && p[i + 7] == last) {
                bits.append("00000000");
                last = p[i + 7];
                i += 8;
            } else {
                double pulse = p[i++];
                bits.append(pulse == 0 ? '0' : '1');
                if (pulse != 0) last = pulse;
            }
        }
        return bits.toString();
    }

    private static List<Double> expand(List<Double> pulses) {
        List<Double> result = new ArrayList<>(pulses.size() * 2);
        for (double p : pulses) {
            result.add(p);
            result.add(p);
        }
        return result;
    }

    private static double[] collapse(List<Double> signal) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0)
            throw new IllegalArgumentException("Invalid B8ZS signal length.");

        double[] result = new double[signal.size() / 2];
        for (int i = 0; i < result.length; i++) {
            Double a = signal.get(i * 2), b = signal.get(i * 2 + 1);
            if (a == null || b == null || !Double.isFinite(a)
                    || !Double.isFinite(b) || a < -1 || a > 1
                    || a.doubleValue() != b.doubleValue())
                throw new IllegalArgumentException("Invalid B8ZS pulse.");
            result[i] = a;
        }
        return result;
    }

    private static void validateBits(String bits) {
        if (bits == null || bits.isEmpty() || !bits.matches("[01]+"))
            throw new IllegalArgumentException("Bit stream must contain only 0 and 1.");
    }
}
