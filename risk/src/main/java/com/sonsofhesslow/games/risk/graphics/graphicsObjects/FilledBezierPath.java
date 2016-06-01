package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class FilledBezierPath extends GLObject implements Updatable {

    public Mesh fillMesh;
    public BezierPath path;
    boolean doRest = false;
    private Mesh outlineMesh;
    private FlowShader flowShader;
    private LineShader lineShader;
    private Vector2 center;
    private float Area;
    private FloatBuffer vertSide;
    private float[] fromColor = {0.7f, 0.7f, 0.7f, 1f};
    private float[] toColor = {0.7f, 0.7f, 0.7f, 1f};
    private float[] outlineColor = new float[]{0, 0, 0, 1};
    private float maxLen = 20;
    private float len = 20;
    private Vector3 origin;

    public FilledBezierPath(BezierPath path, Renderer renderer) // start ctl ctl point ctl ctl point ctl ctl (start)
    {
        super(renderer);
        if (!path.isClosed())
            throw new IllegalArgumentException("the bezier path needs to be closed!");
        this.path = path;

        Vector2[] verts = path.approximatebezierPath(0.001f);
        //Vector2[] verts = path.approximatebezierPathNaive(naive_precision);

        Vector2[] outlineVerts = new Vector2[verts.length * 2];

        //while the triangles are not guaranteed to be non-overlapping constant winding.
        // the way we render the triangles we just dont care.
        short[] outlineTris = new short[6 * outlineVerts.length];
        float[] VertSideArr = new float[outlineVerts.length];
        for (int i = 0; i < verts.length; i++) {
            Vector2 prev = verts[i];
            Vector2 current = verts[(i + 1) % verts.length];
            Vector2 next = verts[(i + 2) % verts.length];
            Vector2 diffa = Vector2.sub(next, current).normalized();
            Vector2 diffb = Vector2.sub(current, prev).normalized();
            Vector2 diff = Vector2.add(diffa, diffb).normalized();
            float scaleFactor = Math.max(Math.abs(Vector2.dot(diff, diffa)), 0.8f);
            Vector2 orth = Vector2.mul(new Vector2(-diff.y, diff.x), 1 / scaleFactor * 0.01f);

            outlineVerts[i * 2 + 0] = Vector2.add(current, orth);
            outlineVerts[i * 2 + 1] = Vector2.sub(current, orth);

            VertSideArr[i * 2 + 0] = 0;
            VertSideArr[i * 2 + 0] = 1;

            outlineTris[i * 6 + 0] = (short) (i * 2 + 0);
            outlineTris[i * 6 + 1] = (short) (i * 2 + 1);
            outlineTris[i * 6 + 2] = (short) ((i * 2 + 2) % outlineVerts.length);

            outlineTris[i * 6 + 3] = (short) (i * 2 + 1);
            outlineTris[i * 6 + 4] = (short) ((i * 2 + 2) % outlineVerts.length);
            outlineTris[i * 6 + 5] = (short) ((i * 2 + 3) % outlineVerts.length);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(VertSideArr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertSide = bb.asFloatBuffer();
        vertSide.put(VertSideArr);
        vertSide.position(0);

        outlineMesh = new Mesh(outlineTris, outlineVerts);


        //finding out the most prominent winding order
        float windAck = 0;
        for (int i = 0; i < verts.length; i++) {
            Vector2 cur = verts[i];
            Vector2 next = verts[(i + 1) % verts.length];
            windAck += (next.x - cur.x) * (next.y + cur.y);
        }
        float winding = Math.signum(windAck);

        //setup the remaining vertex indices
        short[] tris = new short[(verts.length - 2) * 3];
        int currentIndex = 0;
        List<Integer> remainingIndices = new LinkedList<>();
        for (int i = 0; i < verts.length; i++) {
            remainingIndices.add(i);
        }
        //triangulation by earclipping not perfect....
        while (remainingIndices.size() >= 3) {
            boolean removed = false;
            float acceptableConcavity = 0;
            for (int i = 0; i < remainingIndices.size(); i++) {
                int indexA = remainingIndices.get(i);
                int indexB = remainingIndices.get((i + 1) % remainingIndices.size());
                int indexC = remainingIndices.get((i + 2) % remainingIndices.size());

                Vector2 a = verts[indexA];
                Vector2 b = verts[indexB];
                Vector2 c = verts[indexC];

                //only add the tri if it's inside the polygon
                float concavity = Vector2.crossProduct(a, b, c);
                if (Math.signum(concavity) != winding || Math.abs(concavity) <= acceptableConcavity || doRest) {
                    //check if there is any other vertex inside our proposed triangle
                    boolean noneInside = true;
                    if (!doRest)
                        for (int j = 0; j < verts.length; j++) {
                            if (j == indexA || j == indexB || j == indexC) continue;
                            if (Vector2.isInsideTri(verts[j], a, b, c)) {
                                noneInside = false;
                                break;
                            }
                        }

                    if (noneInside || doRest) {
                        //add the triangle and remove the middle vertex from further consideration
                        tris[currentIndex++] = (short) indexA;
                        tris[currentIndex++] = (short) indexB;
                        tris[currentIndex++] = (short) indexC;

                        remainingIndices.remove((i + 1) % remainingIndices.size());
                        removed = true;
                        if (remainingIndices.size() == 2) break;
                    }
                }
            }
            if (!removed) {
                doRest = true;
            }
            if (remainingIndices.size() <= 2) break;
        }

        fillMesh = new Mesh(tris, verts);
        calcCenter();
        origin = new Vector3(center, 0);
    }

    @Override
    public void glInit() {
        lineShader = new LineShader();
        flowShader = new FlowShader();
        fillMesh.init();
        outlineMesh.init();
    }

    private void calcCenter() {
        // from https://en.wikipedia.org/wiki/Centroid
        float x = 0;
        float y = 0;
        float A = 0;
        for (int i = 0; i < fillMesh.vertices.length; i++) {
            Vector2 curr = fillMesh.vertices[i];
            Vector2 next = fillMesh.vertices[(i + 1) % fillMesh.vertices.length];
            float a = curr.x * next.y - next.x * curr.y;
            x += (curr.x + next.x) * a;
            y += (curr.y + next.y) * a;
            A += a;
        }
        Area = Math.abs(A / 2f);
        float nrm = 1f / (3f * A);
        center = new Vector2(x * nrm, y * nrm);
    }

    public Vector2 getCenter() {
        return center;
    }

    public void setColorOutline(float[] color) {
        outlineColor = color.clone();
    }

    public void mergeWith(FilledBezierPath other) {
        fillMesh = Mesh.add(fillMesh, other.fillMesh);
        outlineMesh = Mesh.add(outlineMesh, other.outlineMesh);

        ByteBuffer bb = ByteBuffer.allocateDirect((vertSide.capacity() + other.vertSide.capacity()) * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer VertSideNew = bb.asFloatBuffer();
        VertSideNew.put(vertSide);
        VertSideNew.put(other.vertSide);
        VertSideNew.position(0);
        vertSide = VertSideNew;
        center = Vector2.mul(Vector2.add(Vector2.mul(center, Area), Vector2.mul(other.center, other.Area)), 1f / (other.Area + Area));
    }

    public void setColor(float[] color, Vector2 origin) {
        this.origin = new Vector3(origin, 0);
        maxLen = 20;
        len = 0;
        fromColor = toColor;
        toColor = color.clone();
    }

    public void setColor(float[] color) {
        setColor(color, center);
    }


    @Override
    public boolean update(float dt) {
        if (Math.abs(maxLen - len) > 0.01) {
            len += (maxLen - len) / 200;
            return true;
        }
        return false;
    }


    public void draw(float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        flowShader.use(fillMesh, mvpMatrix, origin, len, toColor, fromColor);
        lineShader.use(outlineMesh, mvpMatrix, outlineColor, vertSide);
    }
}
