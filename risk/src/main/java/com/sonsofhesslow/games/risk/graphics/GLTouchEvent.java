package com.sonsofhesslow.games.risk.graphics;

import android.view.MotionEvent;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

public class GLTouchEvent {
    public final MotionEvent e;
    public final boolean touchedRegion;
    public final int regionId;
    public final Vector2 worldPosition;
    public final Vector2 screenPosition;
    public final float scale;
    public final boolean isZooming;

    public GLTouchEvent(MotionEvent e, boolean touchedRegion, boolean isZooming, int regionId, Vector2 worldPosition, Vector2 screenPosition, float scale) {
        this.e = e;
        this.touchedRegion = touchedRegion;
        this.regionId = regionId;
        this.worldPosition = worldPosition;
        this.screenPosition = screenPosition;
        this.scale = scale;
        this.isZooming = isZooming;
    }
}
