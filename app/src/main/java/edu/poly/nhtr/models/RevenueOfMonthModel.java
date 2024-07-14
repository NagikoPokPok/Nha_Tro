package edu.poly.nhtr.models;

public class RevenueOfMonthModel {
    public String month;
    public Long revenueOfMonth;

    public String getMonth() {
        return month;
    }

    public Long getRevenueOfMonth() {
        return revenueOfMonth;
    }

    public RevenueOfMonthModel(String month, Long revenueOfMonth) {
        this.month = month;
        this.revenueOfMonth = revenueOfMonth;
    }
}
