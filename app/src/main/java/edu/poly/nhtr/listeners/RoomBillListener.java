package edu.poly.nhtr.listeners;

import android.view.View;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerInformationOfBillBinding;
import edu.poly.nhtr.models.RoomBill;

public interface RoomBillListener {
    void setBillList(List<RoomBill> billList);
    void makeBillClick(RoomBill bill);
    void openPopUp(View view, RoomBill bill, ItemContainerInformationOfBillBinding binding);
    void showToast(String message);
    void showLayoutNoData();
    void hideLayoutNoData();
    void showLoading();
    void hideLoading();
    void showDialog(int id);
    void showButtonLoading(int id);
    void hideButtonLoading(int id);
    void closeDialog();
}
