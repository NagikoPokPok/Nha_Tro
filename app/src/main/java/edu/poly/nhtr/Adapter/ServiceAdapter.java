package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.Class.ServiceUtils;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Service;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private final List<Service> services;
    private LayoutInflater inflater;
    Context context;
    private final ServiceListener listener;
    private final RecyclerView recyclerView;


    public ServiceAdapter(Context context, List<Service> services, ServiceListener listener, RecyclerView recyclerView) {
        this.services = services;
        //this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
        this.recyclerView = recyclerView;
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

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onServiceItemCLick(services.get(adapterPosition), recyclerView, adapterPosition);
                }
            }
        });
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
            if(context.getClass().getSimpleName().equals("MainDetailedRoomActivity")) binding.status.setVisibility(View.VISIBLE);
            binding.nameService.setText(service.getName());
            binding.imageService.setImageBitmap(ServiceUtils.getConversionImage(service.getCodeImage()));
            binding.feeService.setText(""+service.getPrice());

            //Thiết lập hành động nhấn vào
            binding.getRoot().setOnClickListener(v -> listener.onServiceClicked(service));
            binding.cardViewService.setOnClickListener(v -> listener.openPopup(v, service, binding));
        }
    }


}
