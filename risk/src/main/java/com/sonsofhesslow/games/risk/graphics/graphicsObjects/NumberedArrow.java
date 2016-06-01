package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

public class NumberedArrow {
    private final Number number;
    private final Arrow arrow;

    public NumberedArrow(Renderer renderer, Vector2 from, Vector2 to, float[] color, int value) {
        number = new Number(value, renderer, color);
        float angle = 0;
        float scale = 0.3f;
        float[] transMatrix = new float[16];
        Vector2 pos = Vector2.lerp(from, to, 0.5f);
        Matrix.translateM(transMatrix, 0, number.getMatrix(), 0, pos.x - 0.5f * scale, pos.y - 0.5f * scale, 0);

        float[] rotMatrix = new float[16];
        Matrix.rotateM(rotMatrix, 0, transMatrix, 0, (float) Math.toDegrees(angle), 0, 0, 1);

        Matrix.scaleM(number.getMatrix(), 0, rotMatrix, 0, scale, scale, scale);

        arrow = new Arrow(from, to, color, renderer);
        arrow.drawOrder = 10000;
        number.drawOrder = 10001;
    }
    public void remove() {
        number.remove();
        arrow.remove();
    }

    public int getValue() {
        return number.getValue();
    }

    public void setValue(int value) {
        number.setValue(value);
    }
}
