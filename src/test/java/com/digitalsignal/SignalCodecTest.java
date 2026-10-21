package com.digitalsignal;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SignalCodecTest {
    private static final String[] CASES = {
            "0", "1", "01", "10", "10110010", "00000000",
            "1111000011110000", "101010101010", "00010001000000001000"
    };

    @Test void roundTripsSupportedSchemes() {
        for (String scheme : SignalCodec.SCHEMES) {
            // Scrambling round-trip tests use runs of zeros and ordinary data.
            for (String bits : CASES) {
                List<Double> signal = SignalCodec.encode(bits, scheme);
                assertEquals(bits, SignalCodec.decode(signal, scheme),
                        () -> scheme + " failed for " + bits);
            }
        }
    }

    @Test void rejectsInvalidBits() {
        assertThrows(IllegalArgumentException.class, () -> SignalCodec.encode("", "NRZ-L"));
        assertThrows(IllegalArgumentException.class, () -> SignalCodec.encode("10201", "NRZ-L"));
        assertThrows(IllegalArgumentException.class, () -> SignalCodec.encode(null, "AMI"));
    }

    @Test void rejectsUnknownScheme() {
        assertThrows(IllegalArgumentException.class, () -> SignalCodec.encode("1010", "unknown"));
    }

    @Test void rejectsOddLengthSignal() {
        assertThrows(IllegalArgumentException.class, () -> SignalCodec.decode(List.of(1.0), "NRZ-L"));
    }

    @Test void outputHasTwoSamplesPerBit() {
        for(String scheme:SignalCodec.SCHEMES)
            assertEquals(16, SignalCodec.encode("10110010", scheme).size());
    }
}
