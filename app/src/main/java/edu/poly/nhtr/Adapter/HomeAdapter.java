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
import java.util.Objects;

import edu.poly.nhtr.Activity.MainViewModel;
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
    MainViewModel mainViewModel;
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

        mainViewModel = ViewModelProviders.of(fragment)
                .get(MainViewModel.class);

    }


    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerHomesBinding itemContainerHomesBinding = ItemContainerHomesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);

        mainViewModel = ViewModelProviders.of(fragment)
                .get(MainViewModel.class);
        return new HomeViewHolder(itemContainerHomesBinding);

    }

    public void performClick(HomeViewHolder holder) {
        if (!isEnabled) {
            homeListener.hideFrameTop();
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
                    //notifyDataSetChanged(); // Câu lệnh dùng để yêu cầu cập nhật lại giao diện của toàn bộ danh sách.
                    updateList();

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
                        if (selectList.isEmpty()) {
                            homeListener.showToast("Please select home to delete");
                        } else {
                            homeListener.openDeleteListHomeDialog(selectList, mode);
                        }
                        // Không gọi mode.finish() ở đây để giữ ActionMode hoạt động.
                    } else if (id == R.id.menu_select_all) {
                        if (selectList.size() == homes.size()) {
                            isSelectAll = false;
                            selectList.clear();
                        } else {
                            isSelectAll = true;
                            selectList.clear();
                            selectList.addAll(homes);
                        }
                        mainViewModel.setText(String.valueOf(selectList.size()));
                        //notifyDataSetChanged();
                        updateList();
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    isEnabled = false;
                    isSelectAll = false;
                    selectList.clear();
                    toggleAllCheckboxes(false);
                    homeListener.showFrameTop();
                    homePresenter.getHomes("init");

                    //notifyDataSetChanged();
                    updateList();

                }
            };
            ((AppCompatActivity) fragment.requireContext()).startActionMode(callback);
        } else {
            ClickItem(holder);
        }

    }

    public void performCheckBoxes(HomeViewHolder holder, int position) {
        holder.binding.ivCheckBox.setOnClickListener(v -> {
            if (holder.binding.ivCheckBox.isChecked()) {
                // Item nào được setChecked laf true thì add vào list
                selectList.add(homes.get(holder.getAdapterPosition()));
            } else {
                selectList.remove(homes.get(holder.getAdapterPosition()));
            }
            mainViewModel.setText(String.valueOf(selectList.size()));
        });

        holder.itemView.setOnClickListener(v -> {
            if (isEnabled) { // Nếu thanh actionMode vẫn còn hiện
                ClickItem(holder);
            } else {
                homeListener.onHomeClicked(homes.get(holder.getAdapterPosition()));
            }
        });

        if (isSelectAll || isVisible[position]) {
            // an nut 3 cham
            holder.binding.frmImage.setVisibility(View.INVISIBLE);
            // Lệnh này hiển thị check box lên cho toàn bộ item
            holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
            // Lệnh dưới này kiểm tra: Item nào có trong selectList thì sẽ được setChecked là true, ko có thì false
            holder.binding.ivCheckBox.setChecked(selectList.contains(homes.get(position))); // Hàm contains sẽ trả về kiểu false/true
        } else { // Khi isVisible = false (khi gọi hàm onDestroyActionMode) thì cho toàn bộ ivCheckBox về false
            holder.binding.ivCheckBox.setChecked(false);
            holder.binding.ivCheckBox.setVisibility(View.GONE);
            holder.binding.frmImage.setVisibility(View.VISIBLE);
        }
    }

    public void setHome(HomeViewHolder holder, int position) {
        holder.setHomeData(homes.get(position), position);
    }




    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {

        setHome(holder, position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                performClick(holder);
                return false;
            }
        });
        performCheckBoxes(holder, position);

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
        mainViewModel.setText(String.valueOf(selectList.size()));
    }

    // Hàm này dùng để giúp xác nhận tất cả item sẽ hiện check box hay sẽ bỏ check box
    private void toggleAllCheckboxes(boolean show) {
        Arrays.fill(isVisible, show);
        // for (int i = 0; i < isVisible.length; i++) {
        //            isVisible[i] = show;
        //        }
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
            binding.getRoot().setOnClickListener(v -> homeListener.onHomeClicked(home));

            binding.imgMenu.setOnClickListener(v -> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                homeListener.openPopup(v, home, binding);
            });

            binding.txtNumberOfRooms.setText(String.valueOf(home.numberOfRooms));
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



}
