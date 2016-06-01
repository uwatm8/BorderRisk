package com.sonsofhesslow.games.risk.graphics.geometry;

import android.util.Pair;

import java.util.List;

public class Bezier {
    public final Vector2[] points;

    public Bezier(Vector2 start, Vector2 control1, Vector2 control2, Vector2 end) {
        this(new Vector2[]{start, control1, control2, end});
    }

    public Bezier(Vector2[] points) {
        this.points = points; //no copy for performance reasons
    }

    private static Vector2[] DeCastillioStep(Vector2[] vectors, float t) {
        Vector2[] NextVectors = new Vector2[vectors.length - 1];
        for (int i = 0; i < vectors.length - 1; i++) {
            NextVectors[i] = Vector2.lerp(vectors[i], vectors[i + 1], t);
        }
        return NextVectors;
    }

    public static boolean intersect(Bezier a, Bezier b, float tolerance, List<Pair<Float, Float>> _out_tab) {
        return intersect(a, b, tolerance, _out_tab, 0f, 1f, 0f, 1f);
    }

    private static boolean intersect(Bezier a, Bezier b, float tolerance) {
        return intersect(a, b, tolerance, 0f, 1f, 0f, 1f);
    }

    public static boolean isPartOf(Bezier a, Bezier b, int resolution) {
        float[] ts = new float[resolution];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = (i + 1) / ((float) ts.length + 1);
        }

        Bezier[] splitsA = a.split(ts);
        Bezier[] splitsB = b.split(ts);

        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                if (Bezier.intersect(splitsA[i], splitsB[j], 0.01f)) {
                    return true;
                }
            }
        }
        return false;

    }

    //todo do real bounding in the intersections. Also probably tolerance by area if we want to be somewhat fancy.
    private static boolean intersect(Bezier bezierA, Bezier bezierB, float tolerance,
                                     List<Pair<Float, Float>> _out_tab, float taLow, float taHigh, float tbLow, float tbHigh) {
        Vector2[] a = bezierA.points;
        Vector2[] b = bezierB.points;

        //using the bezier subdivision algorithm plus keeping track of the t intersection.

        // currently implementation assumes quadratic bezier curves.
        // the algorithm as such could easily be extended to higher level curves

        float taMid = (taLow + taHigh) / 2;
        float tbMid = (tbLow + tbHigh) / 2;


        //lazy bounding box.
        float minAx = a[0].x;
        float maxAx = a[0].x;
        float minAy = a[0].y;
        float maxAy = a[0].y;

        for (int i = 1; i < a.length; i++) {
            if (a[i].x < minAx) minAx = a[i].x;
            if (a[i].y < minAy) minAy = a[i].y;
            if (a[i].x > maxAx) maxAx = a[i].x;
            if (a[i].y > maxAy) maxAy = a[i].y;
        }

        float minBx = b[0].x;
        float maxBx = b[0].x;
        float minBy = b[0].y;
        float maxBy = b[0].y;

        for (int i = 1; i < a.length; i++) {
            if (b[i].x < minBx) minBx = b[i].x;
            if (b[i].y < minBy) minBy = b[i].y;
            if (b[i].x > maxBx) maxBx = b[i].x;
            if (b[i].y > maxBy) maxBy = b[i].y;
        }

        boolean boundIntersectX = minBy < maxAy && maxBy > minAy;
        boolean boundIntersectY = minBx < maxAx && maxBx > minAx;

        if (!(boundIntersectX && boundIntersectY)) {
            return false;
        }

        if (taHigh - taLow < tolerance) {
            _out_tab.add(new Pair<>(taMid, tbMid));
            return true;
        }

        Bezier[] aSplit = bezierA.split(0.5f);
        Bezier a1 = aSplit[0];
        Bezier a2 = aSplit[1];

        Bezier[] bSplit = bezierB.split(0.5f);
        Bezier b1 = bSplit[0];
        Bezier b2 = bSplit[1];

        boolean intersect11 = intersect(a1, b1, tolerance, _out_tab, taLow, taMid, tbLow, tbMid);
        boolean intersect12 = intersect(a1, b2, tolerance, _out_tab, taLow, taMid, tbMid, tbHigh);
        boolean intersect21 = intersect(a2, b1, tolerance, _out_tab, taMid, taHigh, tbLow, tbMid);
        boolean intersect22 = intersect(a2, b2, tolerance, _out_tab, taMid, taHigh, tbMid, tbHigh);

        return intersect11 || intersect12 || intersect21 || intersect22;
    }

    private static boolean intersect(Bezier bezierA, Bezier bezierB, float tolerance,
                                     float taLow, float taHigh, float tbLow, float tbHigh) {
        Vector2[] a = bezierA.points;
        Vector2[] b = bezierB.points;

        //using the bezier subdivision algorithm plus keeping track of the t intersection.

        // currently implementation assumes quadratic bezier curves.
        // the algorithm as such could easily be extended to higher level curves

        float taMid = (taLow + taHigh) / 2;
        float tbMid = (tbLow + tbHigh) / 2;


        //lazy bounding box.
        float minAx = a[0].x;
        float maxAx = a[0].x;
        float minAy = a[0].y;
        float maxAy = a[0].y;

        for (int i = 1; i < a.length; i++) {
            if (a[i].x < minAx) minAx = a[i].x;
            if (a[i].y < minAy) minAy = a[i].y;
            if (a[i].x > maxAx) maxAx = a[i].x;
            if (a[i].y > maxAy) maxAy = a[i].y;
        }

        float minBx = b[0].x;
        float maxBx = b[0].x;
        float minBy = b[0].y;
        float maxBy = b[0].y;

        for (int i = 1; i < a.length; i++) {
            if (b[i].x < minBx) minBx = b[i].x;
            if (b[i].y < minBy) minBy = b[i].y;
            if (b[i].x > maxBx) maxBx = b[i].x;
            if (b[i].y > maxBy) maxBy = b[i].y;
        }

        boolean boundIntersectX = minBy < maxAy && maxBy > minAy;
        boolean boundIntersectY = minBx < maxAx && maxBx > minAx;

        if (!(boundIntersectX && boundIntersectY)) {
            return false;
        }

        if (taHigh - taLow < tolerance) {
            return true;
        }

        Bezier[] aSplit = bezierA.split(0.5f);
        Bezier a1 = aSplit[0];
        Bezier a2 = aSplit[1];

        Bezier[] bSplit = bezierB.split(0.5f);
        Bezier b1 = bSplit[0];
        Bezier b2 = bSplit[1];

        return intersect(a1, b1, tolerance, taLow, taMid, tbLow, tbMid) ||
                intersect(a1, b2, tolerance, taLow, taMid, tbMid, tbHigh) ||
                intersect(a2, b1, tolerance, taMid, taHigh, tbLow, tbMid) ||
                intersect(a2, b2, tolerance, taMid, taHigh, tbMid, tbHigh);
    }

    public Bezier[] split(float t) {
        if (points.length != 4)
            throw new IllegalArgumentException("expected quadratic bezier curve!");
        // explicit beiz split using de castillio for quadratic curves
        // could easily be extended for the generic case

        Vector2[] Step0 = points;
        Vector2[] Step1 = DeCastillioStep(Step0, t);
        Vector2[] Step2 = DeCastillioStep(Step1, t);
        Vector2[] step3 = DeCastillioStep(Step2, t);

        Bezier[] ret = new Bezier[2];
        ret[0] = new Bezier(Step0[0], Step1[0], Step2[0], step3[0]);
        ret[1] = new Bezier(step3[0], Step2[1], Step1[2], Step0[3]);
        return ret;
    }

    private Bezier[] split(float[] ts) {
        Bezier[] ret = new Bezier[ts.length];

        float TLeft = 1;
        Bezier current = this;
        float prev = 0;
        for (int i = 0; i < ts.length; i++) {
            Bezier[] split = current.split((ts[i] - prev) / TLeft);
            ret[i] = split[0];
            current = split[1];
            TLeft -= ts[i] - prev;
            prev = ts[i];
        }
        return ret;
    }

    private Bezier DeCatillioReduce(float t) {
        return new Bezier(DeCastillioStep(this.points, t));
    }

    public Vector2 getValue(float t) {
        Bezier nextBeiz = DeCatillioReduce(t);
        if (nextBeiz.points.length == 1) return nextBeiz.points[0];
        else return nextBeiz.getValue(t);
    }

    public boolean isOnCurve(Vector2 p, float precision) {

        float dist = Math.min(Vector2.sub(p, points[0]).magnitude(), Vector2.sub(p, points[3]).magnitude());
        if (dist < precision) return true;
        //the control points are a bounding box of the curve.
        if (!Vector2.isInsideTri(p, points[0], points[1], points[2]) &&
                !Vector2.isInsideTri(p, points[3], points[1], points[2])) {
            return false;
        }

        Bezier[] r = split(0.5f);
        return r[0].isOnCurve(p, precision) || r[1].isOnCurve(p, precision);
    }
}
