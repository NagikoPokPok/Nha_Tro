package edu.poly.nhtr.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
        holder.setHomeData(homes.get(position));
    }

    @Override
    public int getItemCount() {
        return homes.size();
    }

    class HomeViewHolder extends RecyclerView.ViewHolder
    {

        ItemContainerHomesBinding binding;
        HomeViewHolder(ItemContainerHomesBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            this.binding = itemContainerUserBinding;
        }
        void setHomeData(Home home){
            binding.txtNameHome.setText(home.nameHome);
            binding.txtHomeAddress.setText(home.addressHome);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homeListener.onUserClicked(home);
                }
            });
        }
    }

}
