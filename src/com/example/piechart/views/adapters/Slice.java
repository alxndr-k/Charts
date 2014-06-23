package com.example.piechart.views.adapters;

import java.io.Serializable;
import java.util.Random;

public class Slice implements Serializable {

    private static final int COLOR_OFFSET = 64;
    private static final int COLOR_MAX = 255;
    private static final int COLOR_RANDOMIZE = COLOR_MAX - COLOR_OFFSET;

    private static final int BYTE_SHIFT = 8;

    private static final Random sRand = new Random();

    public float value;
    public int color;

    public Slice(float value) {
        this.value = value;
        this.color = getColor();
    }

    public Slice(float value, int color) {
        this.value = value;
        this.color = color;
    }

    private int getColor() {
        int a = getRandomColor() << (BYTE_SHIFT * 3);
        int r = getRandomColor() << (BYTE_SHIFT * 2);
        int g = getRandomColor() << BYTE_SHIFT;
        int b = getRandomColor();

        return a | r | g | b;
    }

    private int getRandomColor() {
        return COLOR_OFFSET + sRand.nextInt(COLOR_RANDOMIZE);
    }
}
