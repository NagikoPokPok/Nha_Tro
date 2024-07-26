package edu.poly.nhtr.models;

import edu.poly.nhtr.presenters.RoomServicePresenter;

public class RoomService {
    private String roomServiceId;
    private String roomId;
    private String serviceId;
    private Room room;
    private Service service;
    private int quantity;
    private String oldIndex, newIndex;

    public RoomService(String roomServiceId, String roomId, String serviceId) {
        this.roomServiceId = roomServiceId;
        this.roomId = roomId;
        this.serviceId = serviceId;
    }

    public RoomService(String roomServiceId, String roomId, String serviceId, int quantity) {
        this.roomServiceId = roomServiceId;
        this.roomId = roomId;
        this.serviceId = serviceId;
        this.quantity = quantity;
    }

    public String getServiceName(){return service.getName();}

    public String getRoomServiceId() {
        return roomServiceId;
    }

    public void setRoomServiceId(String roomServiceId) {
        this.roomServiceId = roomServiceId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(String oldIndex) {
        this.oldIndex = oldIndex;
    }

    public String getNewIndex() {
        return newIndex;
    }

    public void setNewIndex(String newIndex) {
        this.newIndex = newIndex;
    }
}
