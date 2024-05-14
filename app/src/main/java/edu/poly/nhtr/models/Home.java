package edu.poly.nhtr.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Home implements Serializable {
    public String nameHome, addressHome, numberOfRooms, numberOfRoomsAvailable, idHome;
    public HashMap<String, String> existingHomes = new HashMap<>();

    public Date dateObject;

    public HashMap<String, String> getExistingHomes() {
        return existingHomes;
    }

    public void setExistingHomes(HashMap<String, String> existingHomes) {
        this.existingHomes = existingHomes;
    }

    public Home()
    {

    }

    public Home(String nameHome, String addressHome) {
        this.nameHome = nameHome;
        this.addressHome = addressHome;
    }

    public String getNameHome() {
        return nameHome;
    }

    public void setNameHome(String nameHome) {
        this.nameHome = nameHome;
    }

    public String getAddressHome() {
        return addressHome;
    }

    public void setAddressHome(String addressHome) {
        this.addressHome = addressHome;
    }

    public String getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(String numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public String getNumberOfRoomsAvailable() {
        return numberOfRoomsAvailable;
    }

    public void setNumberOfRoomsAvailable(String numberOfRoomsAvailable) {
        this.numberOfRoomsAvailable = numberOfRoomsAvailable;
    }

    public String getIdHome() {
        return idHome;
    }

    public void setIdHome(String idHome) {
        this.idHome = idHome;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }
}
