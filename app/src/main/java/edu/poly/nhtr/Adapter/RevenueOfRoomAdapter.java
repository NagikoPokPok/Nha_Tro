package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerRevenueOfMonthBinding;
import edu.poly.nhtr.databinding.ItemContainerRevenueOfRoomBinding;
import edu.poly.nhtr.models.RevenueOfMonthModel;
import edu.poly.nhtr.models.RevenueOfRoomModel;

public class RevenueOfRoomAdapter extends RecyclerView.Adapter<RevenueOfRoomAdapter.ViewHolder> {
    private final Context context;
    private final List<RevenueOfRoomModel> revenueOfRoomList;

    public RevenueOfRoomAdapter(Context context, List<RevenueOfRoomModel> revenueOfMonthList) {
        this.context = context;
        this.revenueOfRoomList = revenueOfMonthList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemContainerRevenueOfRoomBinding binding = ItemContainerRevenueOfRoomBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RevenueOfRoomModel revenue = revenueOfRoomList.get(position);
        holder.binding.txtRoom.setText(revenue.getRoom());
        holder.binding.txtRevenueOfRoom.setText(String.valueOf(revenue.getRevenueOfRoom()));
    }

    @Override
    public int getItemCount() {
        return revenueOfRoomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRevenueOfRoomBinding binding;

        public ViewHolder(@NonNull ItemContainerRevenueOfRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
