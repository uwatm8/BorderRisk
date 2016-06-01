package com.sonsofhesslow.games.risk.graphics.geometry;

import android.util.Pair;

import com.sonsofhesslow.games.risk.graphics.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class BezierPath implements Iterable<Bezier> {

    public Vector2[] points;

    public BezierPath(Vector2[] points) {
        // on the form start c1 c2 p c1 c2 (explicit end)(or implicit start)
        this.points = points; // no copy for performance reasons
    }

    //slow af with all these list ideally we should pass around the same list.
    private static List<Vector2> approximatebezier(Vector2[] beiz, float precision) {
        Bezier bezier = new Bezier(beiz);

        Bezier[] split = bezier.split(0.5f);
        float a = Math.abs(Vector2.crossProduct(beiz[0], Vector2.lerp(beiz[1], beiz[2], 0.5f), beiz[3]));

        List<Vector2> ret = new ArrayList<>();
        if (a < precision) {
            ret.add(new Bezier(beiz).getValue(0));
            return ret;
        } else {
            ret.addAll(approximatebezier(split[0].points, precision));
            ret.addAll(approximatebezier(split[1].points, precision));
            return ret;
        }
    }

    public static SplitReturn splitBeizPath(BezierPath path, BezierPath line) {
        List<Pair<Integer, Float>> pathSplits = new ArrayList<>();
        List<Pair<Integer, Float>> lineSplits = new ArrayList<>();

        List<Pair<Float, Float>> intersectionPoints = new ArrayList<>();

        int i = 0;
        for (Bezier lineBeiz : line) {
            int j = 0;
            for (Bezier pathBeiz : path) {
                if (Bezier.intersect(lineBeiz, pathBeiz, 0.001f, intersectionPoints)) {
                    // currently only one intersection per bezier is supported.
                    // however intersection points may return multiple values that are all within the
                    // tolerance of the one intersection point. Thats why we're not currently throwing any exceptions.
                    //and instead just gets the middle-most and ignores the rest.

                    lineSplits.add(new Pair<>(i, intersectionPoints.get(intersectionPoints.size() / 2).first));
                    pathSplits.add(new Pair<>(j, intersectionPoints.get(intersectionPoints.size() / 2).second));
                }
                intersectionPoints.clear();
                ++j;
            }
            ++i;
        }

        if (lineSplits.size() != 2 || pathSplits.size() != 2) {
            return null;
        }

        BezierPath[] splitPath = splitBeizPath(path, pathSplits);
        BezierPath[] splitLine = splitBeizPath(line, lineSplits);

        BezierPathBuilder b = new BezierPathBuilder();
        b.addBeizPath(splitPath[2]);
        if (!b.fitAndAddBeizPath(splitPath[0])) {
            throw new RuntimeException("1: Bug in  split bezier Path...");
        }
        if (!b.fitAndAddBeizPath(splitLine[1])) {
            throw new RuntimeException("2: Bug in  split bezier Path...");
        }

        SplitReturn ret = new SplitReturn();
        ret.first = b.get(true);
        b.clear();
        b.addBeizPath(splitLine[1]);
        b.fitAndAddBeizPath(splitPath[1]);
        ret.second = b.get(true);
        ret.firstSplitPoint = splitLine[1].points[0];
        ret.secondSplitPoint = splitLine[1].points[splitLine[1].points.length - 1];
        return ret;
    }

    private static BezierPath[] splitBeizPath(BezierPath beizPath, List<Pair<Integer, Float>> poses) {

        Collections.sort(poses, new Comparator<Pair<Integer, Float>>() {
            @Override
            public int compare(Pair<Integer, Float> lhs, Pair<Integer, Float> rhs) {
                return (int) Math.signum(lhs.first - rhs.first);
            }
        });

        //does not yet support one beiz beeing split multiple times...
        List<BezierPath> tmp = new ArrayList<>();
        int currentIndex = 0;
        int i = 0;
        BezierPathBuilder builder = new BezierPathBuilder();

        for (Bezier currentBeiz : beizPath) {

            if (currentIndex < poses.size() && poses.get(currentIndex).first <= i) {
                Bezier[] split = currentBeiz.split(poses.get(currentIndex).second);
                builder.addBeiz(split[0]);
                tmp.add(builder.get(false));
                builder.clear();
                builder.addBeiz(split[1]);
                ++currentIndex;
            } else {
                builder.addBeiz(currentBeiz);
            }
            ++i;
        }

        tmp.add(builder.get(false));
        return tmp.toArray(new BezierPath[tmp.size()]);
    }


    /*
    */

    public boolean isClosed() {
        return points.length % 3 == 0;
    }

    @Override
    public Iterator<Bezier> iterator() {
        return new BeizIterator(this);
    }

    //somehow we're fucking up the ordering here.. I think... or we just have floating point errors above. Idk.
    //anyway sticking with the naive method for now.
    public Vector2[] approximatebezierPath(float precision) {
        Vector2[] path = points;
        List<Vector2> ret = new ArrayList<>();
        for (int i = 0; i < path.length - 2; i += 3) {
            Vector2[] beiz = new Vector2[]{path[i], path[i + 1], path[i + 2], path[(i + 3) % path.length]};
            ret.addAll(approximatebezier(beiz, precision));
        }
        return ret.toArray(new Vector2[ret.size()]);
    }

    public Vector2[] approximatebezierPathNaive(int precision) {
        Vector2[] verts = new Vector2[precision * ((points.length) / 3)];
        int counter = 0;
        for (Bezier beiz : this) {
            for (int i = 0; i < precision; i++) {
                verts[counter * precision + i] = beiz.getValue(i / (float) precision);
            }
            ++counter;
        }
        return verts;
    }

    private Vector2[] subdivide(Vector2[] path) {
        Vector2[] ret = new Vector2[path.length * 2];
        int i = 0;
        for (Bezier bezier : this) {
            Bezier[] divided = bezier.split(0.5f);
            ret[i * 2] = divided[0].points[0];
            ret[i * 2 + 1] = divided[0].points[1];
            ret[i * 2 + 2] = divided[0].points[2];

            ret[i * 2 + 3] = divided[1].points[0];
            ret[i * 2 + 4] = divided[1].points[1];
            ret[i * 2 + 5] = divided[1].points[2];
            ++i;
        }
        return ret;
    }

    public BezierPath reverse() {
        points = ArrayUtils.reverse(points);
        return this;
    }

    public static class SplitReturn {
        public BezierPath first;
        public BezierPath second;
        public Vector2 firstSplitPoint;
        public Vector2 secondSplitPoint;
    }

    public static class BeizIterator implements Iterator<Bezier> {
        final BezierPath path;
        public int index = 0;

        public BeizIterator(BezierPath path) {
            this.path = path;
        }

        @Override
        public boolean hasNext() {
            return index < path.points.length / 3;
        }

        @Override
        public Bezier next() {
            Bezier ret = new Bezier(path.points[index * 3], path.points[index * 3 + 1],
                    path.points[index * 3 + 2], path.points[(index * 3 + 3) % path.points.length]);
            ++index;
            return ret;
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove is not implemented");
        }
    }

}
