package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.fragment.ServiceFragment;

public class CustomListCheckBoxAdapter extends RecyclerView.Adapter<CustomListCheckBoxAdapter.ViewHolder> {

    private List<String> items;
    private List<Boolean> checkedStates;
    private LayoutInflater inflater;

    public CustomListCheckBoxAdapter(Context context, List<String> items, List<Boolean> checkedStates) {
        this.items = items;
        this.checkedStates = checkedStates;
        this.inflater = LayoutInflater.from(context);
    }



    @NonNull
    @Override
    public CustomListCheckBoxAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomListCheckBoxAdapter.ViewHolder holder, int position) {
        // Đặt dữ liệu cho CheckBox
        holder.checkBox.setText(items.get(position));
        holder.checkBox.setChecked(checkedStates.get(position));

        // Thiết lập listener cho CheckBox
        holder.checkBox.setOnCheckedChangeListener(null); // Ngăn chặn callback không mong muốn

        // Sử dụng holder.getAdapterPosition() để lấy vị trí hiện tại
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                checkedStates.set(currentPosition, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}