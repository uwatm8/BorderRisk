package com.sonsofhesslow.games.risk.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;

import java.util.List;

public interface GooglePlayNetworkCompatible {
    void onRealTimeMessageReceived(RealTimeMessage rtm);
    void broadcast(byte[] messageArray);

    void onConnected(@Nullable Bundle bundle);
    void onConnectionSuspended(int i);
    void onConnectionFailed(@NonNull ConnectionResult connectionResult);
    void onInvitationReceived(Invitation invitation);
    void onInvitationRemoved(String s);
    void onRoomConnecting(Room room);
    void onRoomAutoMatching(Room room);
    void onPeerInvitedToRoom(Room room, List<String> list);
    void onPeerDeclined(Room room, List<String> list);
    void onPeerJoined(Room room, List<String> list);
    void onPeerLeft(Room room, List<String> list);
    void onConnectedToRoom(Room room);
    void onDisconnectedFromRoom(Room room);
    void onPeersConnected(Room room, List<String> list);
    void onPeersDisconnected(Room room, List<String> list);
    void onP2PConnected(String s);
    void onP2PDisconnected(String s);
    void onRoomCreated(int i, Room room);
    void onJoinedRoom(int i, Room room);
    void onLeftRoom(int i, String s);
    void onRoomConnected(int i, Room room);
}
