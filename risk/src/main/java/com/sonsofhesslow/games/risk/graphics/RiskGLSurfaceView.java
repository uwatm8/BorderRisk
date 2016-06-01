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

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.FilledBezierPath;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class RiskGLSurfaceView extends GLSurfaceView {
    private final ScaleGestureDetector SGD;
    private final GLRenderer mRenderer;
    private final ConcurrentLinkedQueue<GLTouchListener> listeners = new ConcurrentLinkedQueue<>();
    private float scale = -3.0f;
    private boolean isZooming = false;
    //for not clicking when releasing after scrolling
    private float downX;
    private float downY;

    public RiskGLSurfaceView(Context context, Resources resources) {
        super(context);
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        super.setEGLConfigChooser(new MyConfigChooser());
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLRenderer();
        GraphicsManager.getInstance().init(resources, mRenderer,this);
        setRenderer(mRenderer);
        SGD = new ScaleGestureDetector(getContext(), new ScaleListener());

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public RiskGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        //super.setEGLConfigChooser(new MyConfigChooser());
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new GLRenderer();
        setRenderer(mRenderer);
        SGD = new ScaleGestureDetector(getContext(), new ScaleListener());

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void addListener(GLTouchListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        SGD.onTouchEvent(e);
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        GLTouchEvent event;
        Vector2 screenPos = new Vector2(e.getX(), e.getY());
        Vector2 worldPos = mRenderer.screenToWorldCoords(screenPos, 0);

        if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            downX = e.getX();
            downY = e.getY();
        }

        if ((e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_POINTER_UP)
                && (Math.abs(e.getX() - downX + e.getY() - downY) <= 10)) {
            int index = 0;
            boolean hasTouchedRegion = false;
            Vector2 worldPosStitched = mRenderer.screenToWorldCoorsStitched(screenPos, 0);
            for (FilledBezierPath path : GraphicsManager.getInstance().beziers) {
                float z = path.getPos().z;
                Vector2 adjustedWorldPos;
                if (z == 0)
                    adjustedWorldPos = worldPosStitched;
                else
                    adjustedWorldPos = mRenderer.screenToWorldCoorsStitched(screenPos, z);

                if (path.fillMesh.isOnMesh2D(adjustedWorldPos)) {
                    hasTouchedRegion = true;
                    break;
                }
                ++index;
            }
            if (!hasTouchedRegion) index = -1;
            event = new GLTouchEvent(e, hasTouchedRegion, isZooming, index, worldPos, screenPos, scale);

        } else {
            event = new GLTouchEvent(e, false, isZooming, -1, worldPos, screenPos, scale);
        }
        for (GLTouchListener listener : listeners) {
            listener.handle(event);
        }

        return true;
    }

    private static class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_TRANSPARENT_RGB,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,
                    EGL10.EGL_STENCIL_SIZE, 2,
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                return null;
            } else {
                return configs[0];
            }
        }
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= 1 + (1 - detector.getScaleFactor());
            scale = Math.min(-1.1f, Math.max(scale, -6.0f));
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isZooming = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isZooming = false;
        }
    }

}

