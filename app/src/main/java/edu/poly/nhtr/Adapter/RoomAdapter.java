package edu.poly.nhtr.Adapter;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.poly.nhtr.Activity.MainViewModel;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomBinding;
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.models.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>  {
    private final List<Room> rooms;
    private int lastActionPosition = 0;

    private final RoomListener roomListener;
    Fragment fragment;
    MainViewModel mainViewModel;
    private boolean isCheckBoxClicked = false;
    private boolean isDeleteClicked = false;
    boolean isEnabled = false;
    boolean isSelectAll = false;
    public List<Room> selectList = new ArrayList<>();
    public List<Room> getSelectList() {
        return selectList;
    }

    public void setSelectList(List<Room> selectList) {
        this.selectList = selectList;
    }

    boolean[] isVisible;

    public RoomAdapter(List<Room> rooms, RoomListener roomListener, RoomFragment roomFragment) {
        this.rooms = rooms;
        this.roomListener = roomListener;
        this.fragment =roomFragment;
        isVisible = new boolean[rooms.size()];
    }

    @NonNull
    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRoomBinding itemContainerRoomBinding = ItemContainerRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        mainViewModel = ViewModelProviders.of(fragment)
                .get(MainViewModel.class);
        //roomListener.deleteListAll(selectList);
        //updateList();
        return new RoomViewHolder(itemContainerRoomBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        holder.setRoomData(rooms.get(position), position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                roomListener.setDelectAllUI();
                holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
                holder.binding.frmImage.setVisibility(View.GONE);
                ClickItem(holder);
                toggleAllCheckboxes(true);
                updateList();
                return false;
            }
        });
            performCheckBoxes(holder, position);
    }

    public void performCheckBoxes(RoomAdapter.RoomViewHolder holder, int position) {
        holder.binding.ivCheckBox.setOnClickListener(v -> {
            if (holder.binding.ivCheckBox.isChecked()) {
                // Item nào được setChecked laf true thì add vào list
                selectList.add(rooms.get(holder.getAdapterPosition()));
            } else {
                selectList.remove(rooms.get(holder.getAdapterPosition()));
            }
            roomListener.showToast(selectList.size()+"");
        });



//        holder.itemView.setOnClickListener(v -> {
//            if (isEnabled) { // Nếu thanh actionMode vẫn còn hiện
//                ClickItem(holder);
//            } else {
//                roomListener.onRoomClicked(rooms.get(holder.getAdapterPosition()));
//            }
//        });

        if (isSelectAll || isVisible[position]) {
            // an nut 3 cham
            holder.binding.frmImage.setVisibility(View.INVISIBLE);
            // Lệnh này hiển thị check box lên cho toàn bộ item
            holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
            // Lệnh dưới này kiểm tra: Item nào có trong selectList thì sẽ được setChecked là true, ko có thì false
            holder.binding.ivCheckBox.setChecked(selectList.contains(rooms.get(position))); // Hàm contains sẽ trả về kiểu false/true
        } else { // Khi isVisible = false (khi gọi hàm onDestroyActionMode) thì cho toàn bộ ivCheckBox về false
            holder.binding.ivCheckBox.setChecked(false);
            holder.binding.ivCheckBox.setVisibility(View.GONE);
            holder.binding.frmImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }
    public void ClickItem(RoomAdapter.RoomViewHolder holder) {
        // Item được click vào sẽ hiện check box true và được add vào trong selectList (cái này quan trọng)
        Room s = rooms.get(holder.getAdapterPosition());
        if(!holder.binding.ivCheckBox.isChecked())
        {
            holder.binding.ivCheckBox.setChecked(true);
            selectList.add(s);
        }else{
            holder.binding.ivCheckBox.setChecked(false);
            selectList.remove(s);
        }

    }

    private void toggleAllCheckboxes(boolean show) {
        Arrays.fill(isVisible, show);

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
            binding.getRoot().setOnClickListener(v-> {roomListener.onRoomClick(room);});

            String hoTen = binding.edtHoTen.getText().toString().trim();
            if(hoTen.isEmpty()){
                binding.layoutNameOfGuest.setVisibility(View.GONE);
                binding.layoutPhoneNumberOfGuest.setVisibility(View.GONE);
                binding.layoutPaymentStatus.setVisibility(View.GONE);
                binding.layoutNumberOfGuestsAreLiving.setVisibility(View.GONE);
                // Tạo LayoutParams cho LinearLayout
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.layoutPriceOfRoom.getLayoutParams();
                // Thiết lập margin top (đơn vị là pixel)
                params.topMargin = 30;
                // Áp dụng LayoutParams trở lại LinearLayout
                binding.layoutPriceOfRoom.setLayoutParams(params);
            }

            if(binding.txtDescribe.getText().length() == 0)
            {
                binding.lineOfItemRoom.setVisibility(View.GONE);
                binding.txtDescribe.setVisibility(View.GONE);
                // Tạo LayoutParams cho LinearLayout
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.layoutPriceOfRoom.getLayoutParams();
                // Thiết lập margin top (đơn vị là pixel)
                params.bottomMargin = 40;
                // Áp dụng LayoutParams trở lại LinearLayout
                binding.layoutPriceOfRoom.setLayoutParams(params);
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
    public void updateRoom(int position) {
        //homeList.set(position, home);
        lastActionPosition = position - 1;

    }
    public void removeRoom(int position) {
        lastActionPosition = position - 2;
    }
    public int getLastActionPosition() {
        return lastActionPosition;
    }


    public void isDeleteClicked(boolean isClicked) {
        isDeleteClicked = isClicked;
        notifyDataSetChanged();
    }
    public void isCheckBoxClicked(boolean isClicked) {
        isCheckBoxClicked = isClicked;
        notifyDataSetChanged();
    }
    public void updateList() {
        notifyDataSetChanged();

    }
}
