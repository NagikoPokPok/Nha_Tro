package edu.poly.nhtr.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class RoomViewModel extends ViewModel {
    private final MutableLiveData<Room> room = new MutableLiveData<>();
    private final MutableLiveData<List<MainGuest>> mainGuests = new MutableLiveData<>();

    public LiveData<Room> getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room.setValue(room);
    }

    public LiveData<List<MainGuest>> getMainGuests() {
        return mainGuests;
    }

    public void setMainGuests(List<MainGuest> mainGuests) {
        this.mainGuests.setValue(mainGuests);
    }
}
