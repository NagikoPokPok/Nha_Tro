package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>  {
    private final List<Room> rooms;
    private int lastActionPosition = 0;

    private final RoomListener roomListener;

    public RoomAdapter(List<Room> rooms, RoomListener roomListener) {
        this.rooms = rooms;
        this.roomListener = roomListener;
    }

    @NonNull
    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRoomBinding itemContainerRoomBinding = ItemContainerRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new RoomViewHolder(itemContainerRoomBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        holder.setRoomData(rooms.get(position), position);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder{
        public ItemContainerRoomBinding binding;
        RoomViewHolder(ItemContainerRoomBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            this.binding = itemContainerUserBinding;
    }


        void setRoomData(Room room, int position){
            binding.txtNameRoom.setText(room.nameRoom);
            binding.txtHoTen.setText(room.nameUser);
            binding.txtSoDienThoai.setText(room.phoneNumer);
            binding.txtTrangThaiThanhToan.setText(room.status);
            binding.txtSoNguoiDangO.setText(room.numberOfMemberLiving);
            binding.txtPrice.setText(room.price);
            binding.txtDescribe.setText(room.describe);
            binding.txtOrdinalNumber.setText(String.valueOf(position + 1));
//            binding.getRoot().setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    roomListener.onRoomClicked(room);
//                }
//            });

            binding.imgMenu.setOnClickListener(v-> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                roomListener.openPopup(v, room, binding);
            });
        }
}
    public void addRoom(List<Room>rooms) {
        lastActionPosition = rooms.size() - 1;

    }
    public int getLastActionPosition() {
        return lastActionPosition;
    }
}
