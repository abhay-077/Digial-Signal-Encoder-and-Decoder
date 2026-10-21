package com.digitalsignal;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AnalogCodecTest {
    @Test void pcmProducesFixedWidthCodesAndDecodes() {
        List<Double> samples = List.of(-1.0, -0.25, 0.5, 1.0);
        var encoded = AnalogCodec.encodePcm(samples, 4);
        assertEquals(samples.size()*4, encoded.bits().length());
        var decoded = AnalogCodec.decodePcm(encoded.bits(), 4, encoded.min(), encoded.max());
        assertEquals(samples.size(), decoded.size());
        assertEquals(-1.0, decoded.get(0), 1e-9);
        assertEquals(1.0, decoded.get(3), 1e-9);
    }

    @Test void pcmConstantSignalIsSupported() {
        var encoded = AnalogCodec.encodePcm(List.of(2.0, 2.0), 3);
        assertEquals("000000", encoded.bits());
        assertEquals(List.of(2.0, 2.0), AnalogCodec.decodePcm(encoded.bits(), 3, 2, 2));
    }

    @Test void deltaModulationDecodesToStaircase() {
        var encoded = AnalogCodec.encodeDelta(List.of(0.1, 0.8, 0.2, -0.5), 0.25, 0);
        var decoded = AnalogCodec.decodeDelta(encoded.bits(), 0.25, 0);
        assertEquals(encoded.bits(), decoded.bits());
        assertEquals(encoded.samples(), decoded.staircase());
        assertEquals(4, decoded.staircase().size());
    }

    @Test void rejectsBadAnalogInputAndParameters() {
        assertThrows(IllegalArgumentException.class, () -> AnalogCodec.parseSamples("1,foo,3"));
        assertThrows(IllegalArgumentException.class, () -> AnalogCodec.encodePcm(List.of(1.0), 1));
        assertThrows(IllegalArgumentException.class, () -> AnalogCodec.encodeDelta(List.of(1.0), 0, 0));
    }
}
