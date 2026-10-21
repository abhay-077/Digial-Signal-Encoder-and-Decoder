package com.digitalsignal;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {
    private final TextField input = new TextField("10110010000000001001");
    private final ComboBox<String> encoding = new ComboBox<>();
    private final Canvas canvas = new Canvas(1100, 420);
    private final ComboBox<String> mode = new ComboBox<>();
    private final TextField analogInput = new TextField("0, 0.8, 1.4, 0.9, 0, -0.8, -1.2, -0.5, 0.4");
    private final TextField pcmBits = new TextField("4");
    private final TextField dmStep = new TextField("0.3");
    private final TextField dmInitial = new TextField("0");
    private final Label analogOutput = new Label("Analog output: —");
    private List<Double> analogLevels = List.of();
    private String analogBits = "";
    private String analogMode = "";
    private final Label status = new Label("Enter a binary sequence and click Encode.");
    private final Label decoded = new Label("Decoded bits: —");
    private String currentBits = "";
    private String currentEncoding = "";
    private List<Double> levels = List.of();

    @Override public void start(Stage stage) {
        encoding.getItems().setAll(SignalCodec.SCHEMES);
        mode.getItems().setAll("Line coding", "PCM", "Delta Modulation");
        mode.setValue("Line coding");
        analogInput.setPrefColumnCount(30);
        pcmBits.setPrefColumnCount(3);
        dmStep.setPrefColumnCount(4);
        dmInitial.setPrefColumnCount(4);
        encoding.setValue("Manchester");
        input.setPromptText("Binary input, e.g. 10110010");
        input.setPrefColumnCount(24);
        Button encodeButton = new Button("Encode");
        Button decodeButton = new Button("Decode");
        Button randomButton = new Button("Generate random");
        Button zerosButton = new Button("Insert 8 zeros");
        Button analogButton = new Button("Process analog");
        analogButton.setOnAction(e -> processAnalog());
        encodeButton.setOnAction(e -> encode());
        decodeButton.setOnAction(e -> decode());
        randomButton.setOnAction(e -> { input.setText(randomBits(24)); encode(); });
        zerosButton.setOnAction(e -> { input.setText(randomBits(8) + "00000000" + randomBits(8)); encode(); });

        HBox controls = new HBox(10, new Label("Binary input:"), input,
                new Label("Mode:"), mode, new Label("Scheme:"), encoding, encodeButton, decodeButton, randomButton, zerosButton);
        controls.setPadding(new Insets(12, 16, 12, 16));
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setSpacing(9);

        Label heading = new Label("Digital Signal Encoder & Decoder");
        heading.setFont(Font.font("System", 25));
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #172554;");
        Label subtitle = new Label("Line coding, AMI scrambling and waveform-based decoding");
        subtitle.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        HBox analogControls = new HBox(8,
                new Label("Analog samples:"), analogInput,
                new Label("PCM bits/sample:"), pcmBits,
                new Label("DM step:"), dmStep,
                new Label("DM initial:"), dmInitial, analogButton);
        analogControls.setAlignment(Pos.CENTER_LEFT);
        analogControls.setPadding(new Insets(0, 16, 10, 16));
        analogControls.setSpacing(8);
        VBox header = new VBox(5, heading, subtitle, controls, analogControls);
        header.setPadding(new Insets(14, 14, 0, 14));

        canvas.setWidth(1100);
        canvas.setHeight(420);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff;");
        VBox footer = new VBox(6, status, decoded, analogOutput);
        footer.setPadding(new Insets(12, 16, 14, 16));
        status.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        decoded.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        BorderPane root = new BorderPane();
        root.setTop(header); root.setCenter(scroll); root.setBottom(footer);
        root.setStyle("-fx-background-color: #f1f5f9;");
        drawEmpty();
        stage.setTitle("Digital Signal Encoder & Decoder");
        stage.setMinWidth(900); stage.setMinHeight(540);
        stage.setScene(new Scene(root, 1280, 680));
        stage.show();
    }

    private static String randomBits(int n) {
        StringBuilder b = new StringBuilder(n);
        for(int i=0;i<n;i++) b.append(Math.random()<0.5?'0':'1');
        return b.toString();
    }
    private void encode() {
        String bits=input.getText()==null?"":input.getText().trim();
        try {
            levels=SignalCodec.encode(bits,encoding.getValue());
            currentBits=bits; currentEncoding=encoding.getValue();
            double cell=Math.max(28, Math.min(70, 1000.0/bits.length()));
            canvas.setWidth(Math.max(1100, 90+bits.length()*cell));
            drawWaveform();
            status.setText("Encoded "+bits.length()+" bits using "+currentEncoding+".");
            decoded.setText("Decoded bits: —");
        } catch (IllegalArgumentException ex) {
            status.setText("Input error: "+ex.getMessage());
        }
    }
    private void decode() {
        if(levels.isEmpty()){status.setText("Encode a sequence first.");return;}
        try {
            String result=SignalCodec.decode(levels,currentEncoding);
            decoded.setText("Decoded bits: "+result);
            status.setText(result.equals(currentBits)?"Decode successful: recovered signal matches input.":"Decode completed; compare recovered data with input.");
        } catch (IllegalArgumentException ex) { status.setText("Decode error: "+ex.getMessage()); }
    }

    private void processAnalog() {
        try {
            List<Double> samples = AnalogCodec.parseSamples(analogInput.getText());
            if ("PCM".equals(mode.getValue())) {
                int width = Integer.parseInt(pcmBits.getText().trim());
                AnalogCodec.PcmResult result = AnalogCodec.encodePcm(samples, width);
                analogMode = "PCM";
                analogBits = result.bits();
                analogLevels = result.samples();
                analogOutput.setText("PCM bits (" + width + " bits/sample): " + analogBits
                        + " | quantized: " + format(analogLevels));
                drawAnalog(analogLevels, "PCM quantized reconstruction");
                status.setText("PCM encoded " + samples.size() + " analog samples into " + analogBits.length() + " bits.");
            } else if ("Delta Modulation".equals(mode.getValue())) {
                double step = Double.parseDouble(dmStep.getText().trim());
                double initial = Double.parseDouble(dmInitial.getText().trim());
                AnalogCodec.DmResult result = AnalogCodec.encodeDelta(samples, step, initial);
                analogMode = "Delta Modulation";
                analogBits = result.bits();
                analogLevels = result.samples();
                analogOutput.setText("DM bits: " + analogBits + " | staircase: " + format(analogLevels));
                drawAnalog(analogLevels, "Delta modulation staircase");
                status.setText("Delta modulation encoded " + samples.size() + " samples.");
            } else {
                status.setText("Choose PCM or Delta Modulation in Mode.");
            }
        } catch (IllegalArgumentException ex) {
            status.setText("Analog input error: " + ex.getMessage());
        }
    }

    private void decodeAnalog() {
        if (analogBits.isEmpty()) { status.setText("Process analog input first."); return; }
        try {
            if ("PCM".equals(analogMode)) {
                List<Double> source = AnalogCodec.parseSamples(analogInput.getText());
                double min = source.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
                double max = source.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
                int width = Integer.parseInt(pcmBits.getText().trim());
                List<Double> decodedSamples = AnalogCodec.decodePcm(analogBits, width, min, max);
                analogLevels = decodedSamples;
                drawAnalog(decodedSamples, "PCM decoded staircase");
                analogOutput.setText("PCM decoded samples: " + format(decodedSamples));
                status.setText("PCM decoding complete.");
            } else if ("Delta Modulation".equals(analogMode)) {
                double step = Double.parseDouble(dmStep.getText().trim());
                double initial = Double.parseDouble(dmInitial.getText().trim());
                AnalogCodec.DmDecoded result = AnalogCodec.decodeDelta(analogBits, step, initial);
                analogLevels = result.staircase();
                drawAnalog(analogLevels, "Delta modulation decoded staircase");
                analogOutput.setText("DM decoded bits: " + result.bits() + " | staircase: " + format(analogLevels));
                status.setText("Delta modulation decoding complete.");
            }
        } catch (IllegalArgumentException ex) {
            status.setText("Analog decode error: " + ex.getMessage());
        }
    }

    private static String format(List<Double> values) {
        StringBuilder b = new StringBuilder();
        for (int i=0;i<values.size();i++) {
            if(i>0)b.append(", ");
            b.append(String.format(java.util.Locale.ROOT, "%.2f", values.get(i)));
        }
        return b.toString();
    }

    private void drawAnalog(List<Double> values, String title) {
        GraphicsContext g=canvas.getGraphicsContext2D();
        double w=Math.max(1100, 100+values.size()*70), h=420;
        canvas.setWidth(w); canvas.setHeight(h);
        double left=55,right=25,top=48,bottom=42;
        g.setFill(Color.WHITE);g.fillRect(0,0,w,h);
        double min=values.stream().mapToDouble(Double::doubleValue).min().orElse(-1);
        double max=values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if(max==min){max+=1;min-=1;}
        double pad=(max-min)*0.15; min-=pad; max+=pad;
        double plotH=h-top-bottom, plotW=w-left-right, cell=plotW/values.size();
        final double plotTop = top;
        final double plotMax = max;
        final double plotMin = min;
        final double plotHeight = plotH;

        java.util.function.DoubleUnaryOperator y =
                v -> plotTop + (plotMax - v) / (plotMax - plotMin) * plotHeight;
        g.setStroke(Color.web("#e2e8f0"));g.setLineWidth(1);
        for(int i=0;i<=values.size();i++){double x=left+i*cell;g.strokeLine(x,top,x,h-bottom);}
        g.setStroke(Color.web("#94a3b8"));g.strokeLine(left,y.applyAsDouble(0),w-right,y.applyAsDouble(0));
        g.setStroke(Color.web("#0369a1"));g.setLineWidth(3);
        for(int i=0;i<values.size();i++){
            double x=left+i*cell, x2=x+cell, yy=y.applyAsDouble(values.get(i));
            if(i==0)g.moveTo(x,yy);
            g.strokeLine(x,yy,x2,yy);
            if(i+1<values.size())g.strokeLine(x2,yy,x2,y.applyAsDouble(values.get(i+1)));
        }
        g.setFill(Color.web("#172554"));g.setFont(Font.font("System",17));g.fillText(title,left,27);
        g.setFill(Color.web("#334155"));g.setFont(Font.font("System",13));
        for(int i=0;i<values.size();i++)g.fillText(String.format(java.util.Locale.ROOT,"%.2f",values.get(i)),left+i*cell+3,h-15);
    }
    private void drawEmpty() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);g.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        g.setFill(Color.GRAY);g.setFont(Font.font("System",18));g.fillText("Your encoded waveform will appear here.",35,55);
    }
    private void drawWaveform() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        double w=canvas.getWidth(),h=canvas.getHeight(),left=55,right=25,top=48,bottom=42;
        double cell=(w-left-right)/currentBits.length(), half=cell/2;
        double cy=top+(h-top-bottom)/2, amp=(h-top-bottom)*0.32;
        g.setFill(Color.WHITE);g.fillRect(0,0,w,h);
        g.setFont(Font.font("System",14));
        g.setStroke(Color.web("#e2e8f0"));g.setLineWidth(1);
        for(int i=0;i<=currentBits.length();i++){double x=left+i*cell;g.strokeLine(x,top,x,h-bottom);}
        g.setStroke(Color.web("#cbd5e1"));
        g.strokeLine(left,cy-amp,w-right,cy-amp);g.strokeLine(left,cy,w-right,cy);g.strokeLine(left,cy+amp,w-right,cy+amp);
        g.setFill(Color.web("#334155"));g.fillText("+1",12,cy-amp+5);g.fillText("0",25,cy+5);g.fillText("-1",12,cy+amp+5);
        g.setFont(Font.font("System",15));
        for(int i=0;i<currentBits.length();i++)g.fillText(""+currentBits.charAt(i),left+i*cell+cell/2-4,27);
        g.setStroke(Color.web("#15803d"));g.setLineWidth(3);
        double prevY=cy-levels.get(0)*amp;
        g.beginPath();g.moveTo(left,prevY);
        for(int i=0;i<levels.size();i++){
            double x=left+i*half,nx=left+(i+1)*half,y=cy-levels.get(i)*amp;
            if(i>0){g.lineTo(x,prevY);g.lineTo(x,y);} else g.lineTo(x,y);
            g.lineTo(nx,y);prevY=y;
        }
        g.stroke();
        g.setFill(Color.web("#166534"));g.setFont(Font.font("System",16));g.fillText(currentEncoding+" waveform",left,h-12);
    }
    public static void main(String[] args){launch(args);}
}
