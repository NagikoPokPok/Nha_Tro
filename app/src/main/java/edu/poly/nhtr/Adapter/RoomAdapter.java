package edu.poly.nhtr.Adapter;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>  {
    private final List<Room> rooms;
    private int lastActionPosition = 0;

    private final RoomListener roomListener;
    Fragment fragment;
    MainViewModel mainViewModel;
    boolean isEnabled = false;
    boolean isSelectAll = false;
    ArrayList<Room> selectList = new ArrayList<>();
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
        return new RoomViewHolder(itemContainerRoomBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        holder.setRoomData(rooms.get(position), position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isEnabled)
                {
                    roomListener.hideFrameTop();
                    // Hiển thị menu delete lên
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MenuInflater menuInflater = mode.getMenuInflater();
                            menuInflater.inflate(R.menu.menu_delete_homes, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            isEnabled = true;
                            ClickItem(holder);
                            toggleAllCheckboxes(true);
                            notifyDataSetChanged(); // Câu lệnh dùng để yêu cầu cập nhật lại giao diện của toàn bộ danh sách.

                            mainViewModel.getText().observe(fragment, new Observer<String>() {
                                @Override
                                public void onChanged(String s) {
                                    mode.setTitle(String.format("Đã chọn %s nhà", s));
                                }
                            });
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.menu_delete) {
                                if(selectList.isEmpty())
                                {
                                    roomListener.showToast("Please select home to delete");
                                }
//                                else{
//                                    roomListener.openDeleteListHomeDialog(selectList, mode);
//                                }
                                // Không gọi mode.finish() ở đây để giữ ActionMode hoạt động.
//                            } else if (id == R.id.menu_select_all) {
//                                if (selectList.size() == homes.size()) {
//                                    isSelectAll = false;
//                                    selectList.clear();
//                                } else {
//                                    isSelectAll = true;
//                                    selectList.clear();
//                                    selectList.addAll(homes);
//                                }
//                                mainViewModel.setText(String.valueOf(selectList.size()));
//                                notifyDataSetChanged();
                               }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            isEnabled = false;
                            isSelectAll = false;
                            selectList.clear();
                            toggleAllCheckboxes(false);
                            roomListener.showFrameTop();
                            notifyDataSetChanged();

                        }
                    };
                    ((AppCompatActivity) v.getContext()).startActionMode(callback);
                }else{
                    ClickItem(holder);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }
    private void ClickItem(RoomAdapter.RoomViewHolder holder) {
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
        mainViewModel.setText(String.valueOf(selectList.size()));
    }

    private void toggleAllCheckboxes(boolean show) {
        Arrays.fill(isVisible, show);
        // for (int i = 0; i < isVisible.length; i++) {
        //            isVisible[i] = show;
        //        }
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
}
