package edu.poly.nhtr.Adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.icu.text.Collator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.databinding.ItemContainerServiceInDetailBillBinding;
import edu.poly.nhtr.models.RoomService;

public class ServiceInDetailBillAdapter extends RecyclerView.Adapter<ServiceInDetailBillAdapter.ViewHolder> {
    private final List<RoomService> roomServices;

    public ServiceInDetailBillAdapter(List<RoomService> roomServices) {
        this.roomServices = roomServices;
        roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));
    }

    @NonNull
    @Override
    public ServiceInDetailBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerServiceInDetailBillBinding binding = ItemContainerServiceInDetailBillBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ServiceInDetailBillAdapter.ViewHolder holder, int position) {
        RoomService roomService = roomServices.get(position);

        String title = "Tiền " + roomService.getServiceName().toLowerCase();
        holder.binding.txtTitle.setText(title);

        String price = roomService.getService().getPrice() + "đ/" + roomService.getService().getUnit();
        holder.binding.txtPrice.setText(price);

        String titleQuantity;
        if (roomService.getService().getFee_base() == 0)
            titleQuantity = "Số " + roomService.getServiceName().toLowerCase() +" đã sử dụng";
        else
            titleQuantity = "Số " + roomService.getService().getUnit().toLowerCase() + " sử dụng";
        holder.binding.txtTitleQuantity.setText(titleQuantity);

        String quantity = roomService.getQuantity() + " " + roomService.getService().getUnit();
        holder.binding.txtQuantity.setText(quantity);

        String intoMoney = (roomService.getService().getPrice() * roomService.getQuantity()) + "đ";
        holder.binding.txtIntoMoney.setText(intoMoney);

        //Set detail index for electric and water
        if (roomService.getService().getFee_base() == 0){
            String oldIndex = roomService.getOldIndex() + " " + roomService.getService().getUnit();
            holder.binding.txtOldIndex.setText(oldIndex);

            String newIndex = roomService.getNewIndex() + " " + roomService.getService().getUnit();
            holder.binding.txtOldIndex.setText(newIndex);
        }else {
            holder.binding.layoutTitleIndex.setVisibility(View.GONE);
            holder.binding.layoutIndex.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return roomServices.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ItemContainerServiceInDetailBillBinding binding;

        ViewHolder(ItemContainerServiceInDetailBillBinding itemContainerServiceInDetailBillBinding) {
            super(itemContainerServiceInDetailBillBinding.getRoot());
            this.binding = itemContainerServiceInDetailBillBinding;
        }
    }
}
