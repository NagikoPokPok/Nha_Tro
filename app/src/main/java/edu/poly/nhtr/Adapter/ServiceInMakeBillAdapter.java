package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.ItemServiceInMakeBillBinding;
import edu.poly.nhtr.models.RoomService;

public class ServiceInMakeBillAdapter extends RecyclerView.Adapter<ServiceInMakeBillAdapter.ViewHolder> {

    private final List<RoomService> roomServices;

    public ServiceInMakeBillAdapter(List<RoomService> roomServices) {
        this.roomServices = roomServices;
    }

    @NonNull
    @Override
    public ServiceInMakeBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceInMakeBillBinding binding = ItemServiceInMakeBillBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceInMakeBillAdapter.ViewHolder holder, int position) {
        String serviceName = "Tiền " + roomServices.get(position).getServiceName().toLowerCase();
        String titleQuantity = "Số " + roomServices.get(position).getServiceName().toLowerCase() +" đã sử dụng";

        holder.binding.txtServiceName.setText(serviceName);
        holder.binding.txtServiceFee.setText(roomServices.get(position).getService().getPrice());
        holder.binding.txtTitleQuantity.setText(titleQuantity);
        holder.binding.txtQuantity.setText(roomServices.get(position).getQuantity());
    }

    @Override
    public int getItemCount() {
        return roomServices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ItemServiceInMakeBillBinding binding;
        public ViewHolder(@NonNull ItemServiceInMakeBillBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }
    }
}
