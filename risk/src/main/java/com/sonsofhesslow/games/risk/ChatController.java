package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sonsofhesslow.games.risk.model.Risk;

import java.util.ArrayList;

/**
 * Created by Niklas on 02/08/16.
 */
public class ChatController {
    ArrayList<String> chatMessages = new ArrayList<>();
    ArrayAdapter<String> chatAdapter;
    TextView chatTextField;
    ViewGroup parent;
    Risk risk;
    String message;

    ChatController(Context context, ViewGroup parent, Risk risk) {
        chatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, chatMessages);
        this.parent = parent;
        this.risk = risk;
    }

    public void setChatAdapter() {
        ListView listView = (ListView) parent.findViewById(R.id.chatView);
        listView.setAdapter(chatAdapter);
    }

    public void sendMessage() {
        //TODO set message from textfield
        String name = risk.getCurrentPlayer().getName();
        TextView textView = (TextView) parent.findViewById(R.id.chatText);
        String text = textView.getText().toString();
        textView.setText("");
        message = name + ": " + text;

        ListView listView = (ListView) parent.findViewById(R.id.chatView);
        if(listView.getAdapter() == null) {
            listView.setAdapter(chatAdapter);
        }

        chatMessages.add(message);
        chatAdapter.notifyDataSetChanged();
    }

    public void sendMessage(String name, String text) {
        message = name + ": " + text;

        ListView listView = (ListView) parent.findViewById(R.id.chatView);
        if(listView.getAdapter() == null) {
            listView.setAdapter(chatAdapter);
        }

        chatMessages.add(message);
        chatAdapter.notifyDataSetChanged();
    }
}
