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
import edu.poly.nhtr.models.Room;

public class CustomListCheckBoxAdapter extends RecyclerView.Adapter<CustomListCheckBoxAdapter.ViewHolder> {

    private final List<Room> rooms;
    private final List<Boolean> checkedStates;
    private final LayoutInflater inflater;
    private final Boolean isClickable;

    public CustomListCheckBoxAdapter(Context context, List<Room> rooms, List<Boolean> checkedStates) {
        this.rooms = rooms;
        this.checkedStates = checkedStates;
        this.inflater = LayoutInflater.from(context);
        this.isClickable = true;
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
        String roomName;
        if(rooms.get(position).getNameRoom().contains("Phòng")){
            roomName = " " + rooms.get(position).getNameRoom();
        }
        else roomName = "Phòng " + rooms.get(position).getNameRoom();
        holder.checkBox.setText(roomName);
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

        // Kiểm tra điều kiện để quyết định có cho phép click hay không
        if (!isClickable) {
            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);
            holder.checkBox.setClickable(false);
            holder.checkBox.setEnabled(false);
        } else {
            holder.itemView.setClickable(true);
            holder.itemView.setEnabled(true);
            holder.checkBox.setClickable(true);
            holder.checkBox.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

//    public void setClickable(Boolean clickable) {
//        isClickable = clickable;
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);

        }
    }
}