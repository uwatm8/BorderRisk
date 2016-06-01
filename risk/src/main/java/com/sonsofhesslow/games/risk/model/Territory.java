package com.sonsofhesslow.games.risk.model;

import java.util.Observable;

public class Territory extends Observable {
    private int armyCount = 0;
    private int justMovedArmies = 0;
    private Player occupier;
    private Continent continent;
    private int id;
    private Territory[] neighbours;

    public Territory(int id) {
        this.id = id;
        setArmyCount(1);
    }

    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        setChanged();
        notifyObservers(armyCount);

        this.armyCount = armyCount;
    }

    public Player getOccupier() {
        return occupier;
    }

    public void setOccupier(Player occupier) {
        if (occupier != this.occupier) {
            setChanged();
            notifyObservers(occupier);
        }
        if (this.occupier != null) {
            this.occupier.changeTerritoriesOwned(-1);
        }
        occupier.changeTerritoriesOwned(1);
        this.occupier = occupier;
    }

    public void changeArmyCount(int change) {
        setArmyCount(armyCount + change);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Territory[] getNeighbours() {
        return neighbours;
    }

    public boolean isNeighbour(Territory territory) {
        for (Territory neighbour : this.getNeighbours()) {
            if (neighbour == territory) return true;
        }
        return false;
    }

    public void setNeighbours(Territory[] neighbours) {
        this.neighbours = neighbours;
    }

    public void setContinent(int continentId) {
        continent = Continent.values()[continentId];
    }

    public int getJustMovedArmies() {
        return justMovedArmies;
    }

    public void setJustMovedArmies(int justMovedArmies) {
        this.justMovedArmies = justMovedArmies;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public Continent getContinent() {
        return continent;
    }
}
