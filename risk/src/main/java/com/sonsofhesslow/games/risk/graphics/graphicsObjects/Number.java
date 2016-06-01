package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.text.TextPaint;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;


// renders numbers onto textures and then displays them as textured quads.
// textures are cashed and reused.
public class Number extends GLObject {
    private static int[] textures = null;
    private final Mesh mesh;
    private int num = -1;
    private TexQuadShader shader;
    private float[] color = new float[]{0.3f, 0.5f, 0.5f, 1f};

    public Number(int value, Renderer renderer) {
        super(renderer);
        if (textures == null) { //@multithread unsafe (which is fine for now)
            textures = new int[200];
            for (int i = 0; i < textures.length; i++) {
                textures[i] = -1;
            }
        }
        {
            //setting up the matrix
            Vector2 top_right = new Vector2(1, 1);
            Vector2 top_left = new Vector2(1, 0);
            Vector2 bottom_left = new Vector2(0, 0);
            Vector2 bottom_rigth = new Vector2(0, 1);
            Vector2[] verts = new Vector2[]{top_right, top_left, bottom_left, bottom_rigth};
            short[] tris = new short[]{0, 1, 2, 0, 2, 3};
            mesh = new Mesh(tris, verts);
        }
        num = value;
    }

    public Number(int value, Renderer renderer, float[] color) {
        this(value, renderer);
        setColor(color);
    }

    private static int genTexture(String s) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final int FONT_SIZE = 200;
            // Read in the resource
            final Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(0x00ffffff);
            Canvas canvas = new Canvas(bitmap);
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(FONT_SIZE);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(10);
            textPaint.setColor(0x33000000);
            canvas.drawText(s, xPos, yPos, textPaint);

            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(7);
            textPaint.setColor(0xff000000);
            canvas.drawText(s, xPos, yPos, textPaint);

            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(0xffffffff);
            canvas.drawText(s, xPos, yPos, textPaint);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public int getValue() {
        return num;
    }

    public void setValue(int value) {
        num = value;
    }

    // called back from the gl thread by the renderer for init
    public void glInit() {
        mesh.init();
        shader = new TexQuadShader();
    }

    public void draw(float[] projectionMatrix) {
        float[] matrix = new float[16];
        Matrix.multiplyMM(matrix, 0, projectionMatrix, 0, modelMatrix, 0);
        if (num == -1) return;
        if (textures[num] == -1) {
            textures[num] = genTexture(Integer.toString(num));
        }
        shader.use(mesh, matrix, color, textures[num]);
    }

    public void setColor(float[] color) {
        this.color = color.clone();
    }

    @Override
    public void setPos(Vector3 pos) {
        super.setPos(Vector3.Sub(pos, new Vector3(0.5f, 0.5f, 0)));
    }

    public static void invalidateGLMemory()
    {
        textures = null;
    }
}

