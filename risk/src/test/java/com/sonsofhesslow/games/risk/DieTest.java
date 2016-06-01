package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Die;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Territory;

import org.junit.Test;

import static org.junit.Assert.*;

public class DieTest {
    @Test
    public void fight() throws Exception {
        Territory attackingTerritory = new Territory(0);
        Territory defendingTerritory = new Territory(1);
        attackingTerritory.setArmyCount(3);
        defendingTerritory.setArmyCount(2);
        attackingTerritory.setOccupier(new Player(0));
        defendingTerritory.setOccupier(new Player(0));
        int oldDefending = 0;
        int oldAttacking = 0;
        int numberKilledTroops = 0;

        while (attackingTerritory.getArmyCount() > 1 && defendingTerritory.getArmyCount() > 0) {
            oldAttacking = attackingTerritory.getArmyCount();
            oldDefending = defendingTerritory.getArmyCount();
            Die.fight(attackingTerritory, defendingTerritory);
            numberKilledTroops += (oldAttacking - attackingTerritory.getArmyCount()) + (oldDefending - defendingTerritory.getArmyCount());

            assertEquals(5-numberKilledTroops, attackingTerritory.getArmyCount()+defendingTerritory.getArmyCount());
        }
        /*if (numberWins >= 2) {
            assertEquals(true, attackingTerritory.getOccupier() == defendingTerritory.getOccupier());
            assertEquals(true, attackingTerritory.getArmyCount() > 0);
            assertEquals(false, defendingTerritory.getArmyCount() > 0);
        } else {
            assertEquals(false, attackingTerritory.getOccupier() == defendingTerritory.getOccupier());
            assertEquals(true, attackingTerritory.getArmyCount() == 1);
            assertEquals(true, defendingTerritory.getArmyCount() > 0);
        }*/
    }
    @Test
    public void newOccupier() throws Exception {
        Territory attackingTerritory = new Territory(0);
        Territory defendingTerritory = new Territory(1);
        attackingTerritory.setArmyCount(1000);
        defendingTerritory.setArmyCount(1);
        attackingTerritory.setOccupier(new Player(0));
        defendingTerritory.setOccupier(new Player(0));
        Die.fightCompletely(attackingTerritory, defendingTerritory);
        assertEquals(attackingTerritory.getOccupier(),defendingTerritory.getOccupier());
    }

    @Test
    public void fightCompletely() throws Exception {
        for(int i = 0 ; i<20;i++)
        {
            Territory attackingTerritory = new Territory(0);
            Territory defendingTerritory = new Territory(1);
            attackingTerritory.setArmyCount(50);
            defendingTerritory.setArmyCount(50);
            attackingTerritory.setOccupier(new Player(0));
            defendingTerritory.setOccupier(new Player(0));
            Die.fightCompletely(attackingTerritory, defendingTerritory);
            assertTrue(attackingTerritory.getArmyCount()==1||defendingTerritory.getArmyCount()==0);
        }
    }


}
