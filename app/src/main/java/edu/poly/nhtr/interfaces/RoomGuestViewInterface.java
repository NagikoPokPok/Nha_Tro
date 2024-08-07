package edu.poly.nhtr.interfaces;

import com.google.android.material.textfield.TextInputLayout;

import edu.poly.nhtr.models.Guest;

public interface RoomGuestViewInterface {
    interface View {
        void dialogClose();

        boolean isAdded2();

        void openDialogSuccess(int id);

        void showLoadingOfFunctions(int id);

        void hideLoadingOfFunctions(int id);

        void showGuestDetails(Guest guest);

        String getInfoRoomFromGoogleAccount();

        String getInfoHomeFromGoogleAccount();

        void showLoading();

        void hideLoading();

        void showDeleteGuestDialog(Guest guest);
        void showSuccessDialog(int layoutId);
        void disableMenuForMainGuest();
        void showToast(String message);
    }

    interface Presenter {
        void fetchGuestDetails(String guestId);


        void deleteGuest(Guest guest);

        void updateGuestInFirebase(Guest guest);

        void handleNameChanged(String name, TextInputLayout textInputLayout, int boxStrokeColor);

        void handlePhoneChanged(String phone, TextInputLayout textInputLayout, int boxStrokeColor);

        void handleCheckInDateChanged(String checkInDate, String roomId, TextInputLayout textInputLayout, int boxStrokeColor);

    }
}
