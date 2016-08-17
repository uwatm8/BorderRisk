package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.network.NetworkAction;
import com.sonsofhesslow.games.risk.network.NetworkChangeEvent;
import com.sonsofhesslow.games.risk.network.NetworkListener;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Observable;

/**
 * Created by Niklas on 02/08/16.
 */
public class ChatController extends Observable implements NetworkListener {
    ArrayList<String> chatMessages = new ArrayList<>();
    ArrayAdapter<String> chatAdapter;
    TextView chatTextField;
    ViewGroup parent;
    Risk risk;
    String message;

    //online
    String selfName;
    boolean online = false;

    ChatController(Context context, ViewGroup parent, Risk risk) {
        chatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, chatMessages);
        this.parent = parent;
        this.risk = risk;
    }

    ChatController(Context context, ViewGroup parent, Risk risk, String selfName) {
        chatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, chatMessages);
        this.parent = parent;
        this.risk = risk;
        this.selfName = selfName;
        online = true;
    }

    public void setChatAdapter() {
        ListView listView = (ListView) parent.findViewById(R.id.chatView);
        if(listView.getAdapter() == null) {
            listView.setAdapter(chatAdapter);
        }
    }

    public void sendMessage() {
        String name;
        if(online) {
            name = selfName;
        } else {
            name = risk.getCurrentPlayer().getName();
        }

        TextView textView = (TextView) parent.findViewById(R.id.chatText);
        String text = textView.getText().toString();
        textView.setText("");

        message = name + ": " + text;
        updateChat(message);

        setChanged();
        notifyObservers(message);
    }

    public void updateChat(String message) {
        //sets adapter if not already set
        setChatAdapter();

        chatMessages.add(message);
        chatAdapter.notifyDataSetChanged();
    }

    public void handleNetworkChange(NetworkChangeEvent event) {
        if(event.action == NetworkAction.chatChange) {
            updateChat(event.getChatMessage());
        }
    }
}
