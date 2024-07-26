package edu.poly.nhtr.models;

import java.util.Objects;

public class PlusOrMinusMoney {
    private int stt;
    private String title;
    private Boolean isPlus;
    private int money;
    private String reason;

    public PlusOrMinusMoney(String title, Boolean isPlus, int stt) {
        this.title = title;
        this.isPlus = isPlus;
        this.stt = stt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPlus() {
        return isPlus;
    }

    public void setPlus(Boolean plus) {
        isPlus = plus;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
