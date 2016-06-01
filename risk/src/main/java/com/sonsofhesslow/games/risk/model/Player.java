package com.sonsofhesslow.games.risk.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class Player extends Observable {
    private String name;
    private boolean isAlive;
    private int armiesToPlace;
    private final int participantId;
    private Uri imageRefrence;

    ArrayList<Card> cards = new ArrayList<>();
    private int territoriesOwned;

    public Player(int participantId) {
        Random rand = new Random();
        String[] name = {"Bilbo Baggins", "Filibert Bolger", "Fredegar Bolger", "Mrs. Bracegirdle", "Melilot Brandybuck", "Rosie Cotton", "Elanor Gamgee", "Frodo Gamgee", "Hamfast Gamgee", "Farmer Maggot", "Old Noakes", "Mrs. Proudfoot", "Odo Proudfoot", "Otho Sackville-Baggins", "Lobelia Sackville-Baggins", "Ted Sandyman", "Diamond Took"};
        int n = rand.nextInt(name.length);
        this.name = name[n]; //TODO get name input somehow
        this.participantId = participantId;
        this.isAlive = true;
        imageRefrence = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cardList) {
        cards = cardList;
        setChanged();
        notifyObservers(cards);
    }

    public void giveOneCard() {
        cards.add(Card.getRandomCard());
    }

    public int getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void changeTerritoriesOwned(int change) {
        territoriesOwned += change;
    }

    public void giveArmies(int change) {
        armiesToPlace += change;
    }

    public int getArmiesToPlace() {
        return armiesToPlace;
    }

    public void decArmiesToPlace() {
        armiesToPlace--;
    }

    public void decArmiesToPlace(int amount) {
        armiesToPlace -= amount;
    }

    public int getParticipantId() {
        return participantId;
    }

    public Uri getImageRefrence() {
        return imageRefrence;
    }

    public void setImageRefrence(Uri imageRefrence) {
        this.imageRefrence = imageRefrence;
    }
}
