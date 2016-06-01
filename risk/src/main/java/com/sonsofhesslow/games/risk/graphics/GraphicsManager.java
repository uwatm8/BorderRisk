package com.sonsofhesslow.games.risk.graphics;

import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Pair;

import com.sonsofhesslow.games.risk.R;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.FilledBezierPath;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Number;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.NumberedArrow;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Renderer;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Updatable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GraphicsManager {

    static GraphicsManager instance;
    private final ConcurrentLinkedQueue<Updatable> updatables = new ConcurrentLinkedQueue<>();
    private final Map<Pair<Integer, Integer>, NumberedArrow> arrows = new HashMap<>();
    FilledBezierPath[] beziers;
    private Integer[][] beizNeighbors;
    private Integer[] beizContinents;
    private Number[] numbers;
    private Renderer renderer;
    RiskGLSurfaceView surfaceView;
    public static GraphicsManager getInstance() {
        if (instance == null) instance = new GraphicsManager(); //@multithread unsafe
        return instance;
    }

    public void init(Resources resources, Renderer renderer, final RiskGLSurfaceView surfaceView) {
        this.renderer = renderer;
        this.surfaceView = surfaceView;

        Number.invalidateGLMemory();

        if(numbers != null) {
            for(Number number : numbers) {
                number.setValue(-1);
            }
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            long last;

            @Override
            public void run() {
                long current = SystemClock.elapsedRealtime();
                float dt = current - last;
                boolean reqRender = false;
                for (Updatable updatable : updatables) {
                    if (updatable.update(dt)) reqRender = true;
                }
                if (reqRender) surfaceView.requestRender();
                handler.postDelayed(this, 16);
                last = current;
            }
        }, 16);

        try {
            List<SvgImporter.SVGReturnValue> tmp = SvgImporter.read(resources.openRawResource(R.raw.new_world), renderer);
            beziers = new FilledBezierPath[tmp.size()];
            beizNeighbors = new Integer[tmp.size()][];
            beizContinents = new Integer[tmp.size()];
            int c = 0;
            for (SvgImporter.SVGReturnValue ret : tmp) {
                beziers[c] = ret.path;
                beizNeighbors[c] = ret.neighbors.toArray(new Integer[ret.neighbors.size()]);
                beizContinents[c] = ret.continentId;
                updatables.add(ret.path);
                ++c;
            }

            numbers = new Number[tmp.size()];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = new Number(-1, renderer);
                numbers[i].setPos(beziers[i].getCenter());
                numbers[i].drawOrder = 1000;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
        updatables.add(Camera.getInstance());
    }

    public void setHeight(int regionId, float height) {
        beziers[regionId].setPos(new Vector3(0, 0, -height));
    }
    public void moveCameraTowardsTerritory(int territoyId)
    {
        Camera.getInstance().moveCameraTowards(beziers[territoyId].getCenter());
    }

    public void addArrow(int territoryIdFrom, int territoyIdTo, int value, float[] color) {
        if(arrows.containsKey(new Pair<>(territoryIdFrom,territoyIdTo)))
        {
            arrows.get(new Pair<>(territoryIdFrom,territoyIdTo)).remove();
        }
        arrows.put(new Pair<>(territoryIdFrom, territoyIdTo),
                new NumberedArrow(renderer, beziers[territoryIdFrom].getCenter(),
                        beziers[territoyIdTo].getCenter(), color, value));
    }

    public void removeArrow(int territoryIdFrom, int territoyIdTo) {
        Iterator<Map.Entry<Pair<Integer, Integer>, NumberedArrow>> iter = arrows.entrySet().iterator();
        while (iter.hasNext())  // because foreach loops can't handle them removes...
        {
            Map.Entry<Pair<Integer, Integer>, NumberedArrow> entry = iter.next();
            if (entry.getKey().first.equals(territoryIdFrom) && entry.getKey().second.equals(territoyIdTo)) {
                entry.getValue().remove();
                iter.remove();
            }
        }
    }
    public void removeAllArrows()
    {
        Iterator<Map.Entry<Pair<Integer, Integer>, NumberedArrow>> iter = arrows.entrySet().iterator();
        while (iter.hasNext())  // because foreach loops can't handle them removes...
        {
            Map.Entry<Pair<Integer, Integer>, NumberedArrow> entry = iter.next();
            entry.getValue().remove();
            iter.remove();
        }
    }

    public void setColor(int regionId, float[] Color) {
        beziers[regionId].setColor(Color);
    }

    public void setColor(int regionId, float[] Color, int originId) {
        beziers[regionId].setColor(Color, beziers[originId].getCenter());
    }

    public void setOutlineColor(int regionId, float[] Color) {
        beziers[regionId].setColorOutline(Color);
    }

    public void setArmies(int regionId, int numberOfArmies) {
        numbers[regionId].setValue(numberOfArmies);
    }

    public Integer[] getNeighbours(int regionId) {
        return beizNeighbors[regionId];
    }

    public Integer getContinetId(int regionId) {
        return beizContinents[regionId];
    }

    public void setArmyColor(int regionId, float[] color) {
        numbers[regionId].setColor(color);
    }

    public Integer[] getContinentRegions(int continentId) {
        List<Integer> regionsInContinent = new ArrayList<>();
        int c = 0;

        for (Integer i : beizContinents) {
            if (continentId == i) regionsInContinent.add(c);
            ++c;
        }
        return regionsInContinent.toArray(new Integer[regionsInContinent.size()]);
    }

    public int getNumberOfTerritories() {
        return beziers.length;
    }

    public void requestRender() {
        surfaceView.requestRender();
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
}























