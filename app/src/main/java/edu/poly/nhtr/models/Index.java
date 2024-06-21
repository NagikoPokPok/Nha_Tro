package edu.poly.nhtr.models;

public class Index {
    String nameRoom;
    String electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew;

    public Index(String nameRoom, String electricityIndexOld, String electricityIndexNew, String waterIndexOld, String waterIndexNew) {
        this.nameRoom = nameRoom;
        this.electricityIndexOld = electricityIndexOld;
        this.electricityIndexNew = electricityIndexNew;
        this.waterIndexOld = waterIndexOld;
        this.waterIndexNew = waterIndexNew;
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
}
