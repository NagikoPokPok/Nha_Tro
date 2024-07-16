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
import java.util.ArrayList;
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
    private boolean isDeleteClicked = false;
    private boolean isSelectAllClicked = false;
    private final List<RoomBill> selectedBills = new ArrayList<>();
    private boolean multiSelectMode = false;

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

        //Click button
        holder.binding.btnMakeBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomBillListener.makeBillClick(bill);
            }
        });

        holder.binding.frmMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.binding.imgCircleMenu.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorGray));
                roomBillListener.openPopUp(v, bill, holder.binding);
            }
        });


        //delete many bills
        if(isDeleteClicked)
        {
            holder.binding.checkBox.setVisibility(View.VISIBLE);
            holder.binding.frmMenu.setVisibility(View.GONE);
        }else{
            holder.binding.checkBox.setVisibility(View.GONE);
            holder.binding.frmMenu.setVisibility(View.VISIBLE);
        }

        if(isSelectAllClicked)
        {
            holder.binding.checkBox.setChecked(true);
            selectedBills.clear();
            selectedBills.addAll(billList);
        }else{
            holder.binding.checkBox.setChecked(false);
            selectedBills.clear();
        }

        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                if (holder.binding.checkBox.isChecked()) {
                    holder.binding.checkBox.setChecked(false);
                    selectedBills.remove(bill);
                } else {
                    holder.binding.checkBox.setChecked(true);
                    selectedBills.add(bill);
                }
            }
        });



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
            roomBillListener.showLayoutNoData();
        } else {
            roomBillListener.hideLayoutNoData();
            notifyDataSetChanged();
        }
        roomBillListener.hideLoading();
    }

    public void isDeleteChecked(boolean isDeleteChecked)
    {
        this.isDeleteClicked = isDeleteChecked;
        this.multiSelectMode = isDeleteChecked;
        notifyDataSetChanged();
    }

    public void isSelectAllChecked(boolean isSelectAllClicked)
    {
        this.isSelectAllClicked = isSelectAllClicked;
        notifyDataSetChanged();
    }
}
