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
package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.utils.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Mesh {

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    final short[] triangles;
    final Vector2[] vertices;
    final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (coord right? //daniel)
    private final float[] newVerts;
    FloatBuffer vertexBuffer;
    ShortBuffer drawListBuffer;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    public Mesh(short[] triangles, Vector2[] vertices) {
        this.vertices = vertices; // no copy for performance reasons
        this.triangles = triangles; // no copy for performance reasons
        newVerts = new float[vertices.length * COORDS_PER_VERTEX];
        for (int i = 0; i < vertices.length; i++) {
            newVerts[i * COORDS_PER_VERTEX] = vertices[i].x;
            newVerts[i * COORDS_PER_VERTEX + 1] = vertices[i].y;
            newVerts[i * COORDS_PER_VERTEX + 2] = 0;
        }
        calculateMetrics();
    }

    public static Mesh add(Mesh a, Mesh b) {
        Vector2[] newVerts = ArrayUtils.concat(a.vertices, b.vertices);
        int atLen = a.triangles.length;
        int btLen = b.triangles.length;

        short[] newTris = new short[atLen + btLen];
        System.arraycopy(a.triangles, 0, newTris, 0, atLen);
        for (int i = 0; i < btLen; i++) {
            newTris[atLen + i] = (short) (b.triangles[i] + a.vertices.length);
        }
        return new Mesh(newTris, newVerts);
    }

    public boolean isOnMesh2D(Vector2 point) {
        int currentTri = 0;
        if (point.x < minX || point.x > maxX || point.y < minY || point.y > maxY) return false;
        while (triangles.length / 3 > currentTri) {
            Vector2 p0 = vertices[triangles[currentTri * 3 + 0]];
            Vector2 p1 = vertices[triangles[currentTri * 3 + 1]];
            Vector2 p2 = vertices[triangles[currentTri * 3 + 2]];
            ++currentTri;
            if (Vector2.isInsideTri(point, p0, p1, p2)) {
                return true;
            }
        }
        return false;
    }

    private void calculateMetrics() {
        Vector2[] verts = vertices;
        minX = verts[0].x;
        maxX = verts[0].x;
        minY = verts[0].y;
        maxY = verts[0].y;
        for (int i = 1; i < verts.length; i++) {
            minX = Math.min(minX, verts[i].x);
            minY = Math.min(minY, verts[i].y);
            maxX = Math.max(maxX, verts[i].x);
            maxY = Math.max(maxY, verts[i].y);
        }
    }

    public void init() {

        if (triangles.length % 3 != 0) {
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(newVerts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(newVerts);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(triangles.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(triangles);
        drawListBuffer.position(0);
    }
}
