package com.sonsofhesslow.games.risk.graphics;

import android.util.Pair;

import com.sonsofhesslow.games.risk.graphics.geometry.Bezier;
import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.DashedBezierLine;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.FilledBezierPath;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SvgImporter {

    // it appears as though the java scanner cannot properly handle documents with long lines.
    // parsing floats that reach past some arbitrary limit of x characters breaks them up.
    // this is not acceptable beaviour.

    public static List<SVGReturnValue> read(InputStream svgStream, Renderer renderer) throws IOException {
        List<BezierPath> paths = new ArrayList<>();
        List<BezierPath> splits = new ArrayList<>();
        List<BezierPath> connections = new ArrayList<>();
        List<BezierPath> regionConnections = new ArrayList<>();
        List<BezierPath> continentConnections = new ArrayList<>();

        SvgReader sr = new SvgReader(svgStream);
        //parse all paths in the svg. add them into the appropriate category.
        while (true) {
            SVGPath newRead = sr.readPath();
            if (newRead != null) {
                BezierPath readBeiz = newRead.path;

                for (int i = 0; i < readBeiz.points.length; i++) {
                    readBeiz.points[i] = new Vector2(-readBeiz.points[i].x / 250, -readBeiz.points[i].y / 250);
                }

                if (readBeiz.isClosed()) {
                    paths.add(readBeiz);
                } else {
                    if (newRead.isDashed)
                        connections.add(readBeiz);
                    else if (newRead.isContinent) continentConnections.add(readBeiz);
                    else if (newRead.isRegion) regionConnections.add(readBeiz);
                    else splits.add(readBeiz);
                }
            } else {
                break;
            }
        }

        //split the paths with the splits.
        //keep track of which split split what
        List<Pair<BezierPath, Integer>> pathsWithInfo = new ArrayList<>(paths.size());

        int continentId = 0;
        for (BezierPath b : paths) {
            pathsWithInfo.add(new Pair<>(b, continentId++));
        }

        List<Vector2> splitPoints = new ArrayList<>();
        while (splits.size() > 0) {
            boolean removed = false;
            for (int i = 0; i < splits.size(); i++) {
                BezierPath split = splits.get(i);
                int pathLen = pathsWithInfo.size();
                for (int j = 0; j < pathLen; j++) {
                    Pair<BezierPath, Integer> pathWithInfo = pathsWithInfo.get(j);
                    BezierPath.SplitReturn newPaths = BezierPath.splitBeizPath(pathWithInfo.first, split);
                    if (newPaths != null) {
                        pathsWithInfo.remove(j);
                        --j;
                        --pathLen;
                        pathsWithInfo.add(new Pair<>(newPaths.first, pathWithInfo.second));
                        pathsWithInfo.add(new Pair<>(newPaths.second, pathWithInfo.second));
                        removed = true;
                        splitPoints.add(newPaths.secondSplitPoint);
                        splitPoints.add(newPaths.firstSplitPoint);
                    }
                }
                if (removed) {
                    splits.remove(i);
                    --i;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        List<SVGReturnValue> ret = new ArrayList<>(pathsWithInfo.size());
        int c = 0;
        for (Pair<BezierPath, Integer> p : pathsWithInfo) {
            // using a hashset here might be slow...
            ret.add(new SVGReturnValue(new FilledBezierPath(p.first, renderer), p.second, c, new HashSet<Integer>()));
            ++c;
        }

        //could still be sped up a bunch. probably should be as well. we'll see...
        for (Vector2 point : splitPoints) {
            List<Integer> neighbors = new ArrayList<>();
            int i = 0;
            for (SVGReturnValue val : ret) {
                for (Bezier b : val.path.path) {
                    if (b.isOnCurve(point, 0.1f)) {
                        neighbors.add(i);
                        break;
                    }
                }
                ++i;
            }
            for (Integer index : neighbors) {
                ret.get(index).neighbors.addAll(neighbors);
            }
        }
        int i;
        for (BezierPath conn : connections) {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length - 1];

            new DashedBezierLine(conn, renderer); //this should probably not be done from here..
            SVGReturnValue firstVal = null;
            int firstIndex = -1;

            SVGReturnValue secondVal = null;
            int secondIndex = -1;

            i = 0;
            for (SVGReturnValue val : ret) {
                if (val.path.fillMesh.isOnMesh2D(start)) {
                    firstVal = val;
                    firstIndex = i;
                }

                if (val.path.fillMesh.isOnMesh2D(end)) {
                    secondVal = val;
                    secondIndex = i;
                }
                ++i;
            }
            if (firstVal != null && secondVal != null) {
                firstVal.neighbors.add(secondIndex);
                secondVal.neighbors.add(firstIndex);
            } else {
                System.out.println("NULL CONNECTION FAILUER... :/ " + start + "," + end);
            }
        }

        // hadling the region fusion
        for (BezierPath conn : regionConnections) {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length - 1];
            SVGReturnValue firstVal = null;
            SVGReturnValue secondVal = null;

            i = 0;
            for (SVGReturnValue val : ret) {
                if (val.path.fillMesh.isOnMesh2D(start)) {
                    firstVal = val;
                }
                if (val.path.fillMesh.isOnMesh2D(end)) {
                    secondVal = val;
                }
                ++i;
            }

            if (firstVal != null && secondVal != null) {
                //notify that the objects no longer need rendering.
                firstVal.path.remove();
                secondVal.path.remove();
                //merge the objects
                firstVal.path.mergeWith(secondVal.path);
                //add toghether the neigbors
                firstVal.neighbors.addAll(secondVal.neighbors);
                ret.remove(secondVal);

                //notify that the modified shape needs to be drawn (and initialized);
                renderer.delayedInit(firstVal.path);
                //handle the removed neigbor ids
                for (SVGReturnValue val : ret) {
                    boolean contians = false;
                    for (Integer neig : val.neighbors) {
                        if (((int) neig) == secondVal.regionId) {
                            contians = true;
                        }
                    }
                    if (contians) {
                        val.neighbors.remove(secondVal.regionId);
                        val.neighbors.add(firstVal.regionId);
                    }
                }
            } else {
                System.out.println("NULL CONNECTION_REGION FAILUER... :/ " + start + "," + end);
            }
        }


        // hadling the continent fusion
        for (BezierPath conn : continentConnections) {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length - 1];
            int firstContinentId = -1;
            int secondContinentId = -1;

            i = 0;
            for (SVGReturnValue val : ret) {
                if (val.path.fillMesh.isOnMesh2D(start)) {
                    firstContinentId = val.continentId;
                }
                if (val.path.fillMesh.isOnMesh2D(end)) {
                    secondContinentId = val.continentId;
                }
                ++i;
            }
            if (firstContinentId != -1 && secondContinentId != -1) {
                for (SVGReturnValue val : ret) {
                    if (val.continentId == secondContinentId) val.continentId = firstContinentId;
                }
            } else {
                System.out.println("NULL CONNECTION_REGION FAILUER... :/ " + start + "," + end);
            }
        }


        //set the continent_ids & region_ids to the range 0-(n-1)
        //also make sure nobody has itself as a neighbor.
        List<Integer> regionIds = new ArrayList<>(ret.size());
        TreeSet<Integer> continentIds = new TreeSet<>();
        for (SVGReturnValue val : ret) {
            regionIds.add(val.regionId);
            continentIds.add(val.continentId);
        }
        Collections.sort(regionIds);

        i = 0;
        for (SVGReturnValue val : ret) {
            val.continentId = continentIds.headSet(val.continentId).size();
            val.regionId = regionIds.indexOf(val.regionId);
            Set<Integer> newNeighs = new HashSet<>();
            for (Integer neigh : val.neighbors) {
                int newNeigh = regionIds.indexOf(neigh);
                if (newNeigh != i)
                    newNeighs.add(newNeigh);
            }
            val.neighbors = newNeighs;
            ++i;
        }

        return ret;
    }

    public static class SVGReturnValue {

        public final FilledBezierPath path;
        public Integer continentId;
        public Integer regionId;
        public Set<Integer> neighbors;
        public SVGReturnValue(FilledBezierPath path, Integer continentId, Integer regionId, Set<Integer> neighbors) {
            this.path = path;
            this.continentId = continentId;
            this.neighbors = neighbors;
            this.regionId = regionId;
        }
    }


}




