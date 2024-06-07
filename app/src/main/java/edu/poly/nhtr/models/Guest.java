package edu.poly.nhtr.models;

import java.util.Date;

public class Guest {
    public String nameGuest, phoneGuest, idGuest, idRoom, idHome;
    public boolean fileStatus;
    public Date dateIn;

    public Guest(String nameGuest, String phoneGuest, boolean fileStatus, Date dateIn) {
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
        this.dateIn = dateIn;
    }

    public String getGuestId() {
        return idGuest;
    }

    public String getNameGuest() {
        return nameGuest;
    }

    public String getPhoneGuest() {
        return phoneGuest;
    }

    public boolean getFileStatus() {
        return fileStatus;
    }

    public Date getDateIn() {
        return dateIn;
    }

}
