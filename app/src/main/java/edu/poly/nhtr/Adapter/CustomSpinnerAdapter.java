package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.poly.nhtr.R;

public class CustomSpinnerAdapter extends BaseAdapter {
    private final Context context;
    private final String[] items;
    private final Boolean isChooseIndex;
    private int selectedPosition = 1;
    private int dropDownResource;

    public CustomSpinnerAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
        this.isChooseIndex = false;
    }
    public CustomSpinnerAdapter(Context context, String[] items, Boolean isChooseIndex) {
        this.context = context;
        this.items = items;
        this.isChooseIndex = isChooseIndex;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        TextView text = convertView.findViewById(R.id.text);
        ImageView checkmark = convertView.findViewById(R.id.checkmark_spinner);

        if (isChooseIndex){
            text.setText(items[position]);
        }else {
            if (position != 0){
                text.setText(items[position]);
            }else {
                position = selectedPosition;
                text.setText(items[position]);
            }
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        if (position != 0 || (position == 0 && isChooseIndex)){
            selectedPosition = position;
            notifyDataSetChanged();
        }

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(dropDownResource, parent, false);
        }

        TextView text = convertView.findViewById(R.id.text);
        ImageView checkmark = convertView.findViewById(R.id.checkmark_spinner);

        text.setText(items[position]);
        if (position == selectedPosition) {
            checkmark.setVisibility(View.VISIBLE);
            checkmark.setColorFilter(context.getResources().getColor(android.R.color.holo_green_light));
        } else {
            checkmark.setVisibility(View.VISIBLE);
            checkmark.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
        }

        if (position == 0 && !isChooseIndex){
            text.setTextColor(Color.GRAY);
        }

        return convertView;
    }

    public void setDropDownViewResource(int resource) {
        this.dropDownResource = resource;
    }

    @Override
    public boolean isEnabled(int position) {
        if (isChooseIndex) return true;
        return position != 0;
    }
}
