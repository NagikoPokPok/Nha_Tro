package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Class.ServiceUtils;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.listeners.RoomServiceListener;
import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Service;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private final List<Service> services;
    private LayoutInflater inflater;
    Context context;
    private final ServiceListener serviceListener;
    private final RoomServiceListener roomServiceListener;

    private final RecyclerView recyclerView;
    public List<Service> getSelectedServices() {
        return selectedServices;
    }

    private final List<Service> selectedServices = new ArrayList<>();


    public ServiceAdapter(Context context, List<Service> services, ServiceListener serviceListener, RecyclerView recyclerView) {
        this.services = services;
        //this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.serviceListener = serviceListener;
        this.recyclerView = recyclerView;
        this.roomServiceListener = null;
    }

    public ServiceAdapter(Context context, List<Service> services, RoomServiceListener roomServiceListener, RecyclerView recyclerView) {
        this.services = services;
        this.context = context;
        this.roomServiceListener = roomServiceListener;
        this.recyclerView = recyclerView;
        this.serviceListener = null;
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

        holder.feedbackChooseListener(services.get(position));

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && serviceListener != null) {
//                    serviceListener.onServiceItemCLick(services.get(adapterPosition), recyclerView, adapterPosition);
                    serviceListener.onServiceClicked(services.get(adapterPosition), recyclerView, adapterPosition);
                } else if (adapterPosition != RecyclerView.NO_POSITION && roomServiceListener != null) {

                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                    if (layoutManager instanceof LinearLayoutManager) {
                        int orientation = ((LinearLayoutManager) layoutManager).getOrientation();
                        if (orientation == LinearLayoutManager.HORIZONTAL) {
                            // RecyclerView theo chiều ngang
                            roomServiceListener.onChooseServiceClicked(services.get(adapterPosition), adapterPosition);
                            holder.onChooseListener(services.get(adapterPosition));
                        } else if (orientation == LinearLayoutManager.VERTICAL) {
                            // RecyclerView theo chiều dọc
                            roomServiceListener.onServiceItemCLick(services.get(adapterPosition), recyclerView, adapterPosition);
                        }
                    } else if (layoutManager instanceof GridLayoutManager) {
                        int orientation = ((GridLayoutManager) layoutManager).getOrientation();
                        if (orientation == GridLayoutManager.HORIZONTAL) {
                            // RecyclerView theo chiều ngang
                            roomServiceListener.onChooseServiceClicked(services.get(adapterPosition), adapterPosition);
                            holder.onChooseListener(services.get(adapterPosition));
                        } else if (orientation == GridLayoutManager.VERTICAL) {
                            // RecyclerView theo chiều dọc
                            roomServiceListener.onServiceItemCLick(services.get(adapterPosition), recyclerView, adapterPosition);
                        }
                    } else {
                        // Loại LayoutManager khác hoặc không xác định
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (services!=null)
            return services.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ItemServiceBinding binding;
        public ViewHolder(@NonNull ItemServiceBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }

        public void setServiceData(Service service){
            if(context.getClass().getSimpleName().equals("MainDetailedRoomActivity")) binding.status.setVisibility(View.VISIBLE);
            if (service!=null){
                binding.nameService.setText(service.getName());
                binding.imageService.setImageBitmap(ServiceUtils.getConversionImage(service.getCodeImage()));
                binding.feeService.setText(String.valueOf(service.getPrice()));
            }

            //Thiết lập hành động nhấn vào
//            if (serviceListener != null){
//                binding.getRoot().setOnClickListener(v -> serviceListener.onServiceClicked(service, recyclerView, adapterPosition));
//                binding.cardViewService.setOnClickListener(v -> serviceListener.openPopup(v, service, binding));
//            }else if (roomServiceListener != null){
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//            }
        }

        public void onChooseListener(Service service){


            //Hành động chọn
            if (selectedServices.contains(service)) selectedServices.remove(service);
            else {
                selectedServices.add(service);
            }
            notifyDataSetChanged();

        }

        public void feedbackChooseListener(Service service){
            // Tạo GradientDrawable cho viền
            GradientDrawable border = new GradientDrawable();
            border.setColor(0xFFEDEDED); // Màu nền xanh nhạt (Light Blue) với mã màu #0054FF
            border.setStroke(3, 0xFF000000); // Độ dày và màu viền (2dp, màu đen)
            border.setCornerRadius(9); // Bo góc 9dp

            //Hành động chọn
            if (!selectedServices.contains(service)){
                binding.getRoot().setRadius(30f);
                binding.getRoot().setBackground(null);
                binding.getRoot().setBackgroundTintList(null);
            }else {
                binding.getRoot().setRadius(9f);
                binding.getRoot().setBackground(border);
//                binding.getRoot().setBackgroundTintList(itemView.getContext().getResources().getColorStateList(R.color.colorPrimary));
            }
        }


    }



    public void cancelChooseListener(){
        selectedServices.clear();
        notifyDataSetChanged();
    }

}
