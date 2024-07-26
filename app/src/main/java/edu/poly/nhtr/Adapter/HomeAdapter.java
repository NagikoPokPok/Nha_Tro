package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.presenters.HomePresenter;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private int lastActionPosition = 0;

    private List<Home> homes;
    private final HomeListener homeListener;
    private final HomePresenter homePresenter;
    Fragment fragment;
    boolean isEnabled = false;
    boolean isSelectAll = false;
    ArrayList<Home> selectList = new ArrayList<>();
    boolean[] isVisible;

    public HomeAdapter(List<Home> homes, HomeListener homeListener, HomeFragment homeFragment) {
        this.homes = homes;
        this.homeListener = homeListener;
        this.fragment = homeFragment;
        isVisible = new boolean[homes.size()];

        homePresenter = new HomePresenter(this.homeListener);


    }

    public ArrayList<Home> getSelectList() {
        return selectList;
    }

    public void setSelectList(ArrayList<Home> selectList) {
        this.selectList = selectList;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerHomesBinding itemContainerHomesBinding = ItemContainerHomesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);


        return new HomeViewHolder(itemContainerHomesBinding);

    }

    public void performClick(HomeViewHolder holder) {
        if (!isEnabled) {
            isEnabled = true;
            ClickItem(holder);
            toggleAllCheckboxes(true);
            updateList();
        } else {
            ClickItem(holder);
        }

    }


    public void setHome(HomeViewHolder holder, int position) {
        holder.setHomeData(homes.get(position), position);
    }

    public void performCheckBoxes(HomeAdapter.HomeViewHolder holder, int position) {
        holder.binding.ivCheckBox.setOnClickListener(v -> {
            if (holder.binding.ivCheckBox.isChecked()) {
                // Item nào được setChecked laf true thì add vào list
                selectList.add(homes.get(holder.getAdapterPosition()));
            } else {
                selectList.remove(homes.get(holder.getAdapterPosition()));
            }

        });

        holder.itemView.setOnClickListener(v -> {
            if (isEnabled) { // Nếu thanh actionMode vẫn còn hiện
                ClickItem(holder);
            } else {
                homeListener.onHomeClicked(homes.get(holder.getAdapterPosition()));
            }
        });

        if (isVisible[position]) {
            holder.binding.frmImage.setVisibility(View.INVISIBLE);
            holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
            holder.binding.ivCheckBox.setChecked(selectList.contains(homes.get(position)));
        } else {
            holder.binding.ivCheckBox.setChecked(false);
            holder.binding.ivCheckBox.setVisibility(View.GONE);
            holder.binding.frmImage.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        setHome(holder, position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isEnabled = true;
                homeListener.showLayoutDeleteHomes();
                holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
                holder.binding.frmImage.setVisibility(View.GONE);
                ClickItem(holder);
                toggleAllCheckboxes(true);
                updateList();
                return false;
            }
        });
        performCheckBoxes(holder, position);

        homeListener.putListSelected(selectList);

        //roomListener.showToast("Call ");
        CheckBox checkBoxSelectAll = fragment.requireView().findViewById(R.id.checkbox_select_all);

        checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.binding.ivCheckBox.setChecked(true);
                selectList.clear();
                selectList.addAll(homes);
                updateList();
                // Thực hiện các hành động khác khi checkbox được chọn
            } else {
                holder.binding.ivCheckBox.setChecked(false);
                selectList.clear();
                updateList();
                // Thực hiện các hành động khi checkbox không được chọn
            }
        });

        TextView buttonCancelDelete = fragment.requireView().findViewById(R.id.txt_cancel_delete_home);
        buttonCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEnabled = false;
                isSelectAll = false;
                selectList.clear();
                toggleAllCheckboxes(false);
                //homePresenter.getHomes("init");
                homeListener.hideLayoutDeleteHomes();
                updateList();
            }
        });
    }

    public void cancelDeleteAll() {


    }


    private void ClickItem(HomeViewHolder holder) {
        // Item được click vào sẽ hiện check box true và được add vào trong selectList (cái này quan trọng)
        Home s = homes.get(holder.getAdapterPosition());
        if (!holder.binding.ivCheckBox.isChecked()) {
            holder.binding.ivCheckBox.setChecked(true);
            selectList.add(s);
        } else {
            holder.binding.ivCheckBox.setChecked(false);
            selectList.remove(s);
        }
    }

    // Hàm này dùng để giúp xác nhận tất cả item sẽ hiện check box hay sẽ bỏ check box
    private void toggleAllCheckboxes(boolean show) {
        Arrays.fill(isVisible, show);
    }

    @Override
    public int getItemCount() {
        return homes.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {

        public ItemContainerHomesBinding binding;

        HomeViewHolder(ItemContainerHomesBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            this.binding = itemContainerUserBinding;

        }

        void setHomeData(Home home, int position) {
            binding.txtNameHome.setText(home.nameHome);
            binding.txtHomeAddress.setText(home.addressHome);
            binding.txtOrdinalNumber.setText(String.valueOf(position + 1));

            binding.txtNumberOfRooms.setText(String.valueOf(home.numberOfRooms));
            binding.txtNumberOfRoomsIsEmpty.setText(String.valueOf(home.numberOfRoomsAvailable));
            binding.txtNumberOfRoomsIsDelayedPayBill.setText(String.valueOf(home.numberOfRoomsAreDelayedPayBill));

            // Định dạng số tiền
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String revenueOfMonth = numberFormat.format(home.revenueOfMonth) + " VNĐ";
            binding.txtRevenueOfMonth.setText(revenueOfMonth);

            binding.getRoot().setOnClickListener(v -> homeListener.onHomeClicked(home));

            binding.imgMenu.setOnClickListener(v -> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                homeListener.openPopup(v, home, binding);
            });


        }
    }

    public void addHome(List<Home> homes) {

        //homeList.add(home);
        lastActionPosition = homes.size() - 1;

    }

    public void updateHome(int position) {
        //homeList.set(position, home);
        lastActionPosition = position - 1;

    }

    public void removeHome(int position) {
        lastActionPosition = position - 2;
    }

    public int getLastActionPosition() {
        return lastActionPosition;
    }

    public void updateList() {
        notifyDataSetChanged();

    }

    public void isSelectAllChecked(boolean isSelectAllClicked) {
        this.isSelectAll = isSelectAllClicked;
        notifyDataSetChanged();
    }


}
