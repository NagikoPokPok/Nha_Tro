package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.poly.nhtr.R;

public class CustomSpinnerAdapter extends BaseAdapter {
    private Context context;
    private String[] items;
    private int selectedPosition = -1;

    public CustomSpinnerAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
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

        text.setText(items[position]);
        if (position == selectedPosition) {
            checkmark.setVisibility(View.VISIBLE);
            checkmark.setColorFilter(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            checkmark.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }
}
