package com.sonsofhesslow.games.risk.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Die {

    public static int roll() {
        return roll(6);
    }

    public static int roll(int sides) {
        Random random = new Random();
        int number = random.nextInt(sides) + 1;
        return number;
    }

    public static void fight(Territory attacker, Territory defender) {
        if(attacker != null && defender != null && attacker.getOccupier() != null && defender.getOccupier() != null) {
            if (!attacker.getOccupier().equals(defender.getOccupier()) && attacker.getArmyCount() > 1 && defender.getArmyCount() > 0) {
                int diceAmountAttacker = Math.min(attacker.getArmyCount() - 1, 3);
                int diceAmountDefender = Math.min(defender.getArmyCount(), 2);

                ArrayList<Integer> attackDiceValues = new ArrayList<>();
                ArrayList<Integer> defendDiceValues = new ArrayList<>();

                for (int i = 0; i < diceAmountAttacker; i++) {
                    attackDiceValues.add(roll());
                }
                for (int i = 0; i < diceAmountDefender; i++) {
                    defendDiceValues.add(roll());
                }

                Collections.sort(attackDiceValues);
                Collections.sort(defendDiceValues);

                //roll first die
                if (attackDiceValues.get(diceAmountAttacker - 1) > defendDiceValues.get(diceAmountDefender - 1)) {
                    defender.changeArmyCount(-1);
                } else {
                    attacker.changeArmyCount(-1);
                }

                //roll second die
                if (attacker.getArmyCount() > 2 && defender.getArmyCount() > 1) {
                    if (attackDiceValues.get(diceAmountAttacker - 2) > defendDiceValues.get(diceAmountDefender - 2)) {
                        defender.changeArmyCount(-1);
                    } else {
                        attacker.changeArmyCount(-1);
                    }
                }

                if (defender.getArmyCount() == 0) {
                    defender.setOccupier(attacker.getOccupier());
                }
            }
        }
    }

    //fight until either attacker or defender runs out of troops
    public static void fightCompletely(Territory attacker, Territory defender) {
        while (!attacker.getOccupier().equals(defender.getOccupier()) && attacker.getArmyCount() > 1 && defender.getArmyCount() > 0) {
            fight(attacker, defender);
        }
    }
}
