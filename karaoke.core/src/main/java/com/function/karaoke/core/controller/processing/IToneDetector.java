package com.function.karaoke.core.controller.processing;

public interface IToneDetector {
    int analyze(short[] data, int read);
}
