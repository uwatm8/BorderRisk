package com.sonsofhesslow.games.risk.graphics;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Updatable;

public class Camera implements Updatable{
    private static Camera instance;
    public final Vector3 up = new Vector3(0, 1, 0);
    private static final float WORLD_MIN_X = -16;
    private static final float WORLD_MAX_X = 0;
    private static final float WORLD_MIN_Y = -12;
    private static final float WORLD_MAX_Y = 5;
    public float stitchPosition;
    public Vector3 pos = new Vector3(-5, -5, -3); //-5-5 is about the current center..
    public Vector3 lookAt;

    private Camera() {
        updateLookAt();
    }

    public static Camera getInstance() {
        if (instance == null) instance = new Camera();
        return instance;
    }

    private float clamp(float f, float min, float max) {
        return Math.min(Math.max(min, f), max);
    }
    public void setPos(Vector3 newPos)
    {
        startPos = null;
        endPos = null;
        setPos_internal(newPos);
    }
    private void setPos_internal(Vector3 newPos) {

        float width = GLRenderer.viewPortToWorldCoord(new Vector2(-1, 0), 0).x
                - GLRenderer.viewPortToWorldCoord(new Vector2(+1, 0), 0).x;

        float height = GLRenderer.viewPortToWorldCoord(new Vector2(0, -1), 0).y
                - GLRenderer.viewPortToWorldCoord(new Vector2(0, 1), 0).y;


        width = Math.abs(width);
        height = Math.abs(height);

        float minX = WORLD_MIN_X + width / 2;
        float maxX = WORLD_MAX_X - width / 2;
        float minY = WORLD_MIN_Y + height / 2;
        float maxY = WORLD_MAX_Y - height / 2;

        float WORLD_WIDTH = WORLD_MAX_X - WORLD_MIN_X;
        float newX = newPos.x;
        float newY = clamp(newPos.y, minY, maxY);

        if (newPos.x < minX) {
            stitchPosition = (1 - (minX - newPos.x) / width);
        } else if (newPos.x > maxX) {
            stitchPosition = (-((maxX - newPos.x) / width));
        } else {
            stitchPosition = 0;
        }

        if (stitchPosition > 1f) {
            newX -= WORLD_WIDTH;
        }
        if (stitchPosition < 0.0f) {
            newX += WORLD_WIDTH;
        }

        pos = new Vector3(newX, newY, newPos.z);
        updateLookAt();
    }

    public Camera[] getStitchCams() {
        float width = GLRenderer.viewPortToWorldCoord(new Vector2(-1, 0), 0).x
                - GLRenderer.viewPortToWorldCoord(new Vector2(+1, 0), 0).x;
        width = Math.abs(width);

        Camera left = new Camera();
        Camera right = new Camera();
        left.pos = new Vector3(WORLD_MIN_X + width * stitchPosition / 2, pos.y, pos.z);
        right.pos = new Vector3(WORLD_MAX_X - width * (1 - stitchPosition) / 2, pos.y, pos.z);

        left.updateLookAt();
        right.updateLookAt();
        return new Camera[]{left, right};
    }
    private Vector2 startPos = null;
    private Vector2 endPos = null;
    public void setPosRel(Vector3 newPos) {
        setPos(Vector3.Add(newPos, pos));
        updateLookAt();
    }

    public void moveCameraTowards(Vector2 pos)
    {
        startPos = this.pos.ToVector2();
        endPos = pos;
        currentTime = 0;
    }


    private void updateLookAt() {
        lookAt = new Vector3(pos.x, pos.y, 0);
    }

    public float[] getViewMatrix() {
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, pos.x, pos.y, pos.z, lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z);
        return viewMatrix;
    }

    public float currentTime=0;
    public float interpolationTime = 500f;

    @Override
    public boolean update(float dt) {
        if(startPos != null && endPos != null)
        {
            currentTime+=dt;
            float lerpT=Math.min(currentTime/interpolationTime,1);
            setPos_internal(new Vector3(Vector2.lerp(startPos,endPos,lerpT),pos.z));
            if(currentTime >=interpolationTime)
            {
                startPos = null;
                endPos = null;
            }
            return true;
        }
        return false;
    }
}
