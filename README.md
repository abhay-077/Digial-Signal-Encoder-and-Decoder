
# Digital Signal Encoder and Decoder

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

### 3. Analog-to-Digital Conversion
- Pulse Code Modulation (PCM)
- Delta Modulation (DM)
- Encoding and decoding of analog samples.

### 4. Graphical Interface
- JavaFX-based graphical user interface.
- Visualization of encoded signal waveforms.
- Options to select encoding modes and schemes.

### 5. Automated Testing
- JUnit tests for digital signal encoding and decoding.
- Tests for PCM and Delta Modulation.

## Technologies Used

- **Programming Language:** Java 21
- **GUI Framework:** JavaFX
- **Build Tool:** Apache Maven
- **Testing Framework:** JUnit

## Project Structure

```text
Digial-Signal-Encoder-and-Decoder/
├── .idea/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/digitalsignal/
│   │   │   ├── Main.java
│   │   │   ├── SignalCodec.java
│   │   │   └── AnalogCodec.java
│   │   └── resources/
│   └── test/
│       └── java/com/digitalsignal/
│           ├── SignalCodecTest.java
│           └── AnalogCodecTest.java
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

### Run Automated Tests

```bash
mvn clean test
```

### Launch the Application

```bash
mvn javafx:run
```

## Test Results

The project has passed the following automated tests in the development environment:

- AnalogCodecTest: 4 tests passed.
- SignalCodecTest: 5 tests passed.
- **Total: 9 tests passed, 0 failures, 0 errors.**

## Assignment Information

- **Course:** ITT 036
- **Assignment:** Programming Assignment 1
- **Semester:** Autumn 2026
- **Objective:** Implement a digital signal encoder and decoder.

## Acknowledgment

This project was developed for academic purposes. Any external resources and AI assistance used during development should be acknowledged according to the course requirements.
