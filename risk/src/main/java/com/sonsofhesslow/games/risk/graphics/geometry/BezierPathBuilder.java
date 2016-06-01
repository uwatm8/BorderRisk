package com.sonsofhesslow.games.risk.graphics.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BezierPathBuilder {
    private final List<Bezier> beziers = new ArrayList<>();

    public boolean addBeiz(Bezier bezier) {
        if (beziers.isEmpty() || Vector2.almostEqual(bezier.points[0], (beziers.get(beziers.size() - 1).points[3]))) {
            beziers.add(bezier);
            return true;
        } else {
            return false;
        }
    }

    public void addBeizPath(BezierPath beizPath) {
        for (Bezier b : beizPath) {
            if (!addBeiz(b)) throw new RuntimeException("fuck me");
        }
    }

    public boolean fitAndAddBeizPath(BezierPath addition) {
        if (addition.isClosed())
            throw new RuntimeException("self intersecting bezierpaths is not supported");
        Vector2 currentFirst = beziers.get(0).points[0];
        Vector2 currentLast = beziers.get(beziers.size() - 1).points[3];
        Vector2 addedFirst = addition.points[0];
        Vector2 addedLast = addition.points[addition.points.length - 1];

        if (Vector2.almostEqual(currentLast, addedFirst)) {
            addBeizPath(addition);
            return true;
        }

        if (Vector2.almostEqual(currentLast, addedLast)) {
            addBeizPath(addition.reverse());
            return true;
        }

        if (Vector2.almostEqual(currentFirst, addedLast)) {
            BezierPath old = get(false);
            clear();
            addBeizPath(addition);
            addBeizPath(old);
            return true;
        }

        if (Vector2.almostEqual(currentFirst, addedFirst)) {
            BezierPath old = get(false);
            clear();
            addBeizPath(old.reverse());
            addBeizPath(addition);
            return true;
        }
        return false;
    }


    public void clear() {
        beziers.clear();
    }

    public BezierPath get(boolean close) {
        if (close) {
            if (!Vector2.almostEqual(beziers.get(0).points[0], (beziers.get(beziers.size() - 1).points[3]))) {
                throw new RuntimeException("screwed up beiz");
            } else {
                Vector2[] points = new Vector2[beziers.size() * 3];
                int i = 0;
                for (Bezier b : beziers) {
                    points[i++] = b.points[0];
                    points[i++] = b.points[1];
                    points[i++] = b.points[2];
                }
                return new BezierPath(points);
            }
        } else {
            Vector2[] points = new Vector2[beziers.size() * 3 + 1];
            int i = 0;
            for (Bezier b : beziers) {
                points[i++] = b.points[0];
                points[i++] = b.points[1];
                points[i++] = b.points[2];
            }
            Vector2 endPoint = beziers.get(beziers.size() - 1).points[3];

            points[i] = endPoint;

            return new BezierPath(points);
        }
    }
}
