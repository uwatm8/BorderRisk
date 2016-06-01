package com.sonsofhesslow.games.risk;

public class Util {
    public static int getIntFromColor(float Red, float Green, float Blue) {
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    public static int getIntFromColor(float[] c) {
        return getIntFromColor(c[0], c[1], c[2]);
    }

    public static float[] getFloatFromIntColor(int color) {
        int R = (color >> 16) & 0x0000ff;
        int G = (color >> 8) & 0x000000ff;
        int B = color & 0x000000FF;
        return new float[]{(float) R / 255f, (float) G / 255f, (float) B / 255f, 1};
    }
}
