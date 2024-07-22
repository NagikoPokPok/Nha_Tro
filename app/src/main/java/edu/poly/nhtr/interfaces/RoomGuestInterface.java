package edu.poly.nhtr.interfaces;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;

import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.models.Guest;

public interface RoomGuestInterface {
    interface View {


        void showError(String message);
        void showNoDataFound();

        void showLoadingOfFunctions(int id);

        void hideLoadingOfFunctions(int id);

        String getInfoRoomFromGoogleAccount();

        void putGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, DocumentReference documentReference);

        void showToast(String message);

        void showErrorMessage(String message, int id);

        void showLoading();

        void hideLoading();

        void disableAddGuestButton();

        void enableAddGuestButton();

        void openPopup(android.view.View view, Guest guest, ItemContainerGuestBinding binding);

        void openDialogSuccess(int id);

        void dialogClose();
        boolean isAdded2();
    }

    interface Presenter {


        void addGuestToFirebase(Guest guest);

        void getGuests(String roomId);

        // void deleteGuest(String guestId);

        void deleteGuest(Guest guest);

        void updateGuestInFirebase(Guest guest);

        void handleNameChanged(String name, TextInputLayout textInputLayout, int boxStrokeColor);

        void handlePhoneChanged(String phone, TextInputLayout textInputLayout, int boxStrokeColor);

        void handleCheckInDateChanged(String checkInDate, String roomId, TextInputLayout textInputLayout, int boxStrokeColor);
    }
}
