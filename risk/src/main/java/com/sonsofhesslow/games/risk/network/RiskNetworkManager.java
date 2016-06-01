package com.sonsofhesslow.games.risk.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class RiskNetworkManager implements Observer {
    boolean selfModified;
    RiskNetwork riskNetwork = null;
    GoogleApiClient googleApiClient;
    GooglePlayNetwork googlePlayNetwork;
    public static final int RESULT_LEFT_ROOM = 10005;

    public RiskNetworkManager(Context context,UIUpdate uiUpdate) {
        this.googlePlayNetwork = new GooglePlayNetwork();

        // Create the Google Api Client with access to Games
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(googlePlayNetwork)
                .addOnConnectionFailedListener(googlePlayNetwork)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        this.riskNetwork = new RiskNetwork(uiUpdate,this.googleApiClient, googlePlayNetwork);

        riskNetwork.setGooglePlayNetwork(googlePlayNetwork);
        googlePlayNetwork.setNetworkTarget(riskNetwork);
    }

    public RiskNetwork getRiskNetwork() {
        return riskNetwork;
    }

    public void update(Observable obs, Object arg) {
        if (obs instanceof Territory) {
            Territory territory = (Territory) obs;
            if (arg instanceof Integer) {
                /*
                ARMY CHANGE EVENT
                 */
                int event = (Integer) arg;
                if (!selfModified) {
                    RiskNetworkMessage message = RiskNetworkMessage.territoryChangedMessageBuilder(territory, event);
                    try {
                        riskNetwork.broadcast(message.serialize());
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (arg instanceof Player) {
                /*
                OCCUPIER CHANGE EVENT
                 */
                Player event = (Player) arg;
                if (!selfModified) {
                    RiskNetworkMessage message = RiskNetworkMessage.occupierChangedMessageBuilder(territory, event);
                    try {
                        riskNetwork.broadcast(message.serialize());
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        } else if (obs instanceof Risk) {
            /*
            PLAYER CHANGE EVENT LISTENER
             */
            if (arg instanceof Player) {
                Player event = (Player) arg;
                if(!selfModified){
                    RiskNetworkMessage message = RiskNetworkMessage.turnChangedMessageBuilder(event);

                    try {
                        riskNetwork.broadcast(message.serialize());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void startQuickGame() {
        //1-3 opponents
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
    }

    public void startInviteGame(Intent data) {
        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        }

        // create the room
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }

        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
    }

    public void acceptInviteToRoom(String invId) {
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(googlePlayNetwork)
                .setRoomStatusUpdateListener(googlePlayNetwork);
        Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
    }

    public void acceptInviteToRoom() {
        acceptInviteToRoom(riskNetwork.mIncomingInvitationId);
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void signOut() {
        Games.signOut(googleApiClient);
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    public Intent getOpponentIntent() {
        return Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 3);
    }

    public Intent getInvitationIntent(){
        return Games.Invitations.getInvitationInboxIntent(googleApiClient);
    }

    public void leaveRoom() {
        riskNetwork.leaveRoom();
    }

    public boolean isConnected()
    {
        return googleApiClient.isConnected();
    }

    public void acceptInvitation(Intent intent) {
        Invitation inv = intent.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        acceptInviteToRoom(inv.getInvitationId());
    }

    public int[] getParticipantIds() {
        ArrayList<Participant> participants = getRiskNetwork().getmParticipants();

        int c = 0;
        int[] ids = new int[participants.size()];
        for(Participant participant : participants){
            ids[c++] = participant.getParticipantId().hashCode(); //@hash collisions
        }
        return ids;
    }

    public List<String> getParticipantNames() {
        List<String> ret = new ArrayList<>();
        ArrayList<Participant> participants = getRiskNetwork().getmParticipants();
        for(Participant participant : participants)
        {
            ret.add(participant.getDisplayName());
        }
        return ret;
    }

    public List<Uri> getParticipantImages() {
        List<Uri> ret = new ArrayList<>();
        ArrayList<Participant> participants = getRiskNetwork().getmParticipants();
        for(Participant participant : participants)
        {
            ret.add(participant.getIconImageUri());
        }
        return ret;
    }
}

