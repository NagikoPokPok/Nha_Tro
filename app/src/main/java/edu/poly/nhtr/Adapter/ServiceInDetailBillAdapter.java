package edu.poly.nhtr.Adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.databinding.ItemContainerServiceInDetailBillBinding;
import edu.poly.nhtr.models.RoomService;

public class ServiceInDetailBillAdapter extends RecyclerView.Adapter<ServiceInDetailBillAdapter.ViewHolder> {
    private final List<RoomService> roomServices;

    public ServiceInDetailBillAdapter(List<RoomService> roomServices) {
        this.roomServices = roomServices;
    }

    @NonNull
    @Override
    public ServiceInDetailBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerServiceInDetailBillBinding itemContainerServiceInDetailBillBinding = ItemContainerServiceInDetailBillBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new ViewHolder(itemContainerServiceInDetailBillBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull ServiceInDetailBillAdapter.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ItemContainerServiceInDetailBillBinding binding;

        ViewHolder(ItemContainerServiceInDetailBillBinding itemContainerServiceInDetailBillBinding) {
            super(itemContainerServiceInDetailBillBinding.getRoot());
            this.binding = itemContainerServiceInDetailBillBinding;
        }
    }
}
