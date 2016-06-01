package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;


public abstract class GLObject {
    private final Renderer renderer;
    public boolean isActive = true;
    public float drawOrder = 0;
    float[] modelMatrix = new float[16];
    private Vector3 pos = Vector3.Zero();

    GLObject(Renderer renderer) {
        this.renderer = renderer;
        modelMatrix = new float[]
                {
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1,
                };
        renderer.delayedInit(this);
    }

    public float[] getMatrix() { //exposes the matrix, which intentionally allows for changes to it.
        return modelMatrix;
    }

    public void remove() {
        renderer.remove(this);
    }

    public Vector3 getPos() {
        return new Vector3(pos); // the return value cannot modify our state.
    }

    public void setPos(Vector2 vec) {
        setPos(new Vector3(vec, 0));
    }

    public void setPos(Vector3 vec) {
        pos = vec;
        modelMatrix[12] = vec.x;
        modelMatrix[13] = vec.y;
        modelMatrix[14] = vec.z;
        drawOrder = -vec.z;
    }

    public abstract void draw(float[] projectionMatrix);

    public abstract void glInit();
}
