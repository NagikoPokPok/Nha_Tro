package edu.poly.nhtr.Adapter;

import android.icu.text.Collator;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.databinding.ItemServiceInMakeBillBinding;
import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;

public class ServiceInMakeBillAdapter extends RecyclerView.Adapter<ServiceInMakeBillAdapter.ViewHolder> {

    private final List<RoomService> roomServices;
    private final RoomMakeBillListener listener;

    public ServiceInMakeBillAdapter(List<RoomService> roomServices, RoomMakeBillListener listener) {
        this.roomServices = roomServices;
        this.listener = listener;
        roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));
    }

    @NonNull
    @Override
    public ServiceInMakeBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceInMakeBillBinding binding = ItemServiceInMakeBillBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceInMakeBillAdapter.ViewHolder holder, int position) {
        RoomService roomService = roomServices.get(position);
        Service service = roomService.getService();

        String serviceName = "Tiền " + roomService.getServiceName().toLowerCase();
        String titleQuantity;
        if (service.getFee_base() == 0)
            titleQuantity = "Số " + roomService.getServiceName().toLowerCase() +" đã sử dụng";
        else
            titleQuantity = "Số " + service.getUnit().toLowerCase() + " sử dụng";


        holder.binding.txtServiceName.setText(serviceName);
        holder.binding.txtTitleQuantity.setText(titleQuantity);

        String serviceFee = service.getPrice()+"/"+service.getUnit();
        holder.binding.txtServiceFee.setText(serviceFee);

        String quantity = roomServices.get(position).getQuantity()+" "+service.getUnit();
        holder.binding.txtQuantity.setText(quantity);
    }

    @Override
    public int getItemCount() {
        return roomServices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ItemServiceInMakeBillBinding binding;
        public ViewHolder(@NonNull ItemServiceInMakeBillBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }
    }
}
