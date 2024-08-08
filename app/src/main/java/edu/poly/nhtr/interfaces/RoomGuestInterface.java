package edu.poly.nhtr.interfaces;

import android.view.View;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.presenters.RoomBillPresenter;
import edu.poly.nhtr.presenters.RoomGuestPresenter;

public interface RoomGuestInterface {
    interface View {

        android.view.View getRootView();
        void onGuestClick(Guest guest);

        void showError(String message);
        void showNoDataFound();

        void showLoadingOfFunctions(int id);

        void hideLoadingOfFunctions(int id);

        String getInfoHomeFromGoogleAccount();

        String getInfoRoomFromGoogleAccount();

        void putGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, String homeId, DocumentReference documentReference);

        void showToast(String message);

        void showErrorMessage(String message, int id);

        void showLoading();

        void hideLoading();

        void disableAddGuestButton();

        void enableAddGuestButton();

        void openPopup(android.view.View view, Guest guest, ItemContainerGuestBinding binding);


        void openPopupMainGuest(android.view.View view, MainGuest mainGuest, ItemContainerGuestBinding binding);

        void openDialogSuccess(int id);

        void dialogClose();
        
        boolean isAdded2();

        void cancelDeleteAll();

        void noGuestData();

        void openDeleteListDialog(List<Guest> listGuest);

        void deleteListAll(List<Guest> list);


        void setDeleteAllUI();
    }

    interface Presenter {


        void addGuestToFirebase(Guest guest);

        void getGuests(String roomId);

        void deleteGuest(Guest guest);

        void updateGuestInFirebase(Guest guest);

        void handleNameChanged(String name, TextInputLayout textInputLayout, int boxStrokeColor);

        void handlePhoneChanged(String phone, TextInputLayout textInputLayout, int boxStrokeColor);

        void handleCheckInDateChanged(String checkInDate, String roomId, TextInputLayout textInputLayout, int boxStrokeColor);

        void deleteListGuests(List<Guest> listGuest);

        void getDayOfMakeBill(String roomID, RoomGuestPresenter.OnGetDayOfMakeBillCompleteListener listener);
        void checkNotificationIsGiven(String roomID, String homeID, RoomGuestPresenter.OnGetNotificationCompleteListener listener);
        void checkNotificationByHeader(String roomID, String header, String body, RoomGuestPresenter.OnGetNotificationByHeaderBody listener);
    }
}
