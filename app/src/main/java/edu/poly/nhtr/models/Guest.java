package edu.poly.nhtr.models;

import java.util.Date;

public class Guest {
    public String nameGuest, phoneGuest, idGuest, idRoom, idHome;
    public boolean fileStatus;
    public String dateIn;

    public Guest(String idGuest, String nameGuest, String phoneGuest, boolean fileStatus, String dateIn) {
        this.idGuest = idGuest;
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
        this.dateIn = dateIn;
    }

    public Guest(String idGuest, String nameGuest, String phoneGuest, boolean fileStatus) {
        this.idGuest = idGuest;
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
    }

    public Guest() {

    }


    public String getIdGuest() {
        return idGuest;
    }

    public void setIdGuest(String idGuest) {
        this.idGuest = idGuest;
    }

    public String getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(String idRoom) {
        this.idRoom = idRoom;
    }

    public String getIdHome() {
        return idHome;
    }

    public void setIdHome(String idHome) {
        this.idHome = idHome;
    }

    public boolean isFileStatus() {
        return fileStatus;
    }

    public String getGuestId() {
        return idGuest;
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

    public boolean getFileStatus() {
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

}
