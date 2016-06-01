//multiple parts of file taken from Google's example project - https://github.com/playgameservices/android-basic-samples
package com.sonsofhesslow.games.risk.network;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.R;

import java.util.ArrayList;
import java.util.List;

public class RiskNetwork implements GooglePlayNetworkCompatible {
    public static Resources resources;
    public boolean signInClicked = false;

    UIUpdate uiUpdate;

    final static String TAG = "Risk";

    // Request codes for the UIs that is shown with startActivityForResult:
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = false;

    // Room ID where the currently active game is taking place
    String mRoomId = null;



    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // Id of the invitation player received via the invitation listener
    String mIncomingInvitationId = null;

    //google network in use
    private GooglePlayNetwork googlePlayNetwork = null;

    public RiskNetwork(UIUpdate uiUpdate, GoogleApiClient mGoogleApiClient, GooglePlayNetwork googlePlayNetwork) {
        setGooglePlayNetwork(googlePlayNetwork);
        googlePlayNetwork.setNetworkTarget(this);
        this.mGoogleApiClient = mGoogleApiClient;
        this.uiUpdate = uiUpdate;
    }

    private boolean selfModified = false;

    void acceptInviteToRoom() {
        acceptInviteToRoom(mIncomingInvitationId);
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        Log.d(TAG, "Accepting invitation: " + mIncomingInvitationId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(googlePlayNetwork)
                .setRoomStatusUpdateListener(googlePlayNetwork);
        uiUpdate.showWaitScreen();
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Show the waiting room UI
    void showWaitingRoom(Room room) {
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        uiUpdate.showWaitingRoom(i);
    }

    public void onInvitationReceived(Invitation invitation) {
        mIncomingInvitationId = invitation.getInvitationId();
        uiUpdate.displayInvitation(invitation.getInviter().getDisplayName());
    }

    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId) ) {
            mIncomingInvitationId = null;
            uiUpdate.removeInvitation();
        }
    }


    //CALLBACKS SECTION. (API callbacks)

    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        //to be notified when invited to play
        Games.Invitations.registerInvitationListener(mGoogleApiClient, googlePlayNetwork);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToMainOrSignIn();
    }

    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (signInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            signInClicked = false;
            BaseGameUtils.resolveConnectionFailure(uiUpdate.getActivity(), mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, uiUpdate.getActivity().getString(R.string.signin_other_error));
        }
        uiUpdate.showSignInScreen();
        //switchToScreen(R.id.screen_sign_in);
    }
    //connected to the room, (not playing yet)
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");
        //get participants and my ID:
        //mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
        // save room ID if its not initialized in onRoomCreated() so player can leave cleanly before the game starts.
        if(mRoomId==null) {
            mRoomId = room.getRoomId();
        }

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }
    
    public void leaveRoom() {
        if(mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, googlePlayNetwork, mRoomId);
        }
    }

    // Called when player successfully left the room (this happens a result of voluntarily leaving via a call to leaveRoom().
    public void onLeftRoom(int statusCode, String roomId) {
        // player have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainOrSignIn();
    }

    // Called when player  get disconnected from the room. User returns to the main screen.
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        uiUpdate.displayError();
    }

    // room has been created
    public void onRoomCreated(int statusCode, Room room) {
        /*if(mParticipants == null) {
            mParticipants = room.getParticipants();
        }*/

        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so player can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        this.mParticipants = room.getParticipants();

        uiUpdate.startGame();
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }


    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // TODO: 2016-05-13 useful things, not just update
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            // TODO: 2016-05-13 update variables from participants
        }
    }


    //COMMUNICATIONS SECTION. Methods that implement the game's network protocol
    private ArrayList<NetworkListener> listeners = new ArrayList<>();
    public void addListener( NetworkListener listener){
        listeners.add(listener);
    }
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }
    private void notifyListners(NetworkChangeEvent event) {
        for(NetworkListener listener: listeners){
            listener.handleNetworkChange(event);
        }
    }
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] messageBuffer = rtm.getMessageData();
        selfModified = true;
        try {
            RiskNetworkMessage recievedNetworkData = RiskNetworkMessage.deSerialize(messageBuffer);
            notifyListners(new NetworkChangeEvent(recievedNetworkData));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        selfModified = false;
    }

    // Broadcast to everybody else.
    public void broadcast(byte[] messageArray) {
        if(!selfModified && googlePlayNetwork != null) {
            ArrayList<Participant> targetParticipants = new ArrayList<Participant>();

            for(Participant participant : mParticipants) {
                if (participant.getParticipantId().equals(mMyId)) {
                    //should not be sending message to own phone
                    continue;
                }
                if (participant.getStatus() != Participant.STATUS_JOINED) {
                    //should not be sending message if player has not joined properly
                    continue;
                }

                targetParticipants.add(participant);
            }

            googlePlayNetwork.broadcast(messageArray, mGoogleApiClient, targetParticipants, mRoomId);
        }
    }


    //UI SECTION. Methods that implement the game's UI.

    void switchToMainOrSignIn() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            uiUpdate.showMainScreen();
        }
        else {
            uiUpdate.showSignInScreen();
        }
    }


    //MISC SECTION. Miscellaneous methods

    private void resetGameVars() {
        // TODO: 2016-05-13
    }

    public void setGooglePlayNetwork(GooglePlayNetwork googlePlayNetwork) {
        this.googlePlayNetwork = googlePlayNetwork;
    }

    public String getmMyId() {
        return mMyId;
    }

    public ArrayList<Participant> getmParticipants() {
        return mParticipants;
    }
}
