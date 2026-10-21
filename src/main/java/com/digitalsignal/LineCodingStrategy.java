package com.digitalsignal;

import java.util.List;

public interface LineCodingStrategy {
    List<Double> encode(String bits);
    String decode(List<Double> signal);
}
