package edu.poly.nhtr.models;


import java.io.Serializable;

public class Guest implements Serializable {
    protected String nameGuest, phoneGuest, idGuest, idRoom, idHome;
    protected boolean fileStatus;
    protected String dateIn;
    protected String cccdNumber;
    protected String cccdImageFront;
    protected String cccdImageBack;


    public Guest(String idGuest, String nameGuest, String phoneGuest, boolean fileStatus) {
        this.idGuest = idGuest;
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
    }

    public Guest(String nameGuest, String phoneGuest, boolean fileStatus, String dateIn) {
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
        this.dateIn = dateIn;
    }

    public Guest(String nameGuest, String phoneGuest, String idGuest, boolean fileStatus, String dateIn, String cccdNumber, String cccdImageFront, String cccdImageBack) {
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.idGuest = idGuest;
        this.fileStatus = fileStatus;
        this.dateIn = dateIn;
        this.cccdNumber = cccdNumber;
        this.cccdImageFront = cccdImageFront;
        this.cccdImageBack = cccdImageBack;
    }


    public Guest() {

    }

    public String getHomeId() {
        return idHome;
    }

    public void setHomeId(String idHome) {
        this.idHome = idHome;
    }

    public String getRoomId() {
        return idRoom;
    }

    public void setRoomId(String idRoom) {
        this.idRoom = idRoom;
    }

    public String getGuestId() {
        return idGuest;
    }

    public void setGuestId(String idGuest) {
        this.idGuest = idGuest;
    }

    public String getNameGuest() {
        return nameGuest;
    }

    public void setNameGuest(String nameGuest) {
        this.nameGuest = nameGuest;
    }

    public String getPhoneGuest() {
        return phoneGuest;
    }

    public void setPhoneGuest(String phoneGuest) {
        this.phoneGuest = phoneGuest;
    }

    public boolean isFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(boolean fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getDateIn() {
        return dateIn;
    }

    public void setDateIn(String dateIn) {
        this.dateIn = dateIn;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
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
}
