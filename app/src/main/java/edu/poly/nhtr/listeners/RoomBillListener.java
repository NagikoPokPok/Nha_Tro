package edu.poly.nhtr.listeners;

import java.util.List;

import edu.poly.nhtr.models.RoomBill;

public interface RoomBillListener {
    void setBillList(List<RoomBill> billList);
    void makeBillClick(RoomBill bill);
}
