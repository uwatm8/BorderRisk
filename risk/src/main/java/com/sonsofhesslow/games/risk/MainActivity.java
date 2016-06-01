package com.sonsofhesslow.games.risk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.graphics.GLTouchEvent;
import com.sonsofhesslow.games.risk.graphics.GLTouchListener;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.Camera;
import com.sonsofhesslow.games.risk.graphics.GLRenderer;
import com.sonsofhesslow.games.risk.graphics.RiskGLSurfaceView;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;
import com.sonsofhesslow.games.risk.network.RiskNetworkManager;
import com.sonsofhesslow.games.risk.network.UIUpdate;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GLTouchListener, View.OnClickListener, UIUpdate {

    public static Resources resources;
    public static Context context;
    static Overlay overlayController;
    RiskGLSurfaceView graphicsView;

    final static String TAG = "Risk";

    // Request codes for the UIs that is shown with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    Vector2 prevPos;

    private Controller controller;

    private RiskNetworkManager riskNetworkManager = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        graphicsView = new RiskGLSurfaceView(this, getResources());
        overlayController = new Overlay(this);
        graphicsView.addListener(this);

        riskNetworkManager = new RiskNetworkManager(this, this);

        // set up a click listener for everything in main menus
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            overlayController.changeGridLayout(true);
        } else {
            overlayController.changeGridLayout(false);
        }
    }

    public void handle(GLTouchEvent event) {

        if (prevPos != null) {
            //sq.setPos(event.worldPosition);
            Vector2 delta;
            if (!event.isZooming) {
                delta = Vector2.sub(GLRenderer.screenToWorldCoords(prevPos, 0), event.worldPosition);
            } else {
                delta = new Vector2(0, 0);
            }
            switch (event.e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Camera cam = Camera.getInstance();
                    cam.setPosRel(new Vector3(delta, event.scale));
                    break;
            }
        }

        graphicsView.requestRender();
        prevPos = event.screenPosition;
    }

    //handles the menu-button events
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_single_player:
            case R.id.button_single_player_2:
                // play a single-player game
                startGame(false, new int[2]);
                break;
            case R.id.button_sign_in:
                Log.d(TAG, "Sign-in button clicked");
                riskNetworkManager.getRiskNetwork().signInClicked = true;
                riskNetworkManager.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                Log.d(TAG, "Sign-out button clicked");
                riskNetworkManager.signOut();
                riskNetworkManager.disconnect();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = riskNetworkManager.getOpponentIntent();
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = riskNetworkManager.getInvitationIntent();
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                removeInvitation();
                // user wants to accept the invitation shown on the invitation popup (from OnInvitationReceivedListener).
                riskNetworkManager.acceptInviteToRoom();
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    //starts an online game with random players
    void startQuickGame() {
        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.startQuickGame();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // result from "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // result from the "select invitation" UI (invitation inbox).
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                } else if (responseCode == riskNetworkManager.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). Leaving room
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                if (responseCode == Activity.RESULT_OK) {
                    riskNetworkManager.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation to accept.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");

        // accept invitation
        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.acceptInvitation(data);
    }

    // Handle the result of the "Select players UI", launched when the user clicked the
    // "Invite friends" button. Creating a room with selected players.
    public void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }
        Log.d(TAG, "Select players UI succeeded.");

        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.startInviteGame(data);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Activity is going to the background. Leave the current room.
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if player in a room, leave it.
        //leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (riskNetworkManager == null || !riskNetworkManager.isConnected()) {
            switchToScreen(R.id.screen_sign_in);
        } else {
            // TODO: 2016-05-29 fix later, removed to prevent freezing 
            //switchToScreen(R.id.screen_wait);
        }
        super.onStop();
    }

    public void onStart() {
        switchToScreen(R.id.screen_wait);
        if (riskNetworkManager != null) {
            if (riskNetworkManager.isConnected()) {
                Log.w(TAG,
                        "GameHelper: client was already connected on onStart()");
            } else {
                Log.d(TAG, "Connecting client.");
                riskNetworkManager.connect();
            }
        }

        super.onStart();
    }

    // Handle back key to make sure player cleanly leave a game if player are in the middle of one
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            //switchToScreen(mCurScreen);
            setContentView(R.layout.activity_main);
            mCurScreen = R.id.screen_main;
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    public void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (riskNetworkManager != null) {
            riskNetworkManager.leaveRoom();
        }
        switchToMainScreen();
    }

    public void startGame(boolean isOnline, int[] ids) {
        //this.riskNetworkManager = new RiskNetworkManager(this, this);
        resources = getResources();

        if (graphicsView.getParent() != null) {
            setContentView(R.layout.activity_main);
            context = this;
            overlayController = new Overlay(this);
            graphicsView = new RiskGLSurfaceView(this, getResources());
            graphicsView.addListener(this);
        }

        //keeps screen turned on until game is finnished
        keepScreenOn();

        if (isOnline) {
            initOnlineGame(ids);
            List<Uri> images = riskNetworkManager.getParticipantImages();
            List<String> names = riskNetworkManager.getParticipantNames();
            Iterator<Uri> imageIterator = images.iterator();
            Iterator<String> namesIt = names.iterator();
            for (Player player : Controller.getRiskModel().getPlayers()) {
                player.setName(namesIt.next());
                player.setImageRefrence(imageIterator.next());
            }
        } else {
            initOfflineGame(ids);
        }

        if (graphicsView.getParent() == null) {
            overlayController.addView(graphicsView);
            overlayController.addView(R.layout.activity_mainoverlay);
            overlayController.addView(R.layout.activity_cards);

            graphicsView.addListener(controller);
        }
        setContentView(overlayController.getOverlay());
        overlayController.setGamePhase(Risk.GamePhase.PICK_TERRITORIES);
        mCurScreen = R.id.screen_game;
        overlayController.changeGridLayout(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void startGame(boolean isOnline) {
        startGame(isOnline, riskNetworkManager.getParticipantIds());
    }

    //UI SECTION. Methods that implement the game's UI.

    // This array lists everything that's clickable, for installing click event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_single_player,
            R.id.button_single_player_2
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };

    int mCurScreen = -1;

    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            // TODO: 2016-05-24 fix for real?, not just null check
            if (findViewById(id) != null) {
                findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
            }
        }
        mCurScreen = screenId;

        if (findViewById(R.id.invitation_popup) != null) {
            findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        }
    }

    void switchToMainScreen() {
        if (riskNetworkManager != null && riskNetworkManager.isConnected()) {
            removeInvitation();
            for (int id : CLICKABLES) {
                findViewById(id).setOnClickListener(this);
            }

            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }


    }

    //GAME BUTTONS SECTION - handle buttonevents from the buttons in game (not menu)

    public void nextTurnPressed(View v) {
        //controller has always been initialized since nextTurn button
        //is not visible before startGame button has been pressed
        //TODO give territories continents, setArmiesToPlace gives nullpointerexception when pressed
        Log.d(TAG, "nextturn pressed");
        controller.nextTurn();
    }

    public void showCardsPressed(View v) {
        //TODO show new layout with cards and trade in button
        overlayController.setCardVisibility(true);
    }

    public void fightPressed(View v) {
        controller.fightButtonPressed();
    }

    public void placePressed(View v) {
        controller.placeButtonPressed(overlayController.getBarValue());
    }

    public void donePressed(View v) {
        controller.doneButtonPressed();
        overlayController.setNextTurnVisible(true);
    }

    public void hideCards(View v) {
        overlayController.setCardVisibility(false);
        overlayController.setNextTurnVisible(true);
    }

    public void backToMainscreenPressed(View v) {
        setContentView(R.layout.activity_main);
        mCurScreen = R.id.screen_main;
        leaveRoom();
    }

    //MISC SECTION. Miscellaneous methods

    // Sets the flag to keep this screen on
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initOnlineGame(int[] ids) {
        System.out.println("init online game");

        this.controller = new Controller(ids, overlayController, getResources());
        controller.setSelfId(riskNetworkManager.getRiskNetwork().getmMyId().hashCode());
        //add to observables
        Risk riskModel = controller.getRiskModel();
        riskModel.addObserver(riskNetworkManager);

        for (Territory territory : riskModel.getTerritories()) {
            territory.addObserver(riskNetworkManager);
        }

        riskNetworkManager.getRiskNetwork().addListener(controller);
    }

    private void initOfflineGame(int[] ids) {
        this.controller = new Controller(ids, overlayController, getResources());
    }

    private void resetGameVars() {
        // TODO: 2016-05-13 if needed
    }

    public Controller getController() {
        return controller;
    }

    public void showList(View v) {
        if (overlayController.listPopulated) {
            overlayController.setListVisible(true);
        }
    }

    public void hideList(View v) {
        overlayController.setListVisible(false);
    }

    public void getCardsPressed(View v) {
        if (overlayController.getSelectedCards().size() == 3) {
            controller.turnInCards(overlayController.getSelectedCards());
        }
    }

    boolean showInvPopup = false;
    //setting up the callbacks for the network.
    @Override
    public void displayInvitation(String caller) {
        showInvPopup = true;
        //invitation to play a game, store it in mIncomingInvitationId and show popup on screen
        if(findViewById(R.id.incoming_invitation_text) != null) {
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    caller + " " +
                            getString(R.string.is_inviting_you));
        }
        switchToScreen(getmCurScreen()); // This will show the invitation popup

    }

    @Override
    public void removeInvitation() {
        showInvPopup = false;
        switchToScreen(getmCurScreen()); //hide the invitation popup
    }

    @Override
    public void showWaitingRoom(Intent intent) {
        startActivityForResult(intent, RC_WAITING_ROOM);
    }

    @Override
    public void displayError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
    }

    @Override
    public void startGame() {
        startGame(true);
    }

    @Override
    public void showMainScreen() {
        switchToScreen(R.id.screen_main);
    }

    @Override
    public void showSignInScreen() {
        switchToScreen(R.id.screen_sign_in);
    }

    @Override
    public void showWaitScreen() {
        switchToScreen(R.id.screen_wait);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    public int getmCurScreen() {
        return mCurScreen;
    }
}
