package com.sonsofhesslow.games.risk.network;

public class NetworkChangeEvent {

    public NetworkChangeEvent(RiskNetworkMessage message) {
        this.action = message.action;
        this.armies = message.armies;
        this.regionId = message.regionId;
        this.participantId = message.participantId;
        this.chatMessage = message.chatMessage;
    }

    public NetworkAction action;
    private int armies;
    private int regionId;
    private int participantId;
    private String chatMessage;
    public int getArmies()
    {
        if(action == NetworkAction.armyAmountChange)
        {
            return armies;
        }
        else {
            throw new RuntimeException("Armies did not change");
        }
    }

    public int getParticipantId()
    {
        if(action == NetworkAction.occupierChange || action == NetworkAction.turnChange)
        {
            return participantId;
        }
        else {
            throw new RuntimeException("Player id did not change");
        }
    }

    public int getRegionId()
    {
        if(action == NetworkAction.armyAmountChange || action == NetworkAction.occupierChange)
        {
            return regionId;
        }
        else {
            throw new RuntimeException("Player id did not change");
        }
    }

    public String getChatMessage() {
        if(action == NetworkAction.chatChange) {
            return chatMessage;
        } else {
            throw new RuntimeException("No message was sent");
        }
    }
}
