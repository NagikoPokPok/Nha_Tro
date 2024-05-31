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
        RoomViewHolder(ItemContainerRoomBinding itemContainerRoomBinding){
            super(itemContainerRoomBinding.getRoot());
            this.binding = itemContainerRoomBinding;
    }


        void setRoomData(Room room, int position){
            binding.txtNameRoom.setText(room.nameRoom);
            binding.edtHoTen.setText(room.nameUser);
            binding.edtSoDienThoai.setText(room.phoneNumer);
            binding.edtTrangThaiThanhToan.setText(room.status);
            binding.edtSoNguoiDangO.setText(room.numberOfMemberLiving);
            binding.txtPrice.setText(room.price);
            binding.txtDescribe.setText(room.describe);
            binding.txtOrdinalNumber.setText(String.valueOf(position + 1));

            String hoTen = binding.edtHoTen.getText().toString().trim();
            if(hoTen.isEmpty()){
                binding.edtHoTen.setVisibility(View.GONE);
                binding.edtSoDienThoai.setVisibility(View.GONE);
                binding.edtTrangThaiThanhToan.setVisibility(View.GONE);
                binding.edtSoNguoiDangO.setVisibility(View.GONE);
                binding.txtHoTen.setVisibility(View.GONE);
                binding.txtSoDienThoai.setVisibility(View.GONE);
                binding.txtTrangThaiThanhToan.setVisibility(View.GONE);
                binding.txtSoNguoiDangO.setVisibility(View.GONE);
            }

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
