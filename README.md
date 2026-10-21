# Digital Signal Encoder and Decoder

## Project Team

This project was developed by the following students:

| S. No. | Student Name | Enrollment Number |
|---|---|---|
| 1 | Abhay Verma | 2024BITE077 |
| 2 | Pawan Bhat | 2024BITE001 |
| 3 | Vinay Kumar | 2024BITE079 |

## Overview

This project is developed for **ITT 036: Programming Assignment 1 — Autumn 2026**. It implements digital signal encoding and decoding with a JavaFX graphical interface for signal visualization.

## Features

### 1. Digital Data Generation
- Generate random binary data.
- Generate binary sequences containing consecutive zeros.
- Find the longest palindromic substring in a binary sequence.

### 2. Line Coding Techniques
- **NRZ-L:** Non-Return-to-Zero-Level
- **NRZ-I:** Non-Return-to-Zero-Invert
- **Manchester**
- **Differential Manchester**
- **AMI:** Alternate Mark Inversion
- **B8ZS:** Bipolar with 8-Zero Substitution
- **HDB3:** High-Density Bipolar 3-Zero
- **RZ:** Return-to-Zero

### 3. Analog-to-Digital Conversion
- Pulse Code Modulation (PCM)
- Delta Modulation (DM)
- Encoding and decoding of analog samples.

### 4. Graphical Interface
- JavaFX-based graphical user interface.
- Visualization of encoded signal waveforms.
- Options to select encoding modes and schemes.
- Experimental waveform image decoding using image pixel analysis.

### 5. Automated Testing
- JUnit tests for digital signal encoding and decoding.
- Tests for PCM and Delta Modulation.
- Tests for palindrome detection and scrambling behavior.
- Input validation and edge-case testing.

## Technologies Used

- **Programming Language:** Java 21
- **GUI Framework:** JavaFX
- **Build Tool:** Apache Maven
- **Testing Framework:** JUnit 5
- **Design Pattern:** Strategy Pattern

## Project Structure

```text
Digial-Signal-Encoder-and-Decoder/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/digitalsignal/
│   │   │   ├── Main.java
│   │   │   ├── SignalCodec.java
│   │   │   ├── Palindrome.java
│   │   │   ├── WaveformImageDecoder.java
│   │   │   └── ...
│   │   └── resources/
│   └── test/
│       └── java/com/digitalsignal/
│           ├── SignalCodecTest.java
│           ├── AnalogCodecTest.java
│           ├── PalindromeTest.java
│           ├── ScramblerPatternTest.java
│           └── ...
├── .gitignore
├── pom.xml
└── README.md
```

## Requirements

- Java Development Kit (JDK) 21
- Apache Maven
- JavaFX-compatible graphical environment

## How to Run

Open a terminal in the directory containing `pom.xml`.

### Build the Project

```bash
mvn clean package
```

### Run Automated Tests

```bash
mvn test
```

### Launch the Application

```bash
mvn javafx:run
```

## Test Results

The latest reported automated test run completed successfully:

- **Total:** 20 tests
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

Tests cover digital encoding and decoding, analog conversion, palindrome detection, scrambling, and edge cases.

## Design Approach

The line-coding implementations use the **Strategy Pattern**. Individual strategy classes encapsulate the behavior of specific coding schemes, while a central registry selects the appropriate strategy.

## Limitations

- Image-based waveform decoding is experimental.
- Decoding accuracy depends on the waveform layout, image scaling, cropping, and the selected encoding scheme.
- Image-decoding results should be verified against the original input.

## Assignment Information

- **Course:** ITT 036
- **Assignment:** Programming Assignment 1
- **Semester:** Autumn 2026
- **Objective:** Implement a digital signal encoder and decoder.

## Acknowledgment

This project was developed for academic purposes. Any external resources and AI assistance used during development should be acknowledged according to the course requirements.
