package com.sonsofhesslow.games.risk.network;

import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Territory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RiskNetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public NetworkAction action;
    public int armies;
    public int participantId;
    public int regionId;

    static RiskNetworkMessage deSerialize(byte[] arr) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(arr));
        RiskNetworkMessage message = (RiskNetworkMessage) ois.readObject();
        return message;
    }

    byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        return bos.toByteArray();
    }

    public RiskNetworkMessage(NetworkAction action, int armies, int participantId, int regionId) {
        this.action = action;
        this.armies = armies;
        this.participantId = participantId;
        this.regionId = regionId;
    }

    public static RiskNetworkMessage territoryChangedMessageBuilder(Territory territory, int newTroops) {
        return new RiskNetworkMessage(NetworkAction.armyAmountChange, newTroops, -1, territory.getId());
    }

    public static RiskNetworkMessage occupierChangedMessageBuilder(Territory territory, Player newOccupier) {
        return new RiskNetworkMessage(NetworkAction.occupierChange, -1, newOccupier.getParticipantId(), territory.getId());
    }

    public static RiskNetworkMessage turnChangedMessageBuilder(Player currentPlayerDone) {
        return new RiskNetworkMessage(NetworkAction.turnChange, -1, currentPlayerDone.getParticipantId(), -1);
    }
}

