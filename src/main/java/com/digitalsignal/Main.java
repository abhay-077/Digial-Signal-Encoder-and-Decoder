

package com.digitalsignal;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private final TextField input = new TextField("10110010");
    private final ComboBox<String> encoding = new ComboBox<>();
    private final Canvas canvas = new Canvas(1000, 360);
    private final Label status = new Label("Enter bits and click Encode.");
    private final Label decoded = new Label("Decoded bits: —");

    private String currentBits = "";
    private String currentEncoding = "";
    private List<Double> levels = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        encoding.getItems().addAll(
                "NRZ-L",
                "NRZ-I",
                "Manchester",
                "Differential Manchester",
                "AMI",
                "RZ"
        );
        encoding.setValue("Manchester");

        input.setPromptText("Enter binary data, e.g. 10110010");
        input.setPrefWidth(260);

        Button encodeButton = new Button("Encode");
        Button decodeButton = new Button("Decode");

        encodeButton.setOnAction(e -> encode());
        decodeButton.setOnAction(e -> decode());

        HBox controls = new HBox(10,
                new Label("Binary input:"), input,
                new Label("Encoding:"), encoding,
                encodeButton, decodeButton
        );
        controls.setPadding(new Insets(15));
        controls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label heading = new Label("Digital Signal Encoder & Decoder");
        heading.setFont(Font.font("System", 24));
        heading.setPadding(new Insets(15, 15, 0, 15));

        Label subtitle = new Label(
                "Encode binary data and visualize its digital signal."
        );
        subtitle.setPadding(new Insets(0, 15, 5, 15));

        VBox header = new VBox(5, heading, subtitle, controls);

        ScrollPane waveformScroll = new ScrollPane(canvas);
        waveformScroll.setFitToHeight(true);
        waveformScroll.setPrefViewportHeight(360);
        waveformScroll.setPannable(true);

        VBox bottom = new VBox(8, status, decoded);
        bottom.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(waveformScroll);
        root.setBottom(bottom);
        root.setStyle("-fx-background-color: #f5f7fb;");

        drawEmptyCanvas();

        Scene scene = new Scene(root, 1150, 570);
        stage.setTitle("Digital Signal Encoder & Decoder");
        stage.setMinWidth(800);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.show();
    }

    private void encode() {
        String bits = input.getText().trim();

        if (bits.isEmpty() || !bits.matches("[01]+")) {
            status.setText("Error: enter only 0s and 1s.");
            return;
        }

        currentBits = bits;
        currentEncoding = encoding.getValue();
        levels = generateSignal(bits, currentEncoding);

        canvas.setWidth(Math.max(1000, bits.length() * 90.0));
        drawWaveform();

        status.setText(
                "Encoded " + bits.length() + " bits using "
                        + currentEncoding + "."
        );
        decoded.setText("Decoded bits: —");
    }

    private List<Double> generateSignal(String bits, String type) {
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
                    if (bit == '1') {
                        current = -current;
                    }
                    signal.add(current);
                    signal.add(current);
                }

                case "Manchester" -> {
                    if (bit == '0') {
                        signal.add(1.0);
                        signal.add(-1.0);
                    } else {
                        signal.add(-1.0);
                        signal.add(1.0);
                    }
                }

                case "Differential Manchester" -> {
                    if (bit == '0') {
                        current = -current;
                    }
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
                    double level = bit == '1' ? 1 : -1;
                    signal.add(level);
                    signal.add(0.0);
                }
            }
        }

        return signal;
    }

    private void drawEmptyCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GRAY);
        gc.setFont(Font.font(16));
        gc.fillText("Your encoded waveform will appear here.", 30, 50);
    }

    private void drawWaveform() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double left = 55;
        double right = 20;
        double top = 35;
        double bottom = 35;

        double usableWidth = width - left - right;
        double usableHeight = height - top - bottom;
        double centerY = top + usableHeight / 2;
        double amplitude = usableHeight * 0.32;
        double bitWidth = usableWidth / currentBits.length();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        gc.setFont(Font.font(12));
        gc.setStroke(Color.rgb(225, 230, 238));
        gc.setLineWidth(1);

        for (int i = 0; i <= currentBits.length(); i++) {
            double x = left + i * bitWidth;
            gc.strokeLine(x, top, x, height - bottom);
        }

        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(left, centerY - amplitude, width - right,
                centerY - amplitude);
        gc.strokeLine(left, centerY, width - right, centerY);
        gc.strokeLine(left, centerY + amplitude, width - right,
                centerY + amplitude);

        gc.setFill(Color.DIMGRAY);
        gc.fillText("+1", 15, centerY - amplitude + 4);
        gc.fillText("0", 25, centerY + 4);
        gc.fillText("-1", 15, centerY + amplitude + 4);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(13));

        for (int i = 0; i < currentBits.length(); i++) {
            double x = left + i * bitWidth + bitWidth / 2;
            gc.fillText(String.valueOf(currentBits.charAt(i)),
                    x - 4, 22);
        }

        gc.setStroke(Color.web("#16a34a"));
        gc.setLineWidth(3);

        double previousY = centerY - levels.get(0) * amplitude;
        gc.beginPath();
        gc.moveTo(left, previousY);

        for (int i = 0; i < levels.size(); i++) {
            double x = left + i * bitWidth / 2;
            double nextX = left + (i + 1) * bitWidth / 2;
            double y = centerY - levels.get(i) * amplitude;

            if (i == 0) {
                gc.lineTo(x, y);
            } else {
                gc.lineTo(x, previousY);
                gc.lineTo(x, y);
            }

            gc.lineTo(nextX, y);
            previousY = y;
        }

        gc.stroke();

        gc.setFill(Color.web("#15803d"));
        gc.setFont(Font.font(14));
        gc.fillText(currentEncoding + " waveform", left, height - 8);
    }

    private void decode() {
        if (currentBits.isEmpty() || levels.isEmpty()) {
            status.setText("Encode a binary sequence first.");
            return;
        }

        StringBuilder result = new StringBuilder();
        double previous = 1;

        for (int i = 0; i < currentBits.length(); i++) {
            double first = levels.get(i * 2);
            double second = levels.get(i * 2 + 1);
            char bit;

            switch (currentEncoding) {
                case "NRZ-L" ->
                        bit = first > 0 ? '0' : '1';

                case "NRZ-I" -> {
                    bit = first != previous ? '1' : '0';
                    previous = first;
                }

                case "Manchester" ->
                        bit = first > second ? '0' : '1';

                case "Differential Manchester" -> {
                    bit = first != previous ? '0' : '1';
                    previous = second;
                }

                case "AMI" ->
                        bit = first == 0 ? '0' : '1';

                case "RZ" ->
                        bit = first == 0 ? '0' : '1';

                default -> bit = '?';
            }

            result.append(bit);
        }

        decoded.setText("Decoded bits: " + result);
        status.setText("Decoding completed using " + currentEncoding + ".");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
