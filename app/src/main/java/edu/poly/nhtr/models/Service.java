package edu.poly.nhtr.models;

public class Service {
    public String name, codeImage;
    public int price;
    public String unit;
    public String fee_base;
    public String note;

    public Service(String name, String codeImage, int price) {
        this.name = name;
        this.codeImage = codeImage;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeImage() {
        return codeImage;
    }

    public void setCodeImage(String codeImage) {
        this.codeImage = codeImage;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFee_base() {
        return fee_base;
    }

    public void setFee_base(String fee_base) {
        this.fee_base = fee_base;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
