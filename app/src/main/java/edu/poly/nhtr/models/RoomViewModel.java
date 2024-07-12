package edu.poly.nhtr.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class RoomViewModel extends ViewModel {
    private final MutableLiveData<Room> room = new MutableLiveData<>();
    private final MutableLiveData<List<Object>> guests = new MutableLiveData<>();

    public LiveData<Room> getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room.setValue(room);
    }

    public LiveData<List<Object>> getGuests() {
        return guests;
    }

    public void setGuests(List<Object> guestsList) {
        guests.setValue(guestsList);
    }
}
