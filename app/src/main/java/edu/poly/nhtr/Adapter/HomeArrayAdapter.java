package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.models.Home;

public class HomeArrayAdapter extends ArrayAdapter<Home> {
    public HomeArrayAdapter(Context context, List<Home> homes) {
        super(context, 0, homes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_home, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.text_view_item);
        Home home = getItem(position);
        if (home != null) {
            textView.setText(home.getNameHome());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
