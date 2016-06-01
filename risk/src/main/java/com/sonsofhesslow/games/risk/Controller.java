package com.sonsofhesslow.games.risk;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.sonsofhesslow.games.risk.graphics.GLTouchEvent;
import com.sonsofhesslow.games.risk.graphics.GLTouchListener;
import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.model.Card;
import com.sonsofhesslow.games.risk.model.Continent;
import com.sonsofhesslow.games.risk.model.Die;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;
import com.sonsofhesslow.games.risk.network.NetworkChangeEvent;
import com.sonsofhesslow.games.risk.network.NetworkListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Controller implements GLTouchListener, NetworkListener {
    private static final int TERRITORIES_IN_ASIA = 12;
    private static final int TERRITORIES_IN_NORTH_AMERICA = 9;
    private static final int TERRITORIES_IN_EUROPE = 7;
    private static final int TERRITORIES_IN_AFRICA = 6;
    private static final int TERRITORIES_IN_OCEANIA = 4;
    private static final int TERRITORIES_IN_SOUTH_AMERICA = 4;

    private static final int EXTRA_TROOPS_ASIA = 7;
    private static final int EXTRA_TROOPS_NORTH_AMERICA = 5;
    private static final int EXTRA_TROOPS_EUROPE = 5;
    private static final int EXTRA_TROOPS_AFRICA = 3;
    private static final int EXTRA_TROOPS_OCEANIA = 2;
    private static final int EXTRA_TROOPS_SOUTH_AMERICA = 2;

    private static Risk riskModel;

    private int currentPlayerIndex = 0; //used to set next player
    private int selfId;
    private boolean territoryTaken = false;
    private Overlay overlayController;
    private ArrayList<Territory> movementChangedTerritories = new ArrayList<>();
    private View riskView = null;

    //to prevent adding multiple wait screens
    private boolean activeWaitScreen = false;

    //to prevent loop when notifying others
    private boolean hasNotifiedWin = false;

    public Controller(int[] playerIds, Overlay overlayController, Resources resources) {
        this.selfId = 0;
        this.overlayController = overlayController;
        int territoryCount = GraphicsManager.getInstance().getNumberOfTerritories();
        riskModel = new Risk(playerIds, territoryCount); //somehow set number of players (2)

        riskModel.setCurrentPlayer(riskModel.getPlayers()[0]);

        riskView = new View(riskModel, overlayController, resources);

        //add observers for view
        riskModel.addObserver(riskView);
        for (Territory territory : riskModel.getTerritories()) {
            territory.addObserver(riskView);
        }
        for (Player player : riskModel.getPlayers()) {
            player.addObserver(riskView);
        }

        //set neighbours and continent
        for (int i = 0; i < territoryCount; i++) {
            Integer[] ids = GraphicsManager.getInstance().getNeighbours(i);
            int number = ids.length; //number of neighbours
            Territory[] neighbours = new Territory[number];

            //set neighbours
            for (int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }
            riskModel.getTerritories()[i].setNeighbours(neighbours);

            //set continent
            riskModel.getTerritories()[i].setContinent(GraphicsManager.getInstance().getContinetId(i));
        }

        if (!isOnline()) {
            //if it's an online game this will be done later, after selfId is set
            setStartingArmies();
        }
    }

    @Override
    public void handleNetworkChange(NetworkChangeEvent event) {
        System.out.println("network change");
        refreshGamePhase();

        switch (event.action) {
            case armyAmountChange: {
                Territory changedTerritory = Controller.getTerritoryById(event.getRegionId());
                GraphicsManager.getInstance().moveCameraTowardsTerritory(changedTerritory.getId());
                if (changedTerritory != null) {
                    changedTerritory.setArmyCount(event.getArmies());
                }
            }
            break;
            case occupierChange: {
                Territory changedTerritory = Controller.getTerritoryById(event.getRegionId());
                Player newOccupier = null;
                GraphicsManager.getInstance().moveCameraTowardsTerritory(changedTerritory.getId());
                for (Player p : Controller.getRiskModel().getPlayers()) {
                    if (p.getParticipantId() == event.getParticipantId()) {
                        newOccupier = p;
                        break;
                    }
                }
                if (changedTerritory != null) {
                    changedTerritory.setOccupier(newOccupier);
                }
            }
            break;
            case turnChange: {
                nextPlayer();
            }
            break;
        }
        GraphicsManager.getInstance().requestRender();
    }

    public void handle(GLTouchEvent event) {
        if (event.touchedRegion) {
            Territory touchedTerritory = getTerritoryById(event.regionId);
            System.out.println(touchedTerritory.getContinent());
            if (selfId == riskModel.getPlayers()[currentPlayerIndex].getParticipantId()) {
                switch (riskModel.getGamePhase()) {
                    case PICK_TERRITORIES:
                        if (touchedTerritory.getOccupier() == null) {
                            touchedTerritory.setOccupier(riskModel.getCurrentPlayer());


                            final int EXTRA_TRIES;
                            //for debugging only (picks more territories at once)
                            if(isOnline()) {
                                EXTRA_TRIES = 0;
                            } else {
                                //for presentation
                                EXTRA_TRIES = 0;
                            }

                            Random r = new Random();
                            for (int i = 0; i < EXTRA_TRIES; i++) {
                                int randomNumber = r.nextInt(42);
                                Territory randomTerritory = getTerritoryById(randomNumber);
                                if (randomTerritory.getOccupier() == null) {
                                    randomTerritory.setArmyCount(1);
                                    randomTerritory.setOccupier(riskModel.getCurrentPlayer());
                                    riskModel.getCurrentPlayer().decArmiesToPlace();
                                } else{
                                    int territoriesOccupied = 0;
                                    for (Territory territory : riskModel.getTerritories()) {
                                        if (territory.getOccupier() != null) {
                                            territoriesOccupied++;
                                        }
                                    }
                                    //if >20 prevent freeze
                                    if (territoriesOccupied != 42) {
                                        i--;    //find a new territory to place
                                    }
                                }
                            }

                            touchedTerritory.setArmyCount(1);
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                            // TODO: 2016-05-16 better solution?
                            boolean canContinueToPlacePhase = true;
                            for (Territory territory : riskModel.getTerritories()) {
                                if (territory.getOccupier() == null) {
                                    canContinueToPlacePhase = false;        //one territory with no occupier found
                                    break;
                                }
                            }

                            if (canContinueToPlacePhase) {
                                riskModel.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                            }
                            nextPlayer();
                        }
                        break;

                    case PLACE_STARTING_ARMIES:
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            touchedTerritory.changeArmyCount(1);
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                            if (selfId != 0 && riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                                //multiplayer
                                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                            } else {
                                boolean playerHasArmiesLeft = false;
                                //skipping phase if player
                                for (Player player : riskModel.getPlayers()) {
                                    if (player.getArmiesToPlace() > 0) {
                                        playerHasArmiesLeft = true;
                                        break;
                                    }
                                }
                                if (!playerHasArmiesLeft) {
                                    riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                                }
                            }
                            nextPlayer();
                        }
                        break;

                    case PLACE_ARMIES:
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            // TODO: 2016-05-26 armies are placed with a slider, triggered by listener
                            riskModel.setSelectedTerritory(touchedTerritory);
                        }
                        break;

                    case FIGHT:
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()
                                && touchedTerritory.getArmyCount() > 1) {
                            //clear old possible defenders
                            riskModel.getDefenders().clear();
                            //checks if any neighboring territory can be attacked
                            for (Territory neighbour : touchedTerritory.getNeighbours()) {
                                if (neighbour.getOccupier() != riskModel.getCurrentPlayer()) {
                                    riskModel.getDefenders().add(neighbour);
                                    riskModel.setAttackingTerritory(touchedTerritory);
                                    riskModel.setDefendingTerritory(null);
                                }
                            }
                        } else if (riskModel.getDefenders().contains(touchedTerritory)
                                && riskModel.getAttackingTerritory() != null) {
                            riskModel.setDefendingTerritory(touchedTerritory);
                            //TODO show attack button
                        }

                        break;

                    case MOVEMENT:
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()
                                && touchedTerritory.getArmyCount() > 1
                                && riskModel.getSelectedTerritory() == null) {
                            //clear old possible defenders
                            riskModel.getNeighbors().clear();
                            //checks if any neighboring territory can be attacked
                            riskModel.setSelectedTerritory(touchedTerritory);
                            for (Territory neighbour : touchedTerritory.getNeighbours()) {
                                if (neighbour.getOccupier() == riskModel.getCurrentPlayer()) {
                                    riskModel.getNeighbors().add(neighbour);
                                    riskModel.setSecondSelectedTerritory(null);
                                }
                            }
                        } else if (riskModel.getNeighbors().contains(touchedTerritory)
                                && riskModel.getSelectedTerritory() != null) {
                            riskModel.setSecondSelectedTerritory(touchedTerritory);
                            //TODO show attack button
                        }
                        break;
                }
            }
        }
    }

    public void fightButtonPressed() {
        Die.fight(riskModel.getAttackingTerritory(), riskModel.getDefendingTerritory());
        if (riskModel.getDefendingTerritory().getOccupier() == riskModel.getCurrentPlayer()) {
            territoryTaken = true;
            riskModel.getAttackingTerritory().changeArmyCount(-1);
            riskModel.getDefendingTerritory().changeArmyCount(+1);
            riskModel.setSelectedTerritory(riskModel.getAttackingTerritory());
            riskModel.setSecondSelectedTerritory(riskModel.getDefendingTerritory());
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
        } else if (riskModel.getAttackingTerritory().getArmyCount() < 2) {
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
            riskModel.setGamePhase(Risk.GamePhase.FIGHT);
        }

        //check if player won
        if(getNewPlayerIndex() == currentPlayerIndex) {
            //previous player won
            playerWon(riskModel.getPlayers()[currentPlayerIndex]);
        }

        GraphicsManager.getInstance().requestRender();
    }

    public void nextTurn() {
        if (riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT) {
            riskModel.setSelectedTerritory(null);
            riskModel.setSecondSelectedTerritory(null);
            refreshMovementChangedTerritories();
            nextPlayer();
            riskModel.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
        }
        if (riskModel.getGamePhase() == Risk.GamePhase.FIGHT) {
            if (playerCanMove(riskModel.getCurrentPlayer())) {
                riskModel.setGamePhase(Risk.GamePhase.MOVEMENT);
            } else {
                nextPlayer();
                riskModel.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
                // TODO: 2016-05-24 notify player
            }

            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
            riskModel.setSelectedTerritory(null);
        }
    }

    private boolean playerCanMove(Player player) {
        ArrayList<Territory> playersTerritories = new ArrayList<>();
        for (Territory territory : riskModel.getTerritories()) {
            if (territory.getOccupier().equals(player)) {
                playersTerritories.add(territory);
            }
        }

        for (Territory territory : playersTerritories) {
            int armiesToMove = territory.getArmyCount() - territory.getJustMovedArmies();
            if (armiesToMove > 1) {
                //can move
                return true;
            }
        }

        //can move, there is no one territory with more than 1 army to move
        return false;
    }

    public void nextPlayer() {
        int newPlayerIndex = getNewPlayerIndex();

        if(riskModel.getCurrentPlayer().getCards().size() == 5){
            ArrayList<Card> cards = new ArrayList<Card>();
            cards.addAll(riskModel.getCurrentPlayer().getCards());
            Card.handInSet(cards);
            riskModel.getCurrentPlayer().giveArmies(Card.cardAmountToGet());
            riskModel.getCurrentPlayer().setCards(cards);
        }
        if(newPlayerIndex == currentPlayerIndex) {
            //previous player won
            playerWon(riskModel.getPlayers()[currentPlayerIndex]);
        }

        //set next player
        currentPlayerIndex = newPlayerIndex;

        //gives armies for placement phase
        if (riskModel.getGamePhase() != Risk.GamePhase.PICK_TERRITORIES && riskModel.getGamePhase() != Risk.GamePhase.PLACE_STARTING_ARMIES) {
            setArmiesToPlace(riskModel.getPlayers()[currentPlayerIndex]);
        }

        if(territoryTaken) {
            riskModel.getCurrentPlayer().giveOneCard();
            territoryTaken = false;
        }

        //next player
        riskModel.setCurrentPlayer(riskModel.getPlayers()[currentPlayerIndex]);

        handleWaitingScreen();
    }

    public int getNewPlayerIndex() {
        int playerSearchIndex = currentPlayerIndex;

        boolean nextPlayerIndexFound = false;

        while (!nextPlayerIndexFound) {
            playerSearchIndex++;
            if (playerSearchIndex == riskModel.getPlayers().length) {
                playerSearchIndex = 0;
            }

            Player playerToTest = riskModel.getPlayers()[playerSearchIndex];

            //check if player is alive
            if (riskModel.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {
                nextPlayerIndexFound = true;
            } else if (playerToTest.isAlive()) {
                for (Territory territory : riskModel.getTerritories()) {
                    if (territory.getOccupier() == playerToTest) {
                        //player is occupier of atleast one territory, is alive
                        nextPlayerIndexFound = true;
                        break;
                    }
                }
                if (!nextPlayerIndexFound) {
                    //player is no longer alive
                    riskModel.getPlayers()[playerSearchIndex].setAlive(false);
                }
            }
        }
        return playerSearchIndex;
    }

    private void playerWon(Player player) {
        overlayController.addView(R.layout.activity_won);
        overlayController.hideBottom();
        //to notify the rest of the players, hasnotified to prevent loop
        if(isOnline() && !hasNotifiedWin){
            hasNotifiedWin = true;
            nextPlayer();
        }
    }

    @Nullable
    public static Territory getTerritoryById(int id) {
        for (Territory territory : riskModel.getTerritories()) {
            if (territory.getId() == id) {
                return territory;
            }
        }
        return null;
    }

    private void setStartingArmies() {
        //rules from hasbro
        if (isOnline()) {
            for (Player player : riskModel.getPlayers()) {
                if (player.getParticipantId() == selfId) {
                    player.giveArmies(calculateStartingArmies());
                }
            }
        } else {
            //singleplayer
            for (Player player : riskModel.getPlayers()) {
                player.giveArmies(calculateStartingArmies());
            }
        }
    }
    public int calculateStartingArmies() {
        return (50 - (5 * riskModel.getPlayers().length));
    }
    public int calculateStartingArmiesPresentation() {
        //for use when presenting
        return 26;
    }

    public void setArmiesToPlace(Player player) {
        int armies = Math.max(player.getTerritoriesOwned() / 3, 3);

        int[] foundInContinet = new int[6];
        for (Territory territory : riskModel.getTerritories()) {
            if (territory.getOccupier().equals(player))
                ++foundInContinet[territory.getContinent().ordinal()];
        }

        //if owning a whole continent, add corresponding  armies amounts:
        if (foundInContinet[Continent.ASIA.ordinal()] == TERRITORIES_IN_ASIA) {
            armies += EXTRA_TROOPS_ASIA;
        }
        if (foundInContinet[Continent.NORTH_AMERICA.ordinal()] == TERRITORIES_IN_NORTH_AMERICA) {
            armies += EXTRA_TROOPS_NORTH_AMERICA;
        }
        if (foundInContinet[Continent.EUROPE.ordinal()] == TERRITORIES_IN_EUROPE) {
            armies += EXTRA_TROOPS_EUROPE;
        }
        if (foundInContinet[Continent.AFRICA.ordinal()] == TERRITORIES_IN_AFRICA) {
            armies += EXTRA_TROOPS_AFRICA;
        }
        if (foundInContinet[Continent.OCEANIA.ordinal()] == TERRITORIES_IN_OCEANIA) {
            armies += EXTRA_TROOPS_OCEANIA;
        }
        if (foundInContinet[Continent.SOUTH_AMERICA.ordinal()] == TERRITORIES_IN_SOUTH_AMERICA) {
            armies += EXTRA_TROOPS_SOUTH_AMERICA;
        }

        player.giveArmies(armies);
    }

    public void placeButtonPressed(int seekBarValue) {
        if (riskModel.getGamePhase() == Risk.GamePhase.PLACE_ARMIES
                && riskModel.getSelectedTerritory() != null) {
            Territory territory = riskModel.getSelectedTerritory();
            territory.changeArmyCount(seekBarValue);
            riskModel.getCurrentPlayer().decArmiesToPlace(seekBarValue);
            if (riskModel.getCurrentPlayer().getArmiesToPlace() < 1
                    && riskModel.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                riskModel.setSelectedTerritory(null);
            }
            riskModel.placeEvent();
        } else if ((riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT
                || riskModel.getGamePhase() == Risk.GamePhase.FIGHT)
                && riskModel.getSelectedTerritory() != null
                && riskModel.getSecondSelectedTerritory() != null) {
            Territory from = riskModel.getSelectedTerritory();
            Territory to = riskModel.getSecondSelectedTerritory();
            to.changeArmyCount(seekBarValue);
            //to prevent multiple movements for troops (each troop should only be able to move 1 step)
            if (riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                to.setJustMovedArmies(seekBarValue);
                movementChangedTerritories.add(to);
            }
            from.changeArmyCount(-seekBarValue);
            riskModel.placeEvent();
            if (from.getArmyCount() - 1 == 0 || from.getArmyCount() - from.getJustMovedArmies() == 0) {
                if (riskModel.getGamePhase() == Risk.GamePhase.FIGHT) {
                    riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                } else {
                    riskModel.setGamePhase(Risk.GamePhase.MOVEMENT);
                }
                riskModel.setSelectedTerritory(null);
                riskModel.setSecondSelectedTerritory(null);
            }
            if (riskModel.getGamePhase() == Risk.GamePhase.FIGHT) {
                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                riskModel.setSelectedTerritory(null);
                riskModel.setSecondSelectedTerritory(null);
            }
        }
        GraphicsManager.getInstance().requestRender();
    }

    public void doneButtonPressed() {
        riskModel.setSelectedTerritory(null);
        riskModel.setSecondSelectedTerritory(null);
    }

    public void refreshGamePhase() {
        if (riskModel.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {
            boolean canContinueToPlacePhase = true;
            for (Territory territory : riskModel.getTerritories()) {
                if (territory.getOccupier() == null) {
                    canContinueToPlacePhase = false;        //one territory with no occupier found
                    break;
                }
            }

            if (canContinueToPlacePhase) {
                riskModel.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
            }
        }
    }

    public static Risk getRiskModel() {
        return riskModel;
    }

    private void refreshMovementChangedTerritories() {
        for (Territory changedTerritory : movementChangedTerritories) {
            changedTerritory.setJustMovedArmies(0);
        }
    }

    public void setSelfId(int selfId) {
        this.selfId = selfId;

        //give initial armies
        setStartingArmies();
    }

    public boolean isOnline() {
        return riskModel.getPlayers()[0].getParticipantId() != riskModel.getPlayers()[1].getParticipantId();
    }

    public void turnInCards(ArrayList<Integer> selectedCards) {
        Collections.sort(selectedCards);
        ArrayList<Card> temp = new ArrayList<Card>();
        temp.addAll(riskModel.getCurrentPlayer().getCards());
        temp.remove(selectedCards.get(2).intValue());
        temp.remove(selectedCards.get(1).intValue());
        temp.remove(selectedCards.get(0).intValue());
        riskModel.getCurrentPlayer().setCards(temp);
        riskModel.getCurrentPlayer().giveArmies(Card.cardAmountToGet());
    }

    public void handleWaitingScreen() {
        if (isOnline() && riskModel.getCurrentPlayer().getParticipantId() != selfId) {
            //multiplayer & not users turn
            if(!activeWaitScreen) {
                overlayController.addView(R.layout.activity_wait);
                overlayController.setWaitingVisible(true);
                activeWaitScreen = true;
            }
        } else {
            if(activeWaitScreen){
                activeWaitScreen = false;
                overlayController.removeView(R.layout.activity_wait);
                overlayController.setWaitingVisible(false);
            }
        }
        GraphicsManager.getInstance().requestRender();
    }
}
