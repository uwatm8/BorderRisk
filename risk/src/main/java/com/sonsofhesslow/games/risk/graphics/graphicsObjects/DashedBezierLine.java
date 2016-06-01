package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

public class DashedBezierLine extends GLObject {
    private final Mesh mesh;
    private static final int naivePrecision = 30; //higher is more detailed
    private DefaultShader shader;

    public DashedBezierLine(BezierPath path, Renderer renderer) // start ctl ctl point ctl ctl point ctl ctl (start)
    {
        super(renderer);
        Vector2[] verts = path.approximatebezierPathNaive(naivePrecision);
        Vector2[] outlineVerts = new Vector2[verts.length * 2];

        //while the triangles are not guaranteed to be non-overlapping constant winding.
        // the way we render the triangles we just dont care.
        short[] outlineTris = new short[6 * outlineVerts.length];
        for (int i = 0; i < verts.length; i++) {
            Vector2 prev = verts[i];
            Vector2 current = verts[(i + 1) % verts.length];
            Vector2 next = verts[Math.min((i + 2), verts.length - 1)];
            Vector2 diff = Vector2.sub(next, prev);
            Vector2 orth = Vector2.mul(new Vector2(-diff.y, diff.x).normalized(), 0.01f); //think about this.

            outlineVerts[i * 2 + 0] = Vector2.add(current, orth);
            outlineVerts[i * 2 + 1] = Vector2.sub(current, orth);

            if (i % 2 == 0) {
                outlineTris[i * 6 + 0] = (short) (i * 2 + 0);
                outlineTris[i * 6 + 1] = (short) (i * 2 + 1);
                outlineTris[i * 6 + 2] = (short) ((i * 2 + 2) % outlineVerts.length);

                outlineTris[i * 6 + 3] = (short) (i * 2 + 1);
                outlineTris[i * 6 + 4] = (short) ((i * 2 + 2) % outlineVerts.length);
                outlineTris[i * 6 + 5] = (short) ((i * 2 + 3) % outlineVerts.length);
            }
        }

        mesh = new Mesh(outlineTris, outlineVerts);
    }

    @Override
    public void glInit() {
        mesh.init();
        shader = new DefaultShader();
    }

    public void draw(float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        shader.use(mesh, mvpMatrix, new float[]{0, 0, 0, 1});
    }
}
