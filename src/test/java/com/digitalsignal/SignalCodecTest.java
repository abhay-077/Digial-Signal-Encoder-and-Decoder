
package com.digitalsignal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SignalCodecTest {

    private static final String[] CASES = {
        "0", "1", "01", "10",
        "10110010", "00000000",
        "1111000011110000",
        "101010101010",
        "00010001000000001000",
        "0000", "00000", "0000000",
        "000000000", "1000000001",
        "1100000011", "100010001000"
    };

    @Test
    void roundTripsSupportedSchemes() {
        for (String scheme : SignalCodec.SCHEMES) {
            for (String bits : CASES) {
                List<Double> signal = SignalCodec.encode(bits, scheme);

                assertEquals(bits, SignalCodec.decode(signal, scheme),
                    () -> scheme + " failed for " + bits);
            }
        }
    }

    @Test
    void randomizedRoundTrips() {
        Random random = new Random(42);

        for (String scheme : SignalCodec.SCHEMES) {
            for (int test = 0; test < 200; test++) {
                int length = 1 + random.nextInt(128);
                StringBuilder bits = new StringBuilder(length);

                for (int i = 0; i < length; i++) {
                    bits.append(random.nextBoolean() ? '1' : '0');
                }

                String input = bits.toString();
                List<Double> signal = SignalCodec.encode(input, scheme);

                assertEquals(input, SignalCodec.decode(signal, scheme),
                    () -> scheme + " failed for randomized input " + input);
            }
        }
    }

    @Test
    void rejectsInvalidBits() {
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.encode("", "NRZ-L"));
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.encode("10201", "NRZ-L"));
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.encode(null, "AMI"));
    }

    @Test
    void rejectsUnknownScheme() {
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.encode("1010", "unknown"));
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.decode(List.of(1.0, 1.0), "unknown"));
    }

    @Test
    void rejectsInvalidSignalLength() {
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.decode(List.of(1.0), "NRZ-L"));
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.decode(List.of(), "NRZ-L"));
    }

    @Test
    void rejectsNullAndInvalidSamples() {
        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.decode(null, "NRZ-L"));

        List<Double> invalid = new ArrayList<>();
        invalid.add(Double.NaN);
        invalid.add(1.0);

        assertThrows(IllegalArgumentException.class,
            () -> SignalCodec.decode(invalid, "NRZ-L"));
    }

    @Test
    void outputHasTwoSamplesPerBit() {
        for (String scheme : SignalCodec.SCHEMES) {
            assertEquals(16,
                SignalCodec.encode("10110010", scheme).size());
        }
    }
}

