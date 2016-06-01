package com.sonsofhesslow.games.risk.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.List;

public class GooglePlayNetwork implements RealTimeMessageReceivedListener , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {

    private GooglePlayNetworkCompatible networkTarget;

    public void onRealTimeMessageReceived(RealTimeMessage rtm){
        if(networkTarget != null) {
            networkTarget.onRealTimeMessageReceived(rtm);
        }
    }

    public void broadcast(byte[] messageBuffer, GoogleApiClient mGoogleApiClient, ArrayList<Participant> participants, String roomId) {
        // Send to every other participant.
        for (Participant participant : participants) {
            //should send message
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, messageBuffer, roomId, participant.getParticipantId());
        }
    }

    public void setNetworkTarget(GooglePlayNetworkCompatible networkAdapter) {
        this.networkTarget = networkAdapter;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(networkTarget == null) {
            throw new RuntimeException("network target was null"); //@bugs, @// FIXME: 27/05/2016 
        }
        else
        {
            networkTarget.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        networkTarget.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        networkTarget.onConnectionFailed(connectionResult);
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        networkTarget.onInvitationReceived(invitation);
    }

    @Override
    public void onInvitationRemoved(String s) {
        networkTarget.onInvitationRemoved(s);
    }

    @Override
    public void onRoomConnecting(Room room) {
        networkTarget.onRoomConnecting(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        networkTarget.onRoomAutoMatching(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        networkTarget.onPeerInvitedToRoom(room, list);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        networkTarget.onPeerDeclined(room, list);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        networkTarget.onPeerJoined(room, list);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        networkTarget.onPeerLeft(room, list);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        networkTarget.onConnectedToRoom(room);
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        networkTarget.onDisconnectedFromRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        networkTarget.onPeersConnected(room, list);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        networkTarget.onPeersDisconnected(room, list);
    }

    @Override
    public void onP2PConnected(String s) {
        networkTarget.onP2PConnected(s);
    }

    @Override
    public void onP2PDisconnected(String s) {
        networkTarget.onP2PDisconnected(s);
    }

    @Override
    public void onRoomCreated(int i, Room room) {
        networkTarget.onRoomCreated(i ,room);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        networkTarget.onJoinedRoom(i, room);
    }

    @Override
    public void onLeftRoom(int i, String s) {
        networkTarget.onLeftRoom(i, s);
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        networkTarget.onRoomConnected(i, room);
    }
}
