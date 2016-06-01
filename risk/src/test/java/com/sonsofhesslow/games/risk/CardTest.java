package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Card;
import com.sonsofhesslow.games.risk.model.Die;
import com.sonsofhesslow.games.risk.model.Player;

import org.junit.Test;

import static org.junit.Assert.*;

public class CardTest {
    @Test
    public void testCardCreation() throws Exception {
        Card card = new Card();
        assertNotNull(card);
    }

    @Test
    public void testHandInSet() throws Exception {
        //when you have five cards you can always hand them in.
        for(int i = 0; i< 100; i++)
        {
            Player player = new Player(0);
            //as if player gained cards under a longer period
            for(int j = 0; j < 5; j++){
                player.giveOneCard();
            }
            assertEquals(5, player.getCards().size());
            assertTrue(Card.canHandInSet(player.getCards()));
            Card.handInSet(player.getCards());
            assertEquals(2, player.getCards().size());
        }

        //when you have less than 3 cards you can never hand them in
        for(int i = 0; i< 100; i++)
        {
            Player player = new Player(0);
            //as if player gained cards under a longer period
            for(int j = 0; j < 2; j++){
                player.giveOneCard();
            }
            assertEquals(2, player.getCards().size());
            assertFalse(Card.canHandInSet(player.getCards()));
        }
    }

    @Test
    public void testEquals() throws Exception {
        Card a = new Card();
        Die die = new Die();
        assertFalse(a.equals(null));
        assertFalse(a.equals(die));
        assertTrue(a.equals(a));
    }
}
