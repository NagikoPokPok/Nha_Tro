package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.databinding.ItemContainerPlusOrMinusMoneyInDetailBillBinding;
import edu.poly.nhtr.models.PlusOrMinusMoney;

public class PlusOrMinusMoneyInDetailBillAdapter extends RecyclerView.Adapter<PlusOrMinusMoneyInDetailBillAdapter.ViewHolder> {
    private final List<PlusOrMinusMoney> plusOrMinusMoneyList;

    public PlusOrMinusMoneyInDetailBillAdapter(List<PlusOrMinusMoney> plusOrMinusMoneyList) {
        this.plusOrMinusMoneyList = plusOrMinusMoneyList;
    }

    @NonNull
    @Override
    public PlusOrMinusMoneyInDetailBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerPlusOrMinusMoneyInDetailBillBinding binding = ItemContainerPlusOrMinusMoneyInDetailBillBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlusOrMinusMoneyInDetailBillAdapter.ViewHolder holder, int position) {
        PlusOrMinusMoney plusOrMinusMoney = plusOrMinusMoneyList.get(position);

        String titlePlusOrMinus = "Tiền ";
        if (plusOrMinusMoney.getPlus()) titlePlusOrMinus += "cộng thêm";
        else titlePlusOrMinus += "trừ bớt";
        holder.binding.txtTitlePlusOrMinus.setText(titlePlusOrMinus);

        String money = formatMoney(plusOrMinusMoney.getMoney());
        holder.binding.txtPlusOrMinusMoney.setText(money);

        String reason = plusOrMinusMoney.getReason();
        holder.binding.txtReason.setText(reason);
    }

    @Override
    public int getItemCount() {
        return plusOrMinusMoneyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerPlusOrMinusMoneyInDetailBillBinding binding;
        public ViewHolder(@NonNull ItemContainerPlusOrMinusMoneyInDetailBillBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }
    }

    private String formatMoney(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(amount) + " VNĐ";
    }
}
