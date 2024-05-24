package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Service;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private final List<Service> services;
    private LayoutInflater inflater;
    private final ServiceListener listener;


    public ServiceAdapter(Context context, List<Service> services, ServiceListener listener) {
        this.services = services;
        //this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceAdapter.ViewHolder holder, int position) {
        // thiết lập dữ liệu
        holder.setServiceData(services.get(position));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ItemServiceBinding binding;
        public ViewHolder(@NonNull ItemServiceBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }

        public void setServiceData(Service service){
            binding.nameService.setText(service.getName());
            binding.feeService.setText(service.getPrice());

            //Thiết lập hành động nhấn vào
            binding.getRoot().setOnClickListener(v -> listener.onServiceClicked(service));
            binding.cardViewService.setOnClickListener(v -> listener.openPopup(v, service, binding));
        }
    }


}
