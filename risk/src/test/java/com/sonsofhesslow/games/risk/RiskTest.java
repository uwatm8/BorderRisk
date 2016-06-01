package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import org.junit.Test;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import static org.junit.Assert.*;

public class RiskTest {
    @Test
    public void testPlayerCreation() {
        Risk risk = new Risk(new int[3], 42);
        assertNotNull(risk.getPlayers());
        assertEquals(3, risk.getPlayers().length);
        assertEquals(risk.getGamePhase(),Risk.GamePhase.PICK_TERRITORIES);
    }


    class observerTester implements Observer
    {
        public int calls = 0;
        public Object lastArg = null;
        public Observable lastObservable = null;

        @Override
        public void update(Observable observable, Object data) {
            ++calls;
            lastArg = data;
            lastObservable = observable;
        }
    }

    @Test
    public void observers() {
        observerTester tester = new observerTester();
        Risk risk = new Risk(new int[0], 0);
        risk.addObserver(tester);
        Territory t1 = new Territory(1);
        risk.setAttackingTerritory(t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).eventType, Risk.RiskChangeEvent.EventType.ATTACK);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).newTerritory, t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).oldTerritory, null);

        risk.setDefendingTerritory(t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).eventType, Risk.RiskChangeEvent.EventType.DEFENCE);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).newTerritory, t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).oldTerritory, null);

        risk.setSelectedTerritory(t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).eventType, Risk.RiskChangeEvent.EventType.SELECTED);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).newTerritory, t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).oldTerritory, null);

        risk.setSecondSelectedTerritory(t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).eventType, Risk.RiskChangeEvent.EventType.SECOND_SELECTED);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).newTerritory, t1);
        assertEquals(((Risk.RiskChangeEvent) tester.lastArg).oldTerritory, null);

        risk.setGamePhase(Risk.GamePhase.FIGHT);
        assertEquals(tester.lastArg, Risk.GamePhase.FIGHT);
        Player player = new Player(3);
        risk.setCurrentPlayer(player);
        assertEquals(tester.lastArg, player);
        assertEquals(tester.calls, 6);
    }

    @Test
    public void territoryTest()
    {
        //check no two territories has the same id.
        Risk risk = new Risk(new int[2],3);
        Set<Integer> set = new HashSet<>();
        for(Territory t : risk.getTerritories())
        {
            set.add(t.getId());
        }
        assertEquals(set.size(),risk.getTerritories().length);
    }

    @Test
    public void attackingTerritory()
    {
        Risk risk = new Risk(new int[2],3);
        Territory t = new Territory(3);
        risk.setAttackingTerritory(t);
        assertEquals(t, risk.getAttackingTerritory());
    }

    @Test
    public void defendingTerritory()
    {
        Risk risk = new Risk(new int[2],3);
        Territory t = new Territory(3);
        risk.setDefendingTerritory(t);
        assertEquals(t,risk.getDefendingTerritory());
    }

    @Test
    public void selectedTerritory()
    {
        Risk risk = new Risk(new int[2],3);
        Territory t = new Territory(3);
        risk.setSelectedTerritory(t);
        assertEquals(t, risk.getSelectedTerritory());
    }

    @Test
    public void secondSelectedTerritory()
    {
        Risk risk = new Risk(new int[2],3);
        Territory t = new Territory(3);
        risk.setSecondSelectedTerritory(t);
        assertEquals(t, risk.getSecondSelectedTerritory());
    }

    @Test
    public void neighbors()
    {
        Risk risk = new Risk(new int[2],2);
        assertNotNull(risk.getNeighbors());
    }

    @Test
    public void defenders()
    {
        Risk risk = new Risk(new int[2],2);
        assertNotNull(risk.getDefenders());
    }
}
