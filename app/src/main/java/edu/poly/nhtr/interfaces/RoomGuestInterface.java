package edu.poly.nhtr.interfaces;

import java.util.List;

import edu.poly.nhtr.models.MainGuest;

public interface RoomGuestInterface {
    interface View {
        void showMainGuest(List<MainGuest> mainGuests);
        void showError(String message);
        void showNoDataFound();
    }

    interface Presenter {
        void getMainGuests(String roomId);

    }
}
