package edu.poly.nhtr.models;

import java.util.Date;

public class Index {
    String nameRoom, indexID;
    String homeID, roomID;
    String electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew;
    int month, year;
    Date dateObject;

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public Index(String homeID, String indexID, String nameRoom, String electricityIndexOld, String electricityIndexNew, String waterIndexOld, String waterIndexNew, int month, int year) {
        this.homeID = homeID;
        this.indexID = indexID;
        this.nameRoom = nameRoom;
        this.electricityIndexOld = electricityIndexOld;
        this.electricityIndexNew = electricityIndexNew;
        this.waterIndexOld = waterIndexOld;
        this.waterIndexNew = waterIndexNew;
        this.month = month;
        this.year = year;
        //this.dateObject = dateObject;

    }

    public String getHomeID() {
        return homeID;
    }

    public void setHomeID(String homeID) {
        this.homeID = homeID;
    }

    public Index(String homeID) {
        this.homeID = homeID;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public String getElectricityIndexOld() {
        return electricityIndexOld;
    }

    public String getElectricityIndexNew() {
        return electricityIndexNew;
    }

    public String getWaterIndexOld() {
        return waterIndexOld;
    }

    public String getWaterIndexNew() {
        return waterIndexNew;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIndexID() {
        return indexID;
    }

    public void setIndexID(String indexID) {
        this.indexID = indexID;
    }

    public Date getDateObject() {
        return dateObject;
    }
}
