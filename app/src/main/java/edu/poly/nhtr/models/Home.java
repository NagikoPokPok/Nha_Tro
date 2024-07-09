package edu.poly.nhtr.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Home implements Serializable {
    public String nameHome, addressHome, idHome;
    public int numberOfRooms, numberOfRoomsAvailable;
    public HashMap<String, String> existingHomes = new HashMap<>();

    public Date dateObject;
    public Boolean isHaveService;


    public Home()
    {

    }

    @NonNull
    @Override
    public String toString() {
        return nameHome;
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

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(int numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public int getNumberOfRoomsAvailable() {
        return numberOfRoomsAvailable;
    }

    public void setNumberOfRoomsAvailable(int numberOfRoomsAvailable) {
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

    public Boolean getHaveService() {
        return isHaveService;
    }

    public void setHaveService(Boolean haveService) {
        isHaveService = haveService;
    }
}
