/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sonsofhesslow.games.risk.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.GLObject;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer, Renderer {

    private static final List<GLObject> gameObjects = new ArrayList<>();
    private static final ConcurrentLinkedQueue<GLObject> objectsToBeAdded = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<GLObject> objectsToBeRemoved = new ConcurrentLinkedQueue<>();
    private static int width;
    private static int height;

    private static float[] calculateProjectionMatrix(float height, float width) {
        float[] projectionMatrix = new float[16];
        float ratio = width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
        return projectionMatrix;
    }

    private static Vector2 viewPortToWorldCoord(Vector2 point, float zOut, float[] projectionMatrix, float[] viewMatrix) {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] invTransformMatrix = new float[16];
        Matrix.invertM(invTransformMatrix, 0, transformMatrix, 0);
        if (invTransformMatrix[10] == 0) {
            return point;
        }
        float glX = point.x;
        float glY = point.y;
        float glZ = (invTransformMatrix[2] * glX + invTransformMatrix[6] * glY + invTransformMatrix[14] - zOut) / -invTransformMatrix[10];

        float[] pointInGL = new float[]{glX, glY, glZ, 1};
        float[] ret = new float[4];
        Matrix.multiplyMV(ret, 0, invTransformMatrix, 0, pointInGL, 0);

        // avoid div with 0. Don't know if this is a problem
        if (ret[3] == 0.0)
            throw new RuntimeException("2: viewPort to world cords failed, div by zero");

        return new Vector2(ret[0] / ret[3], ret[1] / ret[3]);
    }

    public static Vector2 viewPortToWorldCoord(Vector2 point, float zOut) {
        return viewPortToWorldCoord(point, zOut, calculateProjectionMatrix(height, width), Camera.getInstance().getViewMatrix());
    }

    private static Vector2 screenToWorldCoords(Vector2 point, float zOut, int width, int height, float[] viewMatrix) {
        float glX = ((point.x) * 2.0f / width - 1.0f);
        float glY = ((height - point.y) * 2.0f / height - 1.0f);
        return viewPortToWorldCoord(new Vector2(glX, glY), zOut, calculateProjectionMatrix(height, width), viewMatrix);
    }

    public static Vector2 screenToWorldCoords(Vector2 point, float zOut) {
        return screenToWorldCoords(point, zOut, width, height, Camera.getInstance().getViewMatrix());
    }

    public static Vector2 screenToWorldCoorsStitched(Vector2 point, float zOut) {
        Camera cam = Camera.getInstance();
        if (cam.stitchPosition > 0 && cam.stitchPosition < 1) {
            Camera[] cams = cam.getStitchCams();
            int x = (int) (width * cam.stitchPosition);
            if (x != 0 && x != width) {
                if (point.x < x) {//left
                    return screenToWorldCoords(
                            new Vector2(point.x, point.y),
                            zOut,
                            x,
                            height,
                            cams[0].getViewMatrix());
                } else {//right
                    return screenToWorldCoords(
                            new Vector2(point.x - x, point.y),
                            zOut,
                            width - x,
                            height,
                            cams[1].getViewMatrix());
                }
            }
        }
        return screenToWorldCoords(new Vector2(point.x, point.y), zOut);
    }

    public void delayedInit(GLObject m) {
        objectsToBeAdded.add(m);
    }

    public void remove(GLObject object) {
        objectsToBeRemoved.add(object);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.8f, 1f, 1f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        frameInit();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Camera cam = Camera.getInstance();
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, cam.pos.x, cam.pos.y, cam.pos.z, cam.lookAt.x, cam.lookAt.y, cam.lookAt.z, cam.up.x, cam.up.y, cam.up.z);

        if (cam.stitchPosition > 0 && cam.stitchPosition < 1) {
            Camera[] cams = cam.getStitchCams();
            int x = (int) (width * cam.stitchPosition);
            if (x != 0 && x != width) {
                render(0, 0, x, height, cams[0]);
                render(x, 0, width, height, cams[1]);
                return;
            }
        }
        render(0, 0, width, height, cam);
    }

    private void frameInit() {
        for (GLObject go : objectsToBeRemoved) {
            objectsToBeAdded.remove(go);
            gameObjects.remove(go);
        }
        objectsToBeRemoved.clear();
        for (GLObject go : objectsToBeAdded) {
            go.glInit();
            gameObjects.add(go);
        }
        objectsToBeAdded.clear();
    }

    private void render(int left, int bottom, int right, int top, Camera camera) {
        int width = right - left;
        int height = top - bottom;
        GLES20.glViewport(left, bottom, width, height);

        float[] viewMatrix = camera.getViewMatrix();
        float[] projectionMatrix = calculateProjectionMatrix(height, width);
        float[] MVPMatrix = new float[16];
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Collections.sort(gameObjects, new Comparator<GLObject>() {
            @Override
            public int compare(GLObject lhs, GLObject rhs) {
                return Float.compare(lhs.drawOrder, rhs.drawOrder);
            }
        });
        for (GLObject go : gameObjects) {
            if (go.isActive)
                go.draw(MVPMatrix);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLRenderer.width = width;     // the screen can only ever be of one size
        GLRenderer.height = height;   // statics are fine.
    }
}
