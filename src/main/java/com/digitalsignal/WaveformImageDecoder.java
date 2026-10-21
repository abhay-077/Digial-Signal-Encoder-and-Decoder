package com.digitalsignal;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Recovers two samples per bit from a screenshot of this application's
 * green, rectangular waveform. The caller must provide the encoding scheme.
 */
public final class WaveformImageDecoder {

    private WaveformImageDecoder() {}

    public static List<Double> recoverSamples(
            Image image, int bitCount) {

        if (image == null || image.isError()
                || image.getPixelReader() == null) {
            throw new IllegalArgumentException("Cannot read waveform image.");
        }
        if (bitCount < 1) {
            throw new IllegalArgumentException("Bit count must be positive.");
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        // Match the waveform layout in Main.drawWaveform().
        double left = 55.0;
        double right = 25.0;
        double top = 48.0;
        double bottom = 42.0;
        double centerY = top + (height - top - bottom) / 2.0;
        double amplitude = (height - top - bottom) * 0.32;

        if (width <= left + right || height <= top + bottom) {
            throw new IllegalArgumentException("Image is too small.");
        }

        double cell = (width - left - right) / bitCount;
        double halfCell = cell / 2.0;
        List<Double> samples = new ArrayList<>(bitCount * 2);

        for (int i = 0; i < bitCount * 2; i++) {
            double x = left + (i + 0.5) * halfCell;
            int px = Math.max(0, Math.min(width - 1, (int) Math.round(x)));

            // Search for the green waveform stroke, ignoring grid/text.
            int bestY = -1;
            double bestScore = 0;

            int yMin = Math.max(0, (int) (centerY - amplitude - 8));
            int yMax = Math.min(height - 1,
                    (int) (centerY + amplitude + 8));

            for (int y = yMin; y <= yMax; y++) {
                var color = reader.getColor(px, y);
                double score = color.getGreen()
                        - color.getRed() * 0.55
                        - color.getBlue() * 0.45;

                if (score > bestScore && color.getGreen() > 0.25) {
                    bestScore = score;
                    bestY = y;
                }
            }

            if (bestY < 0) {
                throw new IllegalArgumentException(
                        "Waveform not detected at sample " + i
                        + ". Use an unscaled screenshot of the waveform.");
            }

            double voltage = (centerY - bestY) / amplitude;

            if (Math.abs(voltage) < 0.25) {
                samples.add(0.0);
            } else {
                samples.add(voltage > 0 ? 1.0 : -1.0);
            }
        }

        return List.copyOf(samples);
    }

    public static String decode(Image image, int bitCount, String scheme) {
        List<Double> recovered = recoverSamples(image, bitCount);
        return SignalCodec.decode(recovered, scheme);
    }
}
