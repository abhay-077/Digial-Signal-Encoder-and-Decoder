package com.digitalsignal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScramblerPatternTest {

    private List<Double> pulses(String bits, LineCodingStrategy strategy) {
        List<Double> signal = strategy.encode(bits);
        List<Double> result = new ArrayList<>();

        for (int i = 0; i < signal.size(); i += 2) {
            assertEquals(signal.get(i), signal.get(i + 1),
                    "Each bit must have two equal samples");
            result.add(signal.get(i));
        }

        return result;
    }

    @Test
    void b8zsSubstitutionAfterNegativePulse() {
        List<Double> actual = pulses("100000000", new B8zsStrategy());

        assertEquals(
                List.of(1.0, 0.0, 0.0, 0.0, 1.0, -1.0, 0.0, -1.0, 1.0),
                actual
        );
    }

    @Test
    void b8zsSubstitutionAfterPositivePulse() {
        List<Double> actual = pulses("1100000000", new B8zsStrategy());

        assertEquals(
                List.of(1.0, -1.0, 0.0, 0.0, 0.0, -1.0, 1.0, 0.0, 1.0, -1.0),
                actual
        );
    }

    @Test
    void hdb3EncodesFourZerosAndRoundTrips() {
        for (String bits : List.of(
                "0000", "10000", "110000", "100000000",
                "1010000100000100000"
        )) {
            Hdb3Strategy strategy = new Hdb3Strategy();

            assertEquals(bits, strategy.decode(strategy.encode(bits)),
                    "HDB3 round-trip failed for " + bits);
        }
    }

    @Test
    void b8zsRoundTripsZeroRunsWithBothPolarities() {
        B8zsStrategy strategy = new B8zsStrategy();

        for (String bits : List.of(
                "100000000", "1100000000",
                "1000000001", "11000000001"
        )) {
            assertEquals(bits, strategy.decode(strategy.encode(bits)),
                    "B8ZS round-trip failed for " + bits);
        }
    }
}
