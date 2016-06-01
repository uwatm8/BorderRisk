package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CardGridAdapter extends BaseAdapter {

    ArrayList<String> result;
    Context context;
    ArrayList<Integer> imageId;
    private static LayoutInflater inflater = null;
    ArrayList<Boolean> isClicked;
    ArrayList<View> rowView;
    ArrayList<Integer> selectedView;

    public CardGridAdapter(Context mainActivity, ArrayList<String> prgmNameList, ArrayList<Integer> prgmImages) {
        // TODO Auto-generated constructor stub
        result = prgmNameList;
        context = mainActivity;
        imageId = prgmImages;
        rowView = new ArrayList<View>();
        selectedView = new ArrayList<Integer>();
        isClicked = new ArrayList<Boolean>();
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
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

    public ArrayList<Integer> getCardIndexList() {
        return selectedView;
    }

    public class Holder {
        TextView tv;
        ImageView img;
        RelativeLayout rv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View temp = inflater.inflate(R.layout.activity_card, null);
        rowView.add(temp);
        isClicked.add(false);
        holder.tv = (TextView) rowView.get(position).findViewById(R.id.cardText);
        holder.img = (ImageView) rowView.get(position).findViewById(R.id.cardImage);
        holder.tv.setText(result.get(position));
        holder.img.setImageResource(imageId.get(position));

        rowView.get(position).findViewById(R.id.cardMain).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Holder holder = new Holder();
                int tester = 0;
                if (isClicked.get(position)) {
                    selectedView.remove((Integer) position);
                    holder.rv = (RelativeLayout) rowView.get(position).findViewById(R.id.frameColour);
                    holder.rv.setBackgroundColor(Color.parseColor("#cbbba0"));
                    isClicked.set(position, false);
                } else {
                    selectedView.add((Integer) position);
                    for (int x = 0; x < selectedView.size(); x++) {
                        for (int y = 0; y < selectedView.size(); y++) {
                            if (((TextView) rowView.get(selectedView.get(y)).findViewById(R.id.cardText)).getText() == ((TextView) rowView.get(selectedView.get(x)).findViewById(R.id.cardText)).getText() && x != y)
                                tester++;
                        }
                    }

                    if ((selectedView.size() == 0 || tester == (selectedView.size() * selectedView.size() - selectedView.size()) || tester == 0) && selectedView.size() < 4) {
                        holder.rv = (RelativeLayout) rowView.get(position).findViewById(R.id.frameColour);
                        holder.rv.setBackgroundColor(Color.parseColor("#a58218"));
                        isClicked.set(position, true);
                    } else {
                        selectedView.remove((Integer) position);
                    }
                }
            }

        });

        return rowView.get(position);
    }

}
