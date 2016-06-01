package com.sonsofhesslow.games.risk.model;

import java.util.ArrayList;
import java.util.Observable;

public class Risk extends Observable {

    private Player[] players;
    private Player currentPlayer;
    private static GamePhase gamePhase;
    private Territory[] territories;
    private Territory attackingTerritory;
    private Territory defendingTerritory;
    private Territory selectedTerritory;
    private Territory secondSelectedTerritory;
    private ArrayList<Territory> defenders = new ArrayList<>();
    private ArrayList<Territory> neighbors = new ArrayList<>();

    public enum GamePhase {PICK_TERRITORIES, PLACE_STARTING_ARMIES, PLACE_ARMIES, FIGHT, MOVEMENT}

    public Risk(int playerIds[], int territoryCount) {
        territories = new Territory[territoryCount];

        players = new Player[playerIds.length];
        for (int i = 0; i < playerIds.length; i++) {
            players[i] = new Player(playerIds[i]);
        }
        //create territory objects
        for (int i = 0; i < territoryCount; i++) {
            territories[i] = new Territory(i);
        }
        gamePhase = GamePhase.PICK_TERRITORIES;
    }

    public void setAttackingTerritory(Territory territory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.ATTACK, this, territory, this.attackingTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        attackingTerritory = territory;
    }

    public Territory getAttackingTerritory() {
        return attackingTerritory;
    }

    public void setDefendingTerritory(Territory territory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.DEFENCE, this, territory, this.defendingTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        defendingTerritory = territory;
    }

    public Territory getDefendingTerritory() {
        return defendingTerritory;
    }

    public void setCurrentPlayer(Player player) {
        System.out.println("current player: " + getCurrentPlayer() + " new player: " + player);
        currentPlayer = player;

        setChanged();
        notifyObservers(player);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Territory[] getTerritories() {
        return territories;
    }

    public Player[] getPlayers() {
        return players;
    }

    public ArrayList getNeighbors() {
        return neighbors;
    }

    public ArrayList getDefenders() {
        return defenders;
    }

    public Territory getSelectedTerritory() {
        return selectedTerritory;
    }

    public void setSelectedTerritory(Territory touchedTerritory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.SELECTED, this, touchedTerritory, this.selectedTerritory);

        selectedTerritory = touchedTerritory;
        setChanged();
        notifyObservers(riskChangeEvent);
    }

    public void setSecondSelectedTerritory(Territory touchedTerritory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.SECOND_SELECTED, this, touchedTerritory, this.secondSelectedTerritory);

        secondSelectedTerritory = touchedTerritory;
        setChanged();
        notifyObservers(riskChangeEvent);
    }

    public Territory getSecondSelectedTerritory() {
        return secondSelectedTerritory;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase phase) {
        this.gamePhase = phase;
        setChanged();
        notifyObservers(phase);
    }

    public void placeEvent() {
        setChanged();
        notifyObservers();
    }

    /*
    Listeners
     */
    public static class RiskChangeEvent {
        public enum EventType {ATTACK, DEFENCE, SELECTED, SECOND_SELECTED}

        public EventType eventType;
        Risk risk;
        public Territory newTerritory;
        public Territory oldTerritory;

        public RiskChangeEvent(EventType eventType, Risk risk, Territory newTerritory, Territory oldTerritory) {
            this.eventType = eventType;
            this.risk = risk;
            this.newTerritory = newTerritory;
            this.oldTerritory = oldTerritory;
        }
    }
}
