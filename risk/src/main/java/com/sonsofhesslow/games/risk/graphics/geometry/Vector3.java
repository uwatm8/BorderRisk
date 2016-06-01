package com.sonsofhesslow.games.risk.graphics.geometry;

public class Vector3 {
    public final float x;
    public final float y;
    public final float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v) {
        this(v.x, v.y, v.z);
    }

    public Vector3(Vector2 vector2, float z) {
        this(vector2.x, vector2.y, z);
    }

    public static Vector3 Add(Vector3 a, Vector3 b) {
        return new Vector3(a.x + b.x, a.y + b.y, a.z);
    }

    public static Vector3 Sub(Vector3 a, Vector3 b) {
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3 Mul(Vector3 vec, float scalar) {
        return new Vector3(vec.x * scalar, vec.y * scalar, vec.z * scalar);
    }

    public static Vector3 Zero() {
        return new Vector3(0, 0, 0);
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Vector2 ToVector2() {
        return new Vector2(x, y);
    }
}
