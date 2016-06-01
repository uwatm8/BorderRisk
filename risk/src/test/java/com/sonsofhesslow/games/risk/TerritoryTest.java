package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Continent;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Territory;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class TerritoryTest {
    @Test
    public void testNeighbour() throws Exception {
        Territory territory1 = new Territory(12);
        Territory territory2 = new Territory(13);
        Territory territory3 = new Territory(14);
        Territory[] territory1Neighbours = new Territory[1];
        territory1Neighbours[0] = territory2;
        territory1.setNeighbours(territory1Neighbours);

        assertTrue(territory1.isNeighbour(territory2));
        assertFalse(territory1.isNeighbour(territory3));
    }

    @Test
    public void testOccupier() throws Exception {
        Territory territory = new Territory(12);
        Player p1 = new Player(1);
        territory.setOccupier(p1);
        Player p2 = new Player(2);
        territory.setOccupier(p2);
        assertEquals(p1.getTerritoriesOwned(), 0);
        assertEquals(p2.getTerritoriesOwned(), 1);
    }
    @Test
    public void id() throws Exception
    {
        Territory territory = new Territory(3);
        assertEquals(territory.getId(), 3);
        territory.setId(4);
        assertEquals(territory.getId(), 4);
    }

    @Test
    public void continent() throws Exception
    {
        Territory territory = new Territory(4);
        assertEquals(territory.getContinent(), null);
        territory.setContinent(Continent.NORTH_AMERICA);
        assertEquals(territory.getContinent(), Continent.NORTH_AMERICA);
        territory.setContinent(0);
        assertEquals(territory.getContinent(), Continent.OCEANIA);
    }

    @Test
    public void JustMoved() throws Exception
    {
        Territory territory = new Territory(4);
        territory.setJustMovedArmies(10);
        assertEquals(territory.getJustMovedArmies(), 10);
    }



}
