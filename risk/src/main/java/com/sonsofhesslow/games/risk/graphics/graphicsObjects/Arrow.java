package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

public class Arrow extends GLObject {
    private final Mesh mesh;
    private final Mesh outlineMesh;
    private final float[] color;
    private DefaultShader shader;

    public Arrow(Vector2 from, Vector2 to, float[] color, Renderer renderer) {
        super(renderer);
        this.color = color.clone();

        // verts
        //   0
        //1 23 4
        //
        //  56

        // tris
        // /_\
        //  |
        //  |
        float width = 0.02f;
        float headRatio = 4f;
        float outline = 0.01f;
        Vector2 dir = Vector2.sub(from, to).normalized();
        Vector2 orth = Vector2.mul(new Vector2(-dir.y, dir.x), width);
        Vector2 orthHead = Vector2.mul(orth, headRatio);
        Vector2 bottomHead = Vector2.sub(to, Vector2.mul(dir, -(width * headRatio)));

        short[] tris = new short[]{0, 1, 4, 3, 2, 5, 5, 6, 3};

        Vector2[] verts = new Vector2[]
                {
                        Vector2.add(to, Vector2.mul(dir, outline)),
                        Vector2.sub(bottomHead, orthHead),   // left left
                        Vector2.sub(bottomHead, orth),      // left center
                        Vector2.add(bottomHead, orth),      // right center
                        Vector2.add(bottomHead, orthHead),   // right right
                        Vector2.sub(from, orth),            // left bottom
                        Vector2.add(from, orth),             // right bottom
                };
        mesh = new Mesh(tris, verts);

        orth = Vector2.mul(new Vector2(-dir.y, dir.x), width + outline);
        orthHead = Vector2.mul(new Vector2(-dir.y, dir.x), headRatio * width + outline * 2f);
        bottomHead = Vector2.sub(to, Vector2.mul(dir, -(width * headRatio + outline)));

        tris = new short[]{0, 1, 4, 3, 2, 5, 5, 6, 3};
        verts = new Vector2[]
                {
                        to,
                        Vector2.sub(bottomHead, orthHead),   // left left
                        Vector2.sub(bottomHead, orth),      // left center
                        Vector2.add(bottomHead, orth),      // right center
                        Vector2.add(bottomHead, orthHead),   // right right
                        Vector2.sub(from, orth),            // left bottom
                        Vector2.add(from, orth),             // right bottom
                };
        outlineMesh = new Mesh(tris, verts);
    }

    @Override
    public void draw(float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        shader.use(outlineMesh, projectionMatrix, new float[]{0, 0, 0, 0.6f});
        shader.use(mesh, projectionMatrix, color);
    }

    @Override
    public void glInit() {
        mesh.init();
        outlineMesh.init();
        shader = new DefaultShader();
    }
}
