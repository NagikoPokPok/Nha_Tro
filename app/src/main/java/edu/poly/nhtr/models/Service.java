package edu.poly.nhtr.models;

public class Service {
    private String idHomeParent;
    private String idService;
    private String name, codeImage;
    private int price;
    private String unit;
    private int fee_base;
    private String note;
    private Boolean isDeletable;
    private Boolean isApply;

    public Service(String name, String codeImage, int price) {
        this.name = name;
        this.codeImage = codeImage;
        this.price = price;
        this.isDeletable = true;
        this.isApply = false;
    }

    public Service(String idHomeParent, String name, String codeImage, int price, String unit, int fee_base, String note, Boolean isDeletable, Boolean isApply) {
        this.idHomeParent = idHomeParent;
        this.name = name;
        this.codeImage = codeImage;
        this.price = price;
        this.unit = unit;
        this.fee_base = fee_base;
        this.isDeletable = isDeletable;
        this.isApply = isApply;
        this.note = note;
    }

    public Service(String idHomeParent, String idService, String name, String codeImage, int price, String unit, int fee_base, String note, Boolean isDeletable, Boolean isApply) {
        this.idHomeParent = idHomeParent;
        this.idService = idService;
        this.name = name;
        this.codeImage = codeImage;
        this.price = price;
        this.unit = unit;
        this.fee_base = fee_base;
        this.note = note;
        this.isDeletable = isDeletable;
        this.isApply = isApply;
    }

    public String getIdService() {
        return idService;
    }

    public void setIdService(String idService) {
        this.idService = idService;
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

    public int getFee_base() {
        return fee_base;
    }

    public void setFee_base(int fee_base) {
        this.fee_base = fee_base;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getDeletable() {
        return isDeletable;
    }

    public void setDeletable(Boolean deletable) {
        isDeletable = deletable;
    }

    public Boolean getApply() {
        return isApply;
    }

    public void setApply(Boolean apply) {
        isApply = apply;
    }

    public String getIdHomeParent() {
        return idHomeParent;
    }

    public void setIdHomeParent(String idHomeParent) {
        this.idHomeParent = idHomeParent;
    }

    public boolean isElectricOrWater() {
        if (getName().equals("Điện")||getName().equals("Nước")) return true;
        return false;
    }
}
