package edu.poly.nhtr.models;


public class Guest {
    protected String nameGuest, phoneGuest, idGuest, idRoom, idHome;
    protected boolean fileStatus;
    protected String dateIn;

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

    public Guest(String nameGuest, String phoneGuest, boolean fileStatus, String dateIn) {
        this.nameGuest = nameGuest;
        this.phoneGuest = phoneGuest;
        this.fileStatus = fileStatus;
        this.dateIn = dateIn;
    }


    public Guest() {

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

}
