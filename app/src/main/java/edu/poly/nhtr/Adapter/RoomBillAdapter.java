package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.databinding.ItemContainerInformationOfBillBinding;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.presenters.RoomBillPresenter;

public class RoomBillAdapter extends RecyclerView.Adapter<RoomBillAdapter.ViewHolder> {

    Context context;
    List<RoomBill> billList;
    RoomBillPresenter roomBillPresenter;
    RoomBillListener roomBillListener;

    public RoomBillAdapter(Context context, List<RoomBill> billList, RoomBillPresenter roomBillPresenter, RoomBillListener roomBillListener) {
        this.context = context;
        this.billList = billList;
        this.roomBillPresenter = roomBillPresenter;
        this.roomBillListener = roomBillListener;
    }

    @NonNull
    @Override
    public RoomBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemContainerInformationOfBillBinding binding = ItemContainerInformationOfBillBinding.inflate(inflater, parent, false);
        return new RoomBillAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomBillAdapter.ViewHolder holder, int position) {

        RoomBill bill = billList.get(position);

        holder.binding.month.setText("T." + bill.getMonth());

        holder.binding.year.setText(String.valueOf(bill.getYear()));

        // Định dạng ngày tháng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

// Chuyển đổi LocalDate thành String
        String dateCreateBill = bill.getDateCreateBill().format(formatter);
        String datePayBill = bill.getDatePayBill().format(formatter);

// Hiển thị trong giao diện người dùng
        holder.binding.dateCreateBill.setText(dateCreateBill);
        holder.binding.datePayBill.setText(datePayBill);


    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerInformationOfBillBinding binding;

        public ViewHolder(@NonNull ItemContainerInformationOfBillBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    public void setBillList(List<RoomBill> billList) {
        this.billList = billList;
        if (this.billList.isEmpty()) {
            //notificationListener.showLayoutNoData();
        } else {
            //notificationListener.hideLayoutNoData();
            notifyDataSetChanged();
        }
        //notificationListener.hideLoading();
    }
}
