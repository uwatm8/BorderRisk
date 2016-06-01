package com.sonsofhesslow.games.risk.model;

import java.util.ArrayList;
import java.util.Random;

public class Card {
    public enum CardType {
        INFANTRY, CAVALRY, ARTILLARY
    }

    private static int setsHandedIn = 0;
    private static final Card INFANTRY_CARD = new Card(CardType.INFANTRY);
    private static final Card CAVALRY_CARD = new Card(CardType.CAVALRY);
    private static final Card ARTILLARY_CARD = new Card(CardType.ARTILLARY);


    private CardType cardType;

    public Card() {
        //random card type
        Random random = new Random();
        int randNum = random.nextInt(3);
        switch (randNum) {
            case 0:
                cardType = CardType.INFANTRY;
                break;
            case 1:
                cardType = CardType.ARTILLARY;
                break;
            case 2:
                cardType = CardType.CAVALRY;
                break;
        }
    }

    public Card(CardType cardType) {
        this.cardType = cardType;
    }

    public static Card getRandomCard() {
        Card randomCard = new Card();
        return randomCard;
    }

    public static void handInSet(ArrayList<Card> cards) {
        //3 different cards
        if (cards.contains(INFANTRY_CARD) && cards.contains(CAVALRY_CARD) && cards.contains(ARTILLARY_CARD)) {
            cards.remove(INFANTRY_CARD);
            cards.remove(CAVALRY_CARD);
            cards.remove(ARTILLARY_CARD);
        } else {
            ArrayList<Card> testCards = new ArrayList<>();
            testCards.add(INFANTRY_CARD);
            testCards.add(CAVALRY_CARD);
            testCards.add(ARTILLARY_CARD);

            //loop to test if there are 3 of any of the cards
            for (Card testCard : testCards) {
                int identicalFound = 0;

                for (int i = 0; i < cards.size() && identicalFound != 3; i++) {
                    if (cards.get(i).equals(testCard)) {
                        identicalFound++;
                    }
                }

                if (identicalFound == 3) {
                    for (int i = 0; i < 3; i++) {
                        cards.remove(testCard);
                    }
                }
            }
        }
    }

    public static boolean canHandInSet(ArrayList<Card> cards) {
        //3 different cards test
        if (cards.contains(INFANTRY_CARD) && cards.contains(CAVALRY_CARD) && cards.contains(ARTILLARY_CARD)) {
            return true;
        } else {
            ArrayList<Card> testCards = new ArrayList<>();
            testCards.add(INFANTRY_CARD);
            testCards.add(CAVALRY_CARD);
            testCards.add(ARTILLARY_CARD);

            //3 identical cards test
            for (Card testCard : testCards) {
                int identicalFound = 0;

                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).equals(testCard)) {
                        identicalFound++;
                        if (identicalFound == 3) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int cardAmountToGet() {
        int amountToGet = 0;
        if (setsHandedIn < 6) {
            amountToGet = 4 + setsHandedIn * 2;
        } else {
            amountToGet = -15 + setsHandedIn * 5;
        }
        setsHandedIn++;
        return amountToGet;
    }

    public static int currentCardAmountToGet() {
        int amountToGet = 0;
        if (setsHandedIn < 6) {
            amountToGet = 4 + setsHandedIn * 2;
        } else {
            amountToGet = -15 + setsHandedIn * 5;
        }
        return amountToGet;
    }

    @Override
    public String toString() {
        return "Card{" + "cardType: " + cardType + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }

        Card card = (Card) o;

        return cardType == card.cardType;
    }

    @Override
    public int hashCode() {
        return cardType.hashCode();
    }

    public CardType getCardType() {
        return cardType;
    }
}
