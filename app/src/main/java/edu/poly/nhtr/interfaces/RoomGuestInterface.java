package edu.poly.nhtr.interfaces;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;

public interface RoomGuestInterface {
    interface View {


        void showError(String message);
        void showNoDataFound();

        void showLoadingOfFunctions(int id);

        String getInfoRoomFromGoogleAccount();

        void putGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, DocumentReference documentReference);

        void showToast(String message);

        void showLoading();

        void hideLoading();

        void disableAddGuestButton();

        void enableAddGuestButton();
    }

    interface Presenter {


        void addGuestToFirebase(Guest guest);

        void getGuests(String roomId);
    }
}
