package edu.poly.nhtr.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Room  implements Serializable {
    public String nameRoom;
    public String nameUser;

    public String getPhoneNumer() {
        return phoneNumer;
    }

    public void setPhoneNumer(String phoneNumer) {
        this.phoneNumer = phoneNumer;
    }

    public String phoneNumer;
    public String numberOfMemberLiving;
    public String status;
    public String price;
    public String describe;
    public String roomId;
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
    public Room (String nameRoom, String price, String describe){
        this.nameRoom=nameRoom;
        this.price=price;
        this.describe=describe;
    }

    public Room(){}

    public Room(String nameRoom, String roomId) {
        this.nameRoom = nameRoom;
        this.roomId = roomId;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public void setNameRoom(String nameRoom) {
        this.nameRoom = nameRoom;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getNumberOfMemberLiving() {
        return numberOfMemberLiving;
    }

    public void setNumberOfMemberLiving(String numberOfMemberLiving) {
        this.numberOfMemberLiving = numberOfMemberLiving;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public HashMap<String, String> existingRooms = new HashMap<>();
    public Date dateObject;

    public HashMap<String, String> getExistingHomes() {
        return existingRooms;
    }

    public void setExistingHomes(HashMap<String, String> existingHomes) {
        this.existingRooms = existingHomes;
    }

}
