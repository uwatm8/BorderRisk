package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import android.opengl.GLES20;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.utils.ShaderUtils;

class FlowShader {

    private static final String vertexShaderCode =
            "uniform mat4 matrix;" +
                    "attribute vec4 position;" +
                    "varying vec3 pos;" +
                    "void main() {" +
                    "  gl_Position = matrix * position;" +
                    "pos = vec3(position);" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec3 pos;" +
                    "uniform vec4 color_from;" +
                    "uniform vec4 color_to;" +
                    "uniform vec3 origin;" +
                    "uniform float max_dist_sq;" +
                    "void main() {" +
                    "vec3 sub = origin-pos;" +
                    "float dist_sq = dot(sub,sub);" +
                    "float s = 0.4;" +
                    "float f = smoothstep(max_dist_sq-s,max_dist_sq+s,dist_sq);" +
                    "gl_FragColor = mix(color_from,color_to,f);" +
                    "}";

    private int flowShader = -1;
    private static int positionHandle;
    private static int fromColorHandle;
    private static int toColorHandle;
    private static int maxDistHandle;
    private static int matrixHandle;
    public FlowShader() {
        // prepare shaders and OpenGL program
        int vertexShader = ShaderUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ShaderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        flowShader = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(flowShader, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(flowShader, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(flowShader);                  // create OpenGL program executables

        positionHandle = GLES20.glGetAttribLocation(flowShader, "position");
        fromColorHandle = GLES20.glGetUniformLocation(flowShader, "color_from");
        toColorHandle = GLES20.glGetUniformLocation(flowShader, "color_to");
        maxDistHandle = GLES20.glGetUniformLocation(flowShader, "max_dist_sq");
        matrixHandle = GLES20.glGetUniformLocation(flowShader, "matrix");
    }

    void use(Mesh mesh, float[] matrix, Vector3 origin, float maxDistance, float[] fromColor, float[] toColor) {
        GLES20.glUseProgram(flowShader);
        final int COORDS_PER_VERTEX = 3;


        ShaderUtils.checkGlError("glGetUniformLocation");

        //position
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mesh.vertexStride, mesh.vertexBuffer);

        //color
        GLES20.glUniform4fv(fromColorHandle, 1, fromColor, 0);
        GLES20.glUniform4fv(toColorHandle, 1, toColor, 0);

        float[] originArr = new float[]{origin.x, origin.y, origin.z};
        final int originHandle = GLES20.glGetUniformLocation(flowShader, "origin");
        GLES20.glUniform3fv(originHandle, 1, originArr, 0);
        GLES20.glUniform1f(maxDistHandle, maxDistance * maxDistance);
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mesh.triangles.length,
                GLES20.GL_UNSIGNED_SHORT, mesh.drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
