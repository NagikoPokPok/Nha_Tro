package edu.poly.nhtr.models;

public class MainGuest extends Guest {
    private int totalMembers;
    private String cccdNumber;
    private String dateOfBirth; // Change to String to match input format
    private String gender;
    private String createDate; // Change to String to match input format
    private double roomPrice;
    private String expirationDate; // Change to String to match input format
    private String payDate; // Change to String to match input format
    private int daysUntilDueDate;
    private String cccdImageFront;
    private String cccdImageBack;
    private String contractImageFront;
    private String contractImageBack;


    public MainGuest(int totalMembers, String idGuest, String nameGuest, String phoneGuest, boolean fileStatus, // Change dateIn to String
                     String cccdNumber, String dateOfBirth, String gender, String createDate, double roomPrice,
                     String expirationDate, String payDate, int daysUntilDueDate, String cccdImageFront, String cccdImageBack,
                     String contractImageFront, String contractImageBack) {
        super(idGuest, nameGuest, phoneGuest, fileStatus);
        this.totalMembers = totalMembers;
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

    public MainGuest() {
        super();
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
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
