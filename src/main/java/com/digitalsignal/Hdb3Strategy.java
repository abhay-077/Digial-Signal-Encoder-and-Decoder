package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public final class Hdb3Strategy implements LineCodingStrategy {

    @Override
    public List<Double> encode(String bits) {
        validateBits(bits);
        List<Double> pulses = new ArrayList<>(bits.length());
        double last = -1.0;
        int count = 0;
        int i = 0;

        while (i < bits.length()) {
            if (i + 4 <= bits.length() && bits.startsWith("0000", i)) {
                if (count % 2 == 0) {
                    // B00V: B is normal AMI polarity; V repeats B.
                    double b = -last;
                    pulses.add(b);
                    pulses.add(0.0);
                    pulses.add(0.0);
                    pulses.add(b);
                    last = b;
                } else {
                    // 000V: V repeats the preceding nonzero pulse.
                    pulses.add(0.0);
                    pulses.add(0.0);
                    pulses.add(0.0);
                    pulses.add(last);
                }
                count = 0;
                i += 4;
            } else {
                double p = 0;
                if (bits.charAt(i++) == '1') {
                    p = -last;
                    last = p;
                    count++;
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
        int count = 0;
        int i = 0;

        while (i < p.length) {
            if (i + 4 <= p.length) {
                // 000V: three zeros followed by a same-polarity violation.
                if (p[i] == 0 && p[i + 1] == 0 && p[i + 2] == 0
                        && p[i + 3] != 0 && p[i + 3] == last) {
                    bits.append("0000");
                    last = p[i + 3];
                    count = 0;
                    i += 4;
                    continue;
                }

                // B00V: B and V have the same polarity.
                if (p[i] != 0 && p[i + 1] == 0 && p[i + 2] == 0
                        && p[i + 3] == p[i]) {
                    bits.append("0000");
                    last = p[i + 3];
                    count = 0;
                    i += 4;
                    continue;
                }
            }

            double pulse = p[i++];
            bits.append(pulse == 0 ? '0' : '1');
            if (pulse != 0) {
                last = pulse;
                count++;
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
            throw new IllegalArgumentException("Invalid HDB3 signal length.");

        double[] result = new double[signal.size() / 2];
        for (int i = 0; i < result.length; i++) {
            Double a = signal.get(i * 2), b = signal.get(i * 2 + 1);
            if (a == null || b == null || !Double.isFinite(a)
                    || !Double.isFinite(b) || a < -1 || a > 1
                    || a.doubleValue() != b.doubleValue())
                throw new IllegalArgumentException("Invalid HDB3 pulse.");
            result[i] = a;
        }
        return result;
    }

    private static void validateBits(String bits) {
        if (bits == null || bits.isEmpty() || !bits.matches("[01]+"))
            throw new IllegalArgumentException("Bit stream must contain only 0 and 1.");
    }
}
