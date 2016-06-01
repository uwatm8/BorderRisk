package com.sonsofhesslow.games.risk.graphics;

import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;

public class SVGPath {
    final BezierPath path;
    final boolean isDashed;
    final boolean isRegion;
    final boolean isContinent;
    public SVGPath(BezierPath path, boolean isDashed, boolean isRegion, boolean isContinent) {
        this.path = path;
        this.isDashed = isDashed;
        this.isRegion = isRegion;
        this.isContinent = isContinent;
    }
}
