package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Activity.MainViewModel;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomPresenter;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>  {
    Context context;
    List<Room> rooms;
    private int lastActionPosition = 0;

    private final RoomListener roomListener;
    private final RoomPresenter roomPresenter;
    Fragment fragment;
    MainViewModel mainViewModel;
    private boolean isCheckBoxClicked = false;
    private boolean isDeleteClicked = false;
    boolean isEnabled = false;
    boolean isSelectAll = false;
    private List<Room> selectList = new ArrayList<>();

    boolean[] isVisible;

    public RoomAdapter(List<Room> rooms, RoomListener roomListener, RoomPresenter roomPresenter, RoomFragment roomFragment) {

        this.rooms = rooms;
        this.roomListener = roomListener;
        this.roomPresenter = roomPresenter;
        this.fragment = roomFragment;
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
        //roomListener.showToast("Call ");
        CheckBox checkBoxSelectAll = fragment.requireView().findViewById(R.id.checkbox_select_all);

        checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.binding.ivCheckBox.setChecked(true);
                selectList.clear();
                selectList.addAll(rooms);
                updateList();
                // Thực hiện các hành động khác khi checkbox được chọn
            } else {
                holder.binding.ivCheckBox.setChecked(false);
                selectList.clear();
                updateList();
                // Thực hiện các hành động khi checkbox không được chọn
            }
        });


        roomListener.deleteListAll(selectList);

        holder.setRoomData(rooms.get(position), position);
        Room room = rooms.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isEnabled=true;
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

    public List<Room> getSelectList() {
        roomListener.showToast(selectList.size()+"");
        return selectList;
    }

    public void setSelectList(List<Room> selectList) {
        this.selectList = selectList;
    }

    public void performCheckBoxes(RoomAdapter.RoomViewHolder holder, int position) {
        holder.binding.ivCheckBox.setOnClickListener(v -> {
            if (holder.binding.ivCheckBox.isChecked()) {
                // Item nào được setChecked laf true thì add vào list
                selectList.add(rooms.get(holder.getAdapterPosition()));
            } else {
                selectList.remove(rooms.get(holder.getAdapterPosition()));
            }

            //roomListener.showToast(getSelectList().size() + "");
        });

        holder.itemView.setOnClickListener(v -> {
            if (isEnabled) { // Nếu thanh actionMode vẫn còn hiện
                ClickItem(holder);
            } else {
                roomListener.onRoomClick(rooms.get(holder.getAdapterPosition()));
            }
        });

        if ( isVisible[position]) {
            holder.binding.frmImage.setVisibility(View.INVISIBLE);
            holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
            holder.binding.ivCheckBox.setChecked(selectList.contains(rooms.get(position)));
        } else {
            holder.binding.ivCheckBox.setChecked(false);
            holder.binding.ivCheckBox.setVisibility(View.GONE);
            holder.binding.frmImage.setVisibility(View.VISIBLE);
        }
    }

    public void cancelDeleteAll(){
        if(rooms.size() == 0){
            roomListener.cancelDelectAll();
            roomListener.noRoomData();
        } else {
            isEnabled = false;
            isSelectAll = false;
            selectList.clear();
            toggleAllCheckboxes(false);
            roomPresenter.getRooms("init");
            roomListener.cancelDelectAll();
            updateList();
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }
    public void ClickItem(RoomAdapter.RoomViewHolder holder) {
        Room s = rooms.get(holder.getAdapterPosition());
        if (!holder.binding.ivCheckBox.isChecked()) {
            holder.binding.ivCheckBox.setChecked(true);
            if (!selectList.contains(s)) {
                selectList.add(s);
            }
        } else {
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

            String hoTen = binding.edtHoTen.getText().toString();
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
                binding.btnAddContract.setVisibility(View.VISIBLE);
            } else{
                binding.btnAddContract.setVisibility(View.GONE);
            }

            if (binding.edtTrangThaiThanhToan.getText().toString().equals("Đã thanh toán")) {
                binding.edtTrangThaiThanhToan.setTextColor(Color.parseColor("#008000")); // Màu xanh lá cây
            } else if (binding.edtTrangThaiThanhToan.getText().toString().equals("Chưa thanh toán")) {
                binding.edtTrangThaiThanhToan.setTextColor(Color.parseColor("#0000FF")); // Màu xanh dương (primary)
            } else if (binding.edtTrangThaiThanhToan.getText().toString().equals("Trễ hạn thanh toán")) {
                binding.edtTrangThaiThanhToan.setTextColor(Color.parseColor("#FF0000")); // Màu đỏ
            } else if (binding.edtTrangThaiThanhToan.getText().toString().equals("Chưa gửi hóa đơn")) {
                binding.edtTrangThaiThanhToan.setTextColor(Color.parseColor("#000000")); // Màu đen
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
        notifyDataSetChanged();
        isCheckBoxClicked = isClicked;
        //notifyDataSetChanged();
    }
    public void updateList() {
        notifyDataSetChanged();

    }

    public List<Room> checkList()
    {
        notifyDataSetChanged();
        return  selectList;
    }
}
