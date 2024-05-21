package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private int lastActionPosition = 0;

    private final List<Home> homes;
    private final HomeListener homeListener;

    public HomeAdapter(List<Home> homes, HomeListener homeListener) {
        this.homes = homes;
        this.homeListener = homeListener;
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

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.setHomeData(homes.get(position), position);
    }

    @Override
    public int getItemCount() {
        return homes.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder
    {

        public ItemContainerHomesBinding binding;
        HomeViewHolder(ItemContainerHomesBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            this.binding = itemContainerUserBinding;
        }
        void setHomeData(Home home, int position){
            binding.txtNameHome.setText(home.nameHome);
            binding.txtHomeAddress.setText(home.addressHome);
            binding.txtOrdinalNumber.setText(String.valueOf(position + 1));
            binding.getRoot().setOnClickListener(v -> homeListener.onHomeClicked(home));

            binding.imgMenu.setOnClickListener(v -> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                homeListener.openPopup(v, home, binding);
            });
        }
    }

    public void addHome(List<Home>homes) {

        //homeList.add(home);
        lastActionPosition = homes.size() - 1;

    }

    public void updateHome(int position, Home home) {
        //homeList.set(position, home);
        lastActionPosition = position;
        notifyItemChanged(position);
    }

    public void removeHome(int position) {
        //homeList.remove(position);
        lastActionPosition = position < homes.size() ? position : homes.size() - 1;
        notifyItemRemoved(position);
    }

    public int getLastActionPosition() {
        return lastActionPosition;
    }

}
