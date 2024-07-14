package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerRevenueOfMonthBinding;
import edu.poly.nhtr.models.RevenueOfMonthModel;

public class RevenueOfMonthAdapter extends RecyclerView.Adapter<RevenueOfMonthAdapter.ViewHolder> {
    private final Context context;
    private final List<RevenueOfMonthModel> revenueOfMonthList;

    public RevenueOfMonthAdapter(Context context, List<RevenueOfMonthModel> revenueOfMonthList) {
        this.context = context;
        this.revenueOfMonthList = revenueOfMonthList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemContainerRevenueOfMonthBinding binding = ItemContainerRevenueOfMonthBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RevenueOfMonthModel revenue = revenueOfMonthList.get(position);
        holder.binding.txtMonth.setText(revenue.getMonth());
        holder.binding.txtRevenueOfMonth.setText(String.valueOf(revenue.getRevenueOfMonth()));
    }

    @Override
    public int getItemCount() {
        return revenueOfMonthList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRevenueOfMonthBinding binding;

        public ViewHolder(@NonNull ItemContainerRevenueOfMonthBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
