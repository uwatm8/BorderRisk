package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Player;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {
    @Test
    public void testPlayerCreation() throws Exception {
        Player player = new Player(0);
        assertNotNull(player);
    }

    @Test
    public void testGetCard() throws Exception {
        Player player = new Player(0);
        player.giveOneCard();
        assertNotNull(player.getCards());
        assertEquals(1, player.getCards().size());
    }

    @Test
    public void giveTroops() throws Exception {
        Player player = new Player(0);
        player.giveArmies(4);
        player.decArmiesToPlace();
        assertEquals(3, player.getArmiesToPlace());
    }

    @Test
    public void decTroops() throws Exception {
        Player player = new Player(0);
        player.giveArmies(4);
        player.decArmiesToPlace(2);
        assertEquals(2, player.getArmiesToPlace());
    }

    @Test
    public void participantId() throws Exception {
        Player player = new Player(2);
        assertEquals(2, player.getParticipantId());
    }

    @Test
    public void testName() throws Exception {
        Player player = new Player(2);
        player.setName("abc");
        assertEquals("abc", player.getName());
    }

    @Test
    public void alive()
    {
        Player player = new Player(2);
        assertEquals(true, player.isAlive());
        player.setAlive(false);
        assertEquals(false, player.isAlive());
        player.setAlive(true);
        assertEquals(true, player.isAlive());
    }


}
