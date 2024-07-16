package edu.poly.nhtr.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;


public class RoomBill implements Serializable {
    public String billID;

    public String getBillID() {
        return billID;
    }
    public Date dateCreateBill, datePayBill;
    public int month, year;
    public int numberOfDaysToPayBill;
    public boolean isNotPayBill, isPayedBill, isDelayPayBill;
    public boolean isNotGiveBill;
    public long moneyOfRoom, moneyOfService, moneyOfAddOrMinus, totalOfMoney, totalOfMoneyNeededPay;
    public boolean isMoneyOfAdd, isMoneyOfMinus;
    public int numberOfDaysLived;
    public String reasonForAddOrMinusMoney;
    public String userID, roomID, roomName;
    public boolean isWaterIsIndex;

    public long getTotalOfMoneyNeededPay() {
        return totalOfMoneyNeededPay;
    }

    public boolean isMoneyOfAdd() {
        return isMoneyOfAdd;
    }

    public boolean isMoneyOfMinus() {
        return isMoneyOfMinus;
    }

    public Date getDateCreateBill() {
        return dateCreateBill;
    }

    public Date getDatePayBill() {
        return datePayBill;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getNumberOfDaysToPayBill() {
        return numberOfDaysToPayBill;
    }

    public boolean isNotPayBill() {
        return isNotPayBill;
    }

    public boolean isPayedBill() {
        return isPayedBill;
    }

    public boolean isDelayPayBill() {
        return isDelayPayBill;
    }

    public boolean isNotGiveBill() {
        return isNotGiveBill;
    }

    public long getMoneyOfRoom() {
        return moneyOfRoom;
    }

    public long getMoneyOfService() {
        return moneyOfService;
    }

    public long getMoneyOfAddOrMinus() {
        return moneyOfAddOrMinus;
    }

    public long getTotalOfMoney() {
        return totalOfMoney;
    }

    public int getNumberOfDaysLived() {
        return numberOfDaysLived;
    }

    public String getReasonForAddOrMinusMoney() {
        return reasonForAddOrMinusMoney;
    }

    public String getUserID() {
        return userID;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isWaterIsIndex() {
        return isWaterIsIndex;
    }
}
