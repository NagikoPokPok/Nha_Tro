package edu.poly.nhtr.Adapter;

import static com.google.common.io.Resources.getResource;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.R;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Chuyển đổi Date thành String
        String dateCreateBill = dateFormat.format(bill.getDateCreateBill());
        String datePayBill = dateFormat.format(bill.getDatePayBill());

        holder.binding.dateCreateBill.setText(dateCreateBill);
        holder.binding.datePayBill.setText(datePayBill);

        if(bill.isNotGiveBill || bill.isPayedBill || bill.isDelayPayBill){
            holder.binding.layoutStatusOfPay.setVisibility(View.VISIBLE);
        }else{
            holder.binding.layoutStatusOfPay.setVisibility(View.INVISIBLE);
        }

        if(bill.isNotPayBill()){
            holder.binding.imgStatusOfPay.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOrange));
            holder.binding.txtStatusOfPay.setText("Đã thanh toán");
        }else if(bill.isPayedBill()){
            holder.binding.imgStatusOfPay.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorGreen));
            holder.binding.txtStatusOfPay.setText("Chưa thanh toán");
        }else if(bill.isDelayPayBill()){
            holder.binding.imgStatusOfPay.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorRed));
            holder.binding.txtStatusOfPay.setText("Quá hạn thanh toán");
        }


        if(bill.isNotGiveBill()){
            holder.binding.imgStatusOfGiveBill.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOrange));
            holder.binding.txtStatusOfGiveBill.setText("Chưa gửi phiếu");
        }else{
            holder.binding.layoutStatusOfGiveBill.setVisibility(View.INVISIBLE);
        }

        // Định dạng số tiền
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        if(bill.isMoneyOfAdd()){
            holder.binding.txtMoneyAddOrMinus.setText("Tiền cộng thêm:");
            String moneyOfAddOrMinus = numberFormat.format(bill.getMoneyOfAddOrMinus()) + " VNĐ";
            holder.binding.moneyOfAddOrMinus.setText(moneyOfAddOrMinus);
        }else if(bill.isMoneyOfMinus()){
            holder.binding.txtMoneyAddOrMinus.setText("Tiền giảm đi:");
            String moneyOfAddOrMinus = numberFormat.format(bill.getMoneyOfAddOrMinus()) + " VNĐ";
            holder.binding.moneyOfAddOrMinus.setText(moneyOfAddOrMinus);
        }else{
            holder.binding.txtMoneyAddOrMinus.setText("Tiền cộng thêm/ giảm:");
            holder.binding.moneyOfAddOrMinus.setText("0 VNĐ");
        }

        String moneyOfRoom = numberFormat.format(bill.getMoneyOfRoom()) + " VNĐ";
        holder.binding.moneyOfRoom.setText(moneyOfRoom);

        String moneyOfService = numberFormat.format(bill.getMoneyOfService()) + " VNĐ";
        holder.binding.moneyOfService.setText(moneyOfService);


        String totalOfMoney = numberFormat.format(bill.getTotalOfMoney()) + " VNĐ";
        holder.binding.totalMoney.setText(totalOfMoney);

        String moneyLeftNeedToPay = numberFormat.format(bill.getTotalOfMoneyNeededPay()) + " VNĐ";
        holder.binding.moneyLeftNeedToPay.setText(moneyLeftNeedToPay);


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
