package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    ArrayList<String> playerName;
    ArrayList<String> armyCount;
    ArrayList<float[]> colours;
    Context context;
    ArrayList<Uri> imageId;
    private static LayoutInflater inflater = null;

    public CustomAdapter(Context mainActivity, ArrayList<String> playerName, ArrayList<Uri> playerImage, ArrayList<String> armyCount, ArrayList<float[]> colours) {
        // TODO Auto-generated constructor stub
        this.playerName = playerName;
        this.armyCount = armyCount;
        this.colours = colours;
        context = mainActivity;
        imageId = playerImage;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return playerName.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.activity_playerinfo, null);
        holder.tv = (TextView) rowView.findViewById(R.id.playerName);
        holder.img = (ImageView) rowView.findViewById(R.id.playerImage);
        holder.tv.setText(playerName.get(position));
        holder.tv = (TextView) rowView.findViewById(R.id.armiesToPlace);
        holder.tv.setText(armyCount.get(position));
        holder.tv = (TextView) rowView.findViewById(R.id.colourHax);
        if (colours.size() > position) {
            holder.tv.setBackgroundColor(Util.getIntFromColor(colours.get(position)));
        }
        if (imageId.get(position) != null) {
            ImageManager mrg = ImageManager.create(context);
            mrg.loadImage(holder.img, imageId.get(position));
        } else {
            holder.img.setImageResource(R.drawable.ic_account_box_black_48dp);
        }
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked " + playerName.get(position), Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }

} 