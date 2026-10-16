
package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

public class SignalCodec {

    public static List<Double> encode(String bits, String type) {
        if (bits == null || bits.isEmpty() || !bits.matches("[01]+")) {
            throw new IllegalArgumentException(
                    "Input must contain only 0s and 1s."
            );
        }

        List<Double> signal = new ArrayList<>();
        double current = 1;
        double amiPolarity = 1;

        for (char bit : bits.toCharArray()) {
            switch (type) {
                case "NRZ-L" -> {
                    double level = bit == '0' ? 1 : -1;
                    signal.add(level);
                    signal.add(level);
                }
                case "NRZ-I" -> {
                    if (bit == '1') current = -current;
                    signal.add(current);
                    signal.add(current);
                }
                case "Manchester" -> {
                    signal.add(bit == '0' ? 1.0 : -1.0);
                    signal.add(bit == '0' ? -1.0 : 1.0);
                }
                case "Differential Manchester" -> {
                    if (bit == '0') current = -current;
                    signal.add(current);
                    current = -current;
                    signal.add(current);
                }
                case "AMI" -> {
                    double level = 0;
                    if (bit == '1') {
                        level = amiPolarity;
                        amiPolarity = -amiPolarity;
                    }
                    signal.add(level);
                    signal.add(level);
                }
                case "RZ" -> {
                    signal.add(bit == '1' ? 1.0 : -1.0);
                    signal.add(0.0);
                }
                default -> throw new IllegalArgumentException(
                        "Unsupported encoding: " + type
                );
            }
        }

        return signal;
    }
}
