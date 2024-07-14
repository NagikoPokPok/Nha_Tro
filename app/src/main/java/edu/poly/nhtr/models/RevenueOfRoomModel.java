package edu.poly.nhtr.models;

public class RevenueOfRoomModel {
    public String room;
    public Long revenueOfRoom;

    public RevenueOfRoomModel(String room, Long revenueOfRoom) {
        this.room = room;
        this.revenueOfRoom = revenueOfRoom;
    }

    public String getRoom() {
        return room;
    }

    public Long getRevenueOfRoom() {
        return revenueOfRoom;
    }
}
