package edu.poly.nhtr.models;

import java.util.Date;

public class MainGuest extends Guest {
    private String cccdNumber;
    private Date dateOfBirth;
    private String gender;
    private Date createDate;
    private double roomPrice;
    private Date expirationDate;
    private Date payDate;
    private int daysUntilDueDate;
    private String cccdImageFront;
    private String cccdImageBack;
    private String contractImageFront;
    private String contractImageBack;

    public MainGuest(String idGuest, String nameGuest, String phoneGuest, boolean fileStatus, Date dateIn, String cccdNumber, Date dateOfBirth, String gender, Date createDate, double roomPrice, Date expirationDate, Date payDate, int daysUntilDueDate, String cccdImageFront, String cccdImageBack, String contractImageFront, String contractImageBack) {
        super(idGuest, nameGuest, phoneGuest, fileStatus, dateIn);
        this.cccdNumber = cccdNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.createDate = createDate;
        this.roomPrice = roomPrice;
        this.expirationDate = expirationDate;
        this.payDate = payDate;
        this.daysUntilDueDate = daysUntilDueDate;
        this.cccdImageFront = cccdImageFront;
        this.cccdImageBack = cccdImageBack;
        this.contractImageFront = contractImageFront;
        this.contractImageBack = contractImageBack;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public int getDaysUntilDueDate() {
        return daysUntilDueDate;
    }

    public void setDaysUntilDueDate(int daysUntilDueDate) {
        this.daysUntilDueDate = daysUntilDueDate;
    }

    public String getCccdImageFront() {
        return cccdImageFront;
    }

    public void setCccdImageFront(String cccdImageFront) {
        this.cccdImageFront = cccdImageFront;
    }

    public String getCccdImageBack() {
        return cccdImageBack;
    }

    public void setCccdImageBack(String cccdImageBack) {
        this.cccdImageBack = cccdImageBack;
    }

    public String getContractImageFront() {
        return contractImageFront;
    }

    public void setContractImageFront(String contractImageFront) {
        this.contractImageFront = contractImageFront;
    }

    public String getContractImageBack() {
        return contractImageBack;
    }

    public void setContractImageBack(String contractImageBack) {
        this.contractImageBack = contractImageBack;
    }
}
