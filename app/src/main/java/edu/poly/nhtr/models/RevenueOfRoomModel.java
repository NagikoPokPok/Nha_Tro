package edu.poly.nhtr.models;

public class RevenueOfRoomModel {
    public String room;
    public String revenueOfRoom;

    public RevenueOfRoomModel(String room, String revenueOfRoom) {
        this.room = room;
        this.revenueOfRoom = revenueOfRoom;
    }

    public String getRoom() {
        return room;
    }

    public String getRevenueOfRoom() {
        return revenueOfRoom;
    }
}
