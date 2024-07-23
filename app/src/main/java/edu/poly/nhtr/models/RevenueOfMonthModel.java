package edu.poly.nhtr.models;

public class RevenueOfMonthModel {
    public String month;
    public String year;

    public String getYear() {
        return year;
    }

    public String revenueOfMonth;

    public String getMonth() {
        return month;
    }

    public String getRevenueOfMonth() {
        return revenueOfMonth;
    }

    public RevenueOfMonthModel(String month, String revenueOfMonth) {
        this.month = month;
        this.revenueOfMonth = revenueOfMonth;
    }
}
